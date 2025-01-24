package com.messaging.textrasms.manager.model;

import android.net.Uri;

import java.util.ArrayList;

public class Sticker {

    public String name;
    public String stickername;
    public ArrayList<stickerurl> stickerurlArrayList = new ArrayList<>();

    public ArrayList<stickerurl> getStickerurlArrayList() {
        return stickerurlArrayList;
    }

    public void setStickerurlArrayList(ArrayList<stickerurl> stickerurlArrayList) {
        this.stickerurlArrayList = stickerurlArrayList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public class stickerurl {
        public Uri url;

        public Uri getUrl() {
            return url;
        }

        public void setUrl(Uri url) {
            this.url = url;
        }

        public String getName() {
            return stickername;
        }

        public void setName(String name) {
            stickername = name;
        }


    }
}
