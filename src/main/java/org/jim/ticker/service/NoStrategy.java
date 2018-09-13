package org.jim.ticker.service;

public class NoStrategy extends AbstractStrategy {

    public NoStrategy(String symbol, String accountId, double benchmark) {
        super(symbol, accountId, benchmark);
    }

    @Override
    public void strategy(double price) {
    }

}
