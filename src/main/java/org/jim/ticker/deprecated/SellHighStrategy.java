package org.jim.ticker.deprecated;

public class SellHighStrategy {

    public double benchmark;

    private double increase;
    private double decrease;

    public SellHighStrategy(double benchmark, double increase, double decrease) {
        this.benchmark = benchmark;
        this.increase = increase;
        this.decrease = decrease;
    }

    public int estimate(double price) {
        double change = (price - benchmark) / benchmark;
        if (increase < change) {
            return 1;
        }
        if (decrease > change) {
            return -1;
        }
        return 0;
    }

}
