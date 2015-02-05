package com.wuala.websocket.model;

/**
 * Created by Wang on 10/23/14.
 */
public class User {

    private String username ="";
    private String ip;
    private String device;
    private String password ="";
    private Long userid = 0L;

    public String getUsername() {

        return username;
    }
    public String getPassword() {

        return password;
    }
    public Long getUserid() {

        return userid;
    }

    public void setUsername(String username) {

        this.username = username;
    }
    public void setPassword(String password) {

        this.password = password;
    }

    public void setUserid(Long Userid) {

        this.userid = Userid;
    }

    public String getIp() {

        return ip;
    }

    public void setIp(String ip) {

        this.ip = ip;
    }


    public String getDevice() {

        return device;
    }

    public void setDevice(String device) {

        this.device = device;
    }

}
