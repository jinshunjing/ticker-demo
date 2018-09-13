package org.jim.ticker.service;

import lombok.extern.slf4j.Slf4j;
import org.jim.ticker.model.Order;

@Slf4j
public class SellStrategy extends AbstractStrategy {

    private double minimum;
    private double profit;

    private int window;
    private double change;

    public SellStrategy(String symbol, String accountId, double benchmark,
                        double increase, double decrease,
                        int window, double change) {
        super(symbol, accountId, benchmark);

        this.minimum = benchmark * (1.0 - decrease);
        this.profit = benchmark * (1 + increase);

        this.window = window;
        this.change = change;
    }

    @Override
    public void strategy(double currPrice) {
        int action = this.estimate(currPrice);
        if (0 == action) {
            return;
        }

        Order order = new Order();
        order.symbol = symbol;
        order.accountId = accountId;
        order.type = "sell-limit";
        order.amount = coinBalance.toString();
        order.price = Double.toString(currPrice);

        String payload = com.alibaba.fastjson.JSON.toJSONString(order);
        dingtalkRobot.notify(payload);
        this.placeOrder(order);
    }

    public int estimate(double currPrice) {
        int size = priceList.size();
        if (window >= size) {
            return 0;
        }

        // SELL if price below minimum
        if (currPrice <= minimum) {
            return 1;
        }

        double high = 0.0;
        for (int i = 1; i <= window; i++) {
            double p = priceList.get(size - i);
            if (p > high) {
                high = p;
            }
        }

        // do not sell if there is no profit
        if (high < profit) {
            return 0;
        }

        // SELL if price decreased too much within N minutes
        double ratio = (high - currPrice) / high;
        if (ratio >= change) {
            return 1;
        }

        return 0;
    }

}
