package org.jim.ticker.service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jim.ticker.auth.ApiSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HbgClient {
    static final int CONN_TIMEOUT = 60;
    static final int READ_TIMEOUT = 60;
    static final int WRITE_TIMEOUT = 60;

    static final String API_URL = "https://api.huobi.pro";

    static final MediaType JSON = MediaType.parse("application/json");

    private String apiHost;
    private OkHttpClient client;

    @Value("${api.key.id:}")
    private String accessKeyId;

    @Value("${api.key.secret:}")
    private String accessKeySecret;

    @PostConstruct
    public void init() {
        apiHost = this.getHost();
        if (null == apiHost) {
            throw new IllegalStateException("Api Host is null");
        }

        client = new OkHttpClient.Builder()
                .proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 9080)))
                .connectTimeout(CONN_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS).writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    public String get(String uri, Map<String, String> params) {
        if (params == null) {
            params = new HashMap<>(0);
        }
        return call("GET", uri, null, params);
    }

    public String post(String uri, Object object) {
        return call("POST", uri, object, new HashMap<>(0));
    }

    private String call(String method, String uri, Object object, Map<String, String> params) {
        ApiSignature sign = new ApiSignature();
        sign.createSignature(this.accessKeyId, this.accessKeySecret, method, apiHost, uri, params);

        try {
            Request.Builder builder = null;
            if ("POST".equals(method)) {
                String payload = com.alibaba.fastjson.JSON.toJSONString(object);
                RequestBody body = RequestBody.create(JSON, payload);
                builder = new Request.Builder().url(API_URL + uri + "?" + toQueryString(params)).post(body);
            } else {
                builder = new Request.Builder().url(API_URL + uri + "?" + toQueryString(params)).get();
            }

            Request request = builder.build();
            Response response = client.newCall(request).execute();
            String s = response.body().string();
            return s;
        } catch (IOException e) {
            throw new IllegalStateException("IOException", e);
        }
    }

    private String toQueryString(Map<String, String> params) {
        return String.join("&", params.entrySet().stream().map((entry) -> {
            return entry.getKey() + "=" + ApiSignature.urlEncode(entry.getValue());
        }).collect(Collectors.toList()));
    }

    private String getHost() {
        String host = null;
        try {
            host = new URL(API_URL).getHost();
        } catch (MalformedURLException e) {
            System.err.println("parse API_URL error,system exit!,please check API_URL:" + API_URL );
            System.exit(0);
        }
        return host;
    }

}
