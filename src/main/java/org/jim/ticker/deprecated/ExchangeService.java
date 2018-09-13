package org.jim.ticker.deprecated;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jim.ticker.deprecated.SellHighStrategy;
import org.jim.ticker.deprecated.SellLowStrategy;
import org.jim.ticker.dingtalk.DingtalkRobot;
import org.jim.ticker.model.Order;
import org.jim.ticker.service.HbgClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class ExchangeService {

    @Autowired
    private DingtalkRobot dingtalkRobot;

    @Autowired
    private HbgClient hbgClient;

    private String symbol;
    private String accountId;

    private BigDecimal usdtBalance;
    private BigDecimal coinBalance;

    private List<Double> priceList;
    private static final int PRICE_MAX_SIZE = 1440;

    private List<String> buyOrderList;
    private List<String> sellOrderList;

    private SellHighStrategy sellHighStrategy;
    private SellLowStrategy sellLowStrategy;

    private double benchmark;

    public ExchangeService(String symbol, String accountId, SellHighStrategy strategy) {
        this(symbol, accountId);

        sellHighStrategy = strategy;
        benchmark = strategy.benchmark;
    }

    public ExchangeService(String symbol, String accountId, SellLowStrategy strategy) {
        this(symbol, accountId);

        sellLowStrategy = strategy;
        benchmark = strategy.benchmark;
    }

    public ExchangeService(String symbol, String accountId, double benchmark) {
        this(symbol, accountId);

        this.benchmark = benchmark;
    }

    public ExchangeService(String symbol, String accountId) {
        this.symbol = symbol;
        this.accountId = accountId;

        usdtBalance = BigDecimal.ZERO;
        coinBalance = BigDecimal.ZERO;

        priceList = new ArrayList<>();

        buyOrderList = new ArrayList<>();
        sellOrderList = new ArrayList<>();
    }

    public void strategy() {
        if (5 > priceList.size()) {
            return;
        }

        if (null != sellHighStrategy) {
            this.trySellHighStrategy();
            return;
        }

        if (null != sellLowStrategy) {
            this.trySellLowStrategy();
            return;
        }
    }

    private void trySellHighStrategy() {
        double currPrice = priceList.get(priceList.size() - 1);
        int action = sellHighStrategy.estimate(currPrice);
        if (0 == action) {
            return;
        }

        boolean placeOrder = false;
        Order order = new Order();
        order.symbol = symbol;
        order.accountId = accountId;

        // increase more than 10%
        if (1 == action) {
            // try once
            if (!sellOrderList.isEmpty()) {
                return;
            }

            order.type = "sell-limit";
            order.amount = "500";
            order.price = Double.toString(currPrice);
            placeOrder = true;
        }

        // decrease more than 5%
        if (-1 == action) {
            // try once
            if (!buyOrderList.isEmpty()) {
                return;
            }

            order.type = "buy-limit";
            order.amount = "200";
            order.price = Double.toString(currPrice);
            placeOrder = true;
        }

        if (placeOrder) {
            String payload = com.alibaba.fastjson.JSON.toJSONString(order);
            dingtalkRobot.notify(payload);
            //this.placeOrder(order);
        }
    }

    public void trySellLowStrategy() {
        double currPrice = priceList.get(priceList.size() - 1);
        double prevPrice = priceList.get(priceList.size() - 5);

        int action = sellLowStrategy.estimate(currPrice, prevPrice);
        if (0 == action) {
            return;
        }

        boolean placeOrder = false;
        Order order = new Order();
        order.symbol = symbol;
        order.accountId = accountId;

        // decrease more than 1%
        if (-1 == action) {
            if (!sellOrderList.isEmpty()) {
                return;
            }

            order.type = "sell-limit";
            order.amount = "500";
            order.price = Double.toString(currPrice);
            placeOrder = true;
        }

        // increase more than 1%
        if (1 == action) {
           if (!buyOrderList.isEmpty()) {
               return;
           }

           order.type = "buy-limit";
           order.amount = "500";
           order.price = Double.toString(currPrice);
           placeOrder = true;
        }

        if (placeOrder) {
            String payload = com.alibaba.fastjson.JSON.toJSONString(order);
            dingtalkRobot.notify(payload);
            //this.placeOrder(order);
        }
    }

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
                usdtBalance = balance;
            } else {
                coinBalance = balance;
            }
        }
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

        if (PRICE_MAX_SIZE <= priceList.size()) {
            priceList.clear();
        }
        priceList.add(price);

        strategy();
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

    public double firstPrice() {
        if (priceList.isEmpty()) {
            return 0.0;
        } else {
            return priceList.get(0);
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

        if (order.type.startsWith("buy")) {
            buyOrderList.add(orderId);
        } else {
            sellOrderList.add(orderId);
        }
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
