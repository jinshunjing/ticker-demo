package org.jim.ticker.deprecated;

public class SellLowStrategy {

    public double benchmark;

    private double increase;
    private double decrease;

    public SellLowStrategy(double benchmark, double increase, double decrease) {
        this.benchmark = benchmark;
        this.increase = increase;
        this.decrease = decrease;
    }

    public int estimate(double currPrice, double prevPrice) {
        double change = (currPrice - prevPrice) / prevPrice;
        if (increase < change) {
            return 1;
        }
        if (decrease > change) {
            return -1;
        }
        return 0;
    }

}
