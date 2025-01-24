package com.messaging.textrasms.manager.feature.models;

public class AdModel {
    String title;
    String url;
    String iconuri;
    String id;
    String app_id;
    String description;
    int coin;
    boolean isvisible = true;

    public boolean isIsvisible() {
        return isvisible;
    }

    public void setIsvisible(boolean isvisible) {
        this.isvisible = isvisible;
    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getIconuri() {
        return iconuri;
    }

    public void setIconuri(String iconuri) {
        this.iconuri = iconuri;
    }

    public void settitle(String name) {
        this.title = name;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
