package org.jim.ticker.dingtalk;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class TextMessage {
    private String text;
    private List<String> atMobiles;
    private boolean isAtAll;

    public String toJsonString() {
        Map<String, Object> items = new HashMap<>();
        items.put("msgtype", "text");
        Map<String, String> textContent = new HashMap<>();
        if (StringUtils.isBlank(this.text)) {
            throw new IllegalArgumentException("text should not be blank");
        } else {
            textContent.put("content", this.text);
            items.put("text", textContent);
            Map<String, Object> atItems = new HashMap<>();
            if (this.atMobiles != null && !this.atMobiles.isEmpty()) {
                atItems.put("atMobiles", this.atMobiles);
            }

            if (this.isAtAll) {
                atItems.put("isAtAll", true);
            }

            items.put("at", atItems);
            return JSON.toJSONString(items);
        }
    }

}
