package com.gpsnavigation.maps.gpsroutefinder.routemap.models;


import androidx.annotation.Keep;



import java.util.Map;

@Keep
public class UserModel {

    String userId, name, email, profilePicture,subscriptionType;
    double subscriptionTime;

    public UserModel(String userId, String name, String email, String profilePicture ) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profilePicture = profilePicture;
    }


    public double getSubscriptionTime() {
        return subscriptionTime;
    }

    public void setSubscriptionTime(double subscriptionTime) {
        this.subscriptionTime = subscriptionTime;
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(String subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
