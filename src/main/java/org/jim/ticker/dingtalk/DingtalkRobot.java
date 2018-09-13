package org.jim.ticker.dingtalk;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@Service
public class DingtalkRobot {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Value("${dingtalk.webhook:}")
    private String hook;

    private boolean active;

    private OkHttpClient httpClient;

    @PostConstruct
    public void init() {
        if (StringUtils.isNotEmpty(hook)) {
            active = true;
            httpClient = new OkHttpClient();
        }
    }

    public void notify(String content) {
        if (active) {
            TextMessage textMessage = TextMessage.builder()
                    .text(content)
                    .isAtAll(false).build();
            post(textMessage.toJsonString());
        }
    }

    private void post(String json) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(hook)
                .post(body)
                .build();
        try {
            httpClient.newCall(request).execute().close();
        } catch (IOException e) {
            log.error("Send to ding talk failed.", e);
        }
    }


}
