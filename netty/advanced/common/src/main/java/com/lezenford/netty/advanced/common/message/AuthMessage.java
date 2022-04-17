package com.lezenford.netty.advanced.common.message;

public class AuthMessage extends Message{

    private String login = "login1";
    private String password = "pass1";

    public AuthMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

}
