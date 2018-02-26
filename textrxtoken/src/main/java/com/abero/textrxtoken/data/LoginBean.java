package com.abero.testrxtoken.data;

/**
 * Created by HuangJie on 2017/6/28.
 */

public class LoginBean {

    /**
     * tokenExpireTime : 60
     * userAvatar : http://221.4.223.101:8000/avatar/smart_avatar.jpg
     * usertoken : 7067c959a500e48e299e892358ec528d
     * status : false
     */

    private int     tokenExpireTime;
    private String userAvatar;
    private String usertoken;
    private int status;

    public int getTokenExpireTime() {
        return tokenExpireTime;
    }

    public void setTokenExpireTime(int tokenExpireTime) {
        this.tokenExpireTime = tokenExpireTime;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getUsertoken() {
        return usertoken;
    }

    public void setUsertoken(String usertoken) {
        this.usertoken = usertoken;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "LoginBean{" +
                "tokenExpireTime=" + tokenExpireTime +
                ", userAvatar='" + userAvatar + '\'' +
                ", usertoken='" + usertoken + '\'' +
                ", status=" + status +
                '}';
    }
}
