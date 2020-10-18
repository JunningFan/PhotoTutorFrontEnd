package com.example.phototutor.ui.login.ui.login;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserView {
    private String displayName;     // Nickname
    private String username;
    private String token;
    //... other data fields that may be accessible to the UI

    LoggedInUserView(String displayName) {
        this.displayName = displayName;
        this.username = "null";
        this.token = "null";
    }

    void setToken(String token) {
        this.token = token;
    }

    void setUsername(String username) {
        this.username = username;
    }

    void setDisplayName(String nickname) {
        this.displayName = nickname;
    }

    String getDisplayName() {
        return displayName;
    }
}