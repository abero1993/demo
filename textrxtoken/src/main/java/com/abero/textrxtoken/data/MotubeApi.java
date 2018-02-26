package com.abero.testrxtoken.data;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Administrator on 2017/8/11.
 */

public interface MotubeApi {

    public interface UserAction {
        //登陆接口
        //http://221.4.223.101:6200/login?userid=van&password=81dc9bdb52d04dc20036dbd8313ed055&type=1
        @POST("login")
        Observable<LoginBean> login(@Query("userid") String userid, @Query("password") String password, @Query
                ("type") int type);


        //更新usertoken接口
        //http://221.4.223.101:6200/updateUsertoken?userid=smart&usertoken=60c1423ea1780816129b270dd3fc63bd
        @POST("updateUsertoken")
        Observable<UpdateUsertokenBean> updateUsertoken(@Query("userid") String userid, @Query("usertoken") String
                usertoken);


    }

    public interface UserList {
        @POST("index/index")
        Observable<IndexBean> index(@Query("usertoken") String usertoken);
    }

    public interface UserAction2 {
        @POST("login")
        Call<LoginBean> login(@Query("userid") String userid, @Query("password") String password, @Query
                ("type") int type);

        //更新usertoken接口
        //http://221.4.223.101:6200/updateUsertoken?userid=smart&usertoken=60c1423ea1780816129b270dd3fc63bd
        @POST("updateUsertoken")
        Call<UpdateUsertokenBean> updateUsertoken(@Query("userid") String userid, @Query("usertoken") String
                usertoken);
    }


}
