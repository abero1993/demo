package com.abero.testrxtoken;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;



import rx.Subscriber;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    MotubeDataSource dataSource = MotubeDataSource.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.index).setOnClickListener(this);
        findViewById(R.id.update).setOnClickListener(this);
        findViewById(R.id.expired).setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.start:
                dataSource.login("guan", MD5Utils.md5("1234"), 1);
                /*dataSource.login(, new Subscriber<LoginBean>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "login onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "login onError" + e.getMessage());
                    }

                    @Override
                    public void onNext(LoginBean loginBean) {
                        Log.i(TAG, "login onNext: " + loginBean.toString());
                        if (!TextUtils.isEmpty(loginBean.getUsertoken()))
                            GlobalToken.updateToken(loginBean.getUsertoken());

                    }
                });*/
                break;
            case R.id.index:
                dataSource.getIndex(GlobalToken.getToken(), new Subscriber<IndexBean>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "index onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "index onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(IndexBean indexBeanHttpResult) {
                        Log.i(TAG, "index onNext: " + indexBeanHttpResult.toString());

                    }
                });
                break;
            case R.id.update:
                dataSource.updateToken("guan", GlobalToken.getToken(), new Subscriber<UpdateUsertokenBean>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "update onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "update error=" + e.getMessage());
                    }

                    @Override
                    public void onNext(UpdateUsertokenBean updateUsertokenBean) {
                        Log.i(TAG, "update onNext=" + updateUsertokenBean.getUsertoken());
                        if (!TextUtils.isEmpty(updateUsertokenBean.getUsertoken()))
                            GlobalToken.updateToken(updateUsertokenBean.getUsertoken());

                    }
                });
                break;

            case R.id.expired:
                dataSource.getIndex("qwjdljfldldj", new Subscriber<IndexBean>() {
                    @Override
                    public void onCompleted() {
                        Log.i(TAG, "index onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "index onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(IndexBean indexBeanHttpResult) {
                        Log.i(TAG, "index onNext: " + indexBeanHttpResult.toString());

                    }
                });
                break;

            default:
                break;
        }
    }
}
