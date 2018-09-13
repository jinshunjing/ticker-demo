package org.jim.ticker.model;

import com.alibaba.fastjson.annotation.JSONField;

public class Order {
    public String symbol;

    @JSONField(name="account-id")
    public String accountId;

    public String amount;
    public String price = "0.0";
    public String type;
    public String source = "margin-api";
}
