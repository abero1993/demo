package com.abero.testrxtoken.data;

/**
 * Created by Administrator on 2017/8/11.
 */

public class GlobalToken {

    private static String sToken;

    public static synchronized void updateToken(String token) {
        sToken = token;
    }

    public static String getToken() {
        return sToken;
    }


}
