package com.abero.testrxtoken.data;

import android.text.TextUtils;
import android.util.Log;

import com.abero.testrxtoken.data.execption.TokenInvalidException;
import com.abero.testrxtoken.data.execption.TokenNotExistException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import retrofit2.http.Query;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by Administrator on 2017/8/11.
 */

public class ProxyHandler implements InvocationHandler {

    private final static String TAG = "Token_Proxy";

    private final static String TOKEN = "usertoken";

    private final static int REFRESH_TOKEN_VALID_TIME = 1000 * 30;
    private static long tokenChangedTime = 0;
    private Throwable mRefreshTokenError = null;
    private boolean mIsTokenNeedRefresh;

    private Object mProxyObject;
    private IGlobalManager mGlobalManager;

    public ProxyHandler(Object proxyObject, IGlobalManager globalManager) {
        mProxyObject = proxyObject;
        mGlobalManager = globalManager;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        return Observable.just(null).flatMap(new Func1<Object, Observable<?>>() {
            @Override
            public Observable<?> call(Object o) {
                try {
                    try {
                        Log.i(TAG, "call: ");
                        if (mIsTokenNeedRefresh) {
                            updateMethodToken(method, args);
                        }
                        return (Observable<?>) method.invoke(mProxyObject, args);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Throwable> observable) {
                return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                    @Override
                    public Observable<?> call(Throwable throwable) {
                        if (throwable instanceof TokenInvalidException) {
                            Log.e(TAG, "TokenInvalidException");
                            return refreshTokenWhenTokenInvalid();
                        } else if (throwable instanceof TokenNotExistException) {
                            // Token 不存在，执行退出登录的操作。（为了防止多个请求，都出现 Token 不存在的问题，
                            // 这里需要取消当前所有的网络请求）
                            Log.e(TAG, "TokenNotExistException");
                            mGlobalManager.exitLogin();
                            return Observable.error(throwable);
                        }
                        return Observable.error(throwable);
                    }
                });
            }
        });
    }

    /**
     * Refresh the token when the current token is invalid.
     *
     * @return Observable
     */
    private Observable<?> refreshTokenWhenTokenInvalid() {
        synchronized (ProxyHandler.class) {
            // Have refreshed the token successfully in the valid time.
            if (new Date().getTime() - tokenChangedTime < REFRESH_TOKEN_VALID_TIME) {
                mIsTokenNeedRefresh = true;
                return Observable.just(true);
            } else {
                // call the refresh token api.
                MotubeDataSource.getInstance().get(MotubeDataSource.BASE_URL_6200, MotubeApi.UserAction.class).login
                        ("guan", MD5Utils.md5("1234"), 1).subscribe(new Subscriber<LoginBean>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError:" + e.getMessage());
                    }

                    @Override
                    public void onNext(LoginBean loginBean) {
                        Log.i(TAG, "onNext token=" + loginBean.getUsertoken());
                        GlobalToken.updateToken(loginBean.getUsertoken());
                        tokenChangedTime = new Date().getTime();
                        mIsTokenNeedRefresh = true;
                    }
                });
                if (mRefreshTokenError != null) {
                    return Observable.error(mRefreshTokenError);
                } else {
                    return Observable.just(true);
                }
            }
        }
    }

    /**
     * Update the token of the args in the method.
     * <p>
     * PS： 因为这里使用的是 GET 请求，所以这里就需要对 Query 的参数名称为 token 的方法。
     * 若是 POST 请求，或者使用 Body ，自行替换。因为 参数数组已经知道，进行遍历找到相应的值，进行替换即可（更新为新的 token 值）。
     */
    private void updateMethodToken(Method method, Object[] args) {
        Log.i(TAG, "updateMethodToken");
        if (mIsTokenNeedRefresh && !TextUtils.isEmpty(GlobalToken.getToken())) {
            Annotation[][] annotationsArray =  method.getParameterAnnotations();
            Annotation[] annotations;
            if (annotationsArray != null && annotationsArray.length > 0) {
                for (int i = 0; i < annotationsArray.length; i++) {
                    annotations = annotationsArray[i];
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof Query) {
                            if (TOKEN.equals(((Query) annotation).value())) {
                                args[i] = GlobalToken.getToken();
                            }
                        }
                    }
                }
            }
            mIsTokenNeedRefresh = false;
        }
    }
}