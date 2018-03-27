package com.rokin.mobile.analytics.network;

import android.support.annotation.IntRange;

/**
 * Created by Sourav
 */

public class Request {
    private ResponseListener responseListener;
    private String data;
    private int type;
    private String tag;

    public Request(ResponseListener responseListener, String data, @IntRange(from = 0) int type) {
        this.responseListener = responseListener;
        this.data = data;
        this.type = type;
    }

    public ResponseListener getResponseListener() {
        return responseListener;
    }

    public void setResponseListener(ResponseListener responseListener) {
        this.responseListener = responseListener;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
