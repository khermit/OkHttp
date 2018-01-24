package com.quan.okhttp;

import java.io.IOException;
/*
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/** 原生okhttp使用方法，在此项目中不用，而是使用okhttp_utils
 * Created by quandk on 17-12-14.
 */

public class MyokHttp {
    /*
    private OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    /**
     * get 请求, 必须在子线程中请求
     * @param url 网络链接
     * @return
     * @throws IOException
     */
    /*
    public String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    public String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
    */
}
