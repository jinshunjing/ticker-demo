package org.jim.ticker.service;

import lombok.extern.slf4j.Slf4j;
import org.jim.ticker.model.Order;

import java.math.BigDecimal;

@Slf4j
public class SellStrategy extends AbstractStrategy {

    private double minimum;
    private double profit;
    private double maximum;

    private int window;
    private double change;

    public SellStrategy(String symbol, String accountId, double benchmark,
                        double minimum, double profit, double maximum,
                        int window, double change) {
        super(symbol, accountId, benchmark);

        this.minimum = minimum;
        this.profit = profit;
        this.maximum = maximum;

        this.window = window;
        this.change = change;
    }

    @Override
    public void strategy(double currPrice) {
        // no coin
        if (coinBalance.doubleValue() < 10.0) {
            return;
        }

        String sellPrice = this.estimate(currPrice);
        if (null == sellPrice) {
            return;
        }

        this.sell(sellPrice);
    }

    public String estimate(double currPrice) {
        int size = priceList.size();
        if (window >= size) {
            return null;
        }

        // SELL if price below minimum
        if (currPrice <= minimum) {
            return Double.toString(minimum);
        }

        // SELL if price above maximum
        if (currPrice >= maximum) {
            return Double.toString(currPrice);
        }

        double high = 0.0;
        for (int i = 1; i <= window; i++) {
            double p = priceList.get(size - i);
            if (p > high) {
                high = p;
            }
        }

        /// do not sell if there is no profit
        if (high < profit) {
            return null;
        }

        // SELL if price decreased too much within N minutes
        double ratio = (high - currPrice) / high;
        if (ratio >= change) {
            return Double.toString(currPrice);
        }

        return null;
    }

    public void sell(String sellPrice) {
        // update balance
        super.balance();

        // place order
        Order order = new Order();
        order.symbol = symbol;
        order.accountId = accountId;
        order.type = "sell-limit";
        order.amount = coinBalance.toString();
        order.price = sellPrice;
        this.placeOrder(order);

        String payload = com.alibaba.fastjson.JSON.toJSONString(order);
        dingtalkRobot.notify(payload);

        // reset balance
        coinBalance = BigDecimal.ZERO;
    }

}
