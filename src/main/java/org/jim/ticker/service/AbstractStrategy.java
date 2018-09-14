package org.jim.ticker.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jim.ticker.dingtalk.DingtalkRobot;
import org.jim.ticker.model.Order;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractStrategy {
    @Autowired
    protected DingtalkRobot dingtalkRobot;

    @Autowired
    protected HbgClient hbgClient;

    protected String symbol;
    protected String accountId;

    protected BigDecimal usdtBalance;
    protected BigDecimal coinBalance;

    protected double benchmark;
    protected List<Double> priceList;
    protected int priceListMaxSize;


    public AbstractStrategy(String symbol, String accountId, double benchmark) {
        this.symbol = symbol;
        this.accountId = accountId;

        usdtBalance = BigDecimal.ZERO;
        coinBalance = BigDecimal.ZERO;

        this.benchmark = benchmark;

        priceList = new ArrayList<>();
        priceListMaxSize = 4 * 60 * 24;
    }

    public abstract void strategy(double price);

    /**
     * Account balance
     *
     */
    public void balance() {
        String resp = hbgClient.get("/v1/account/accounts/" + accountId + "/balance", null);
        parseBalance(resp);
    }

    private void parseBalance(String response) {
        JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(response);
        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray listArray = data.getJSONArray("list");
        for (Object obj : listArray) {
            JSONObject item = (JSONObject) obj;
            String currency = item.getString("currency");
            String type = item.getString("type");
            BigDecimal balance = item.getBigDecimal("balance");

            if (!"trade".equals(type)) {
                continue;
            }

            if ("usdt".equals(currency)) {
                usdtBalance = balance.setScale(0, BigDecimal.ROUND_FLOOR);
            } else {
                coinBalance = balance.setScale(0, BigDecimal.ROUND_FLOOR);
            }
        }
    }

    public String getCoinBalance() {
        return coinBalance.toString();
    }

    public String getUsdtBalance() {
        return usdtBalance.toString();
    }

    /**
     * Price
     *
     */
    public void price() {
        HashMap map = new HashMap();
        map.put("symbol", symbol);
        String resp = hbgClient.get("/market/trade", map);
        double price = this.parsePrice(resp);

        if (priceListMaxSize <= priceList.size()) {
            priceList.clear();
        }
        priceList.add(price);

        this.strategy(price);
    }

    private double parsePrice(String response) {
        JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(response);
        JSONObject tick = jsonObject.getJSONObject("tick");
        JSONArray dataArray = tick.getJSONArray("data");
        JSONObject data = dataArray.getJSONObject(0);
        double price = data.getDouble("price");
        return price;
    }

    public double currentPrice() {
        if (priceList.isEmpty()) {
            return 0.0;
        } else {
            return priceList.get(priceList.size() - 1);
        }
    }

    public double priceChange() {
        if (priceList.isEmpty()) {
            return 0.0;
        }

        double first = benchmark;
        double last = priceList.get(priceList.size() - 1);
        double change = (last - first) / first;
        return ((int) (change * 10000)) / 100.0;
    }

    /**
     * Place order
     *
     * @param order
     */
    public void placeOrder(Order order) {
        String resp = hbgClient.post("/v1/order/orders/place", order);
        String orderId = this.parseOrderId(resp);
    }

    private String parseOrderId(String response) {
        JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(response);
        String data = jsonObject.getString("data");
        return data;
    }

    /**
     * Cancel order
     *
     * @param orderId
     */
    public void cancelOrder(String orderId) {
        String resp = hbgClient.post("/v1/order/orders/" + orderId + "/submitcancel", null);
        System.out.println(resp);
    }


}
