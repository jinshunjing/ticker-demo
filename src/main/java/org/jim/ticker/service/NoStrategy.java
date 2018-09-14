package org.jim.ticker.service;

public class NoStrategy extends AbstractStrategy {

    /**
     * Skip N minutes
     */
    private int skipPrice = 0;
    private static int SKIP_PRICE = 4 * 1;

    public NoStrategy(String symbol, String accountId, double benchmark) {
        super(symbol, accountId, benchmark);
    }

    @Override
    public void price() {
        if (skipPrice > 0) {
            skipPrice--;
            return;
        }

        super.price();
        skipPrice = SKIP_PRICE;
    }

    @Override
    public void balance() {
    }

    @Override
    public void strategy(double price) {
    }

}
