package org.jim.ticker.config;

import org.jim.ticker.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public AbstractStrategy btcStrategy() {
        double benchmark = 6200;
        return new NoStrategy("btcusdt", "1231234", benchmark);
    }

    @Bean
    public AbstractStrategy ontStrategy() {
        double benchmark = 1.56;
        double increase = 0.18;
        double decrease = 0.0;

        int window = 5 * 4;
        double change = 0.02;

        return new SellStrategy("ontusdt", "4247636", benchmark, increase, decrease, window, change);
    }

    @Bean
    public AbstractStrategy eosStrategy() {
        double benchmark = 4.76;
        double increase = 0.05;
        double decrease = 0.0;

        int window = 5 * 4;
        double change = 0.01;
        return new SellStrategy("eosusdt", "3220457", benchmark, increase, decrease, window, change);
    }

}
