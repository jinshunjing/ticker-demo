package org.jim.ticker.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.jim.ticker.dingtalk.DingtalkRobot;
import org.jim.ticker.service.AbstractStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class MarketJob {

    @Autowired
    private AbstractStrategy btcStrategy;

    @Autowired
    private AbstractStrategy ontStrategy;

    @Autowired
    private AbstractStrategy eosStrategy;

    @Autowired
    private DingtalkRobot dingtalkRobot;

    public static final int SKIP_ROUND = 5 * 4;
    private int skip = 0;

    @Scheduled(fixedDelay = 1000 * 15)
    public void price() {
        log.debug("price job");

        ontStrategy.price();
        eosStrategy.price();
        btcStrategy.price();

        if (0 < skip) {
            skip--;
            return;
        }

        double btcPrice = btcStrategy.currentPrice();
        double btcChange = btcStrategy.priceChange();
        double ontPrice = ontStrategy.currentPrice();
        double ontChange = ontStrategy.priceChange();
        double eosPrice = eosStrategy.currentPrice();
        double eosChange = eosStrategy.priceChange();

        StringBuilder sb = new StringBuilder();
        sb.append(new Date().toString()).append("\n");
        sb.append("BTC: ").append(btcPrice).append(", ").append(btcChange).append("\n");
        sb.append("ONT: ").append(ontPrice).append(", ").append(ontChange).append("\n");
        sb.append("EOS: ").append(eosPrice).append(", ").append(eosChange);
        dingtalkRobot.notify(sb.toString());

        skip = SKIP_ROUND;
    }

    @Scheduled(fixedDelay = 1000 * 61 * 30)
    public void balance() {
        log.debug("balance job");

        ontStrategy.balance();
        eosStrategy.balance();

        StringBuilder sb = new StringBuilder();
        sb.append(new Date().toString()).append("\n");
        sb.append("ONT: ").append(ontStrategy.getCoinBalance()).append(", ").append(ontStrategy.getUsdtBalance()).append("\n");
        sb.append("EOS: ").append(eosStrategy.getCoinBalance()).append(", ").append(eosStrategy.getUsdtBalance());
        dingtalkRobot.notify(sb.toString());
    }

}
