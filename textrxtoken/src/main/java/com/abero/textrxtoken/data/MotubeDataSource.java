package com.abero.testrxtoken.data;

import android.util.Log;

import com.abero.testrxtoken.data.schedulers.SchedulerProvider;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by abero on 2017/8/11.
 */

public class MotubeDataSource implements IGlobalManager {

    private static final String TAG = "MotubeDataSource";

    public static final String BASE_URL_6200 = "http://119.146.223.64:6200/";
    public static final String BASE_URL_6500 = "http://119.146.223.64:6500/";

    private MotubeApi.UserAction userAction;
    private MotubeApi.UserList userList;
    private MotubeApi.UserAction2 userActionApi;
    private static MotubeDataSource sDataSource;


    private MotubeDataSource() {

        userAction = getProxy(BASE_URL_6200, MotubeApi.UserAction.class);
        userList = getProxy(BASE_URL_6500, MotubeApi.UserList.class);
        userActionApi = get(BASE_URL_6200, MotubeApi.UserAction2.class);

    }

    public Retrofit getRetrofit(String url) {

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

        okHttpClientBuilder.connectTimeout(5, TimeUnit.SECONDS);

        OkHttpClient okHttpClient = okHttpClientBuilder.build();

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(MyGsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create());

        return retrofitBuilder.baseUrl(url).build();

    }


    public <T> T get(String baseUrl, Class<T> tClass) {
        return getRetrofit(baseUrl).create(tClass);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(String baseUrl, Class<T> tClass) {
        T t = getRetrofit(baseUrl).create(tClass);
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), new Class<?>[]{tClass}, new ProxyHandler(t, this));
    }

    public static MotubeDataSource getInstance() {
        synchronized (MotubeDataSource.class) {
            if (null == sDataSource)
                sDataSource = new MotubeDataSource();

            return sDataSource;
        }
    }


    public void login(final String userName, String password, int type, Subscriber<UpdateUsertokenBean> subscriber2) {

        Observer<UpdateUsertokenBean> subscriber=new Subscriber<UpdateUsertokenBean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(UpdateUsertokenBean updateUsertokenBean) {
                Log.i(TAG, "onNext: "+updateUsertokenBean.getUsertoken());
            }
        };

        userAction.login(userName, password, type)
                .flatMap(new Func1<LoginBean, Observable<UpdateUsertokenBean>>() {
                    @Override
                    public Observable<UpdateUsertokenBean> call(LoginBean loginBean) {
                        return userAction.updateUsertoken(userName, loginBean.getUsertoken());
                    }
                })
                .subscribeOn(SchedulerProvider.getInstance().io())
                .observeOn(SchedulerProvider.getInstance().ui())
                .subscribe(subscriber);


    }

    public void login(final String userName, String password, int type) {
        userActionApi.login(userName, password, type).enqueue(new Callback<LoginBean>() {
            @Override
            public void onResponse(Call<LoginBean> call, Response<LoginBean> response) {
                Log.i(TAG, "onResponse: login=" + response.body().toString());
                userActionApi.updateUsertoken(userName, response.body().getUsertoken()).enqueue(new Callback<UpdateUsertokenBean>() {


                    @Override
                    public void onResponse(Call<UpdateUsertokenBean> call, Response<UpdateUsertokenBean> response) {

                    }

                    @Override
                    public void onFailure(Call<UpdateUsertokenBean> call, Throwable t) {

                    }
                });
            }

            @Override
            public void onFailure(Call<LoginBean> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }


        });
    }

    public void updateToken(String userName, String token, Subscriber<UpdateUsertokenBean> subscriber) {

        userAction.updateUsertoken(userName, token)
                .subscribeOn(SchedulerProvider.getInstance().io())
                .observeOn(SchedulerProvider.getInstance().ui())
                .subscribe(subscriber);
    }

    public void getIndex(String token, Subscriber<IndexBean> subscriber) {

        Log.i(TAG, "getIndex");
        userList.index(token)
                .subscribeOn(SchedulerProvider.getInstance().io())
                .observeOn(SchedulerProvider.getInstance().ui())
                .subscribe(subscriber);
    }


    @Override
    public void exitLogin() {

    }
}
