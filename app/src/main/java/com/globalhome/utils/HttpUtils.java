package com.globalhome.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.TimeUnit;

import me.jessyan.progressmanager.ProgressManager;
import okhttp3.OkHttpClient;

/**
 * Created by ZTZ on 2018/3/20.
 */

public class HttpUtils {

    private OkHttpClient okHttpClient;

    Handler mHandler;

    private HttpUtils() {
        okHttpClient = ProgressManager.getInstance().with(new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)).build();
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static HttpUtils getInstance() {
        return Holder.httpUtils;
    }

    private static class Holder {
        private static HttpUtils httpUtils = new HttpUtils();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

}
