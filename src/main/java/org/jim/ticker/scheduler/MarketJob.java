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
    private AbstractStrategy ethStrategy;

    @Autowired
    private AbstractStrategy ontStrategy;

    @Autowired
    private AbstractStrategy eosStrategy;

    @Autowired
    private DingtalkRobot dingtalkRobot;

    public static final int SKIP_PRICE = 5 * 4;
    private int skipPrice = 0;

    public static final int SKIP_BALANCE = 6;
    private int skipBalance = 0;

    @Scheduled(fixedRate = 1000 * 15)
    public void price() {
        log.debug("price job");

        btcStrategy.price();
        ethStrategy.price();
        eosStrategy.price();
        ontStrategy.price();

        if (0 < skipPrice) {
            skipPrice--;
            return;
        }

        double btcPrice = btcStrategy.currentPrice();
        double btcChange = btcStrategy.priceChange();
        double ethPrice = ethStrategy.currentPrice();
        double ethChange = ethStrategy.priceChange();
        double ontPrice = ontStrategy.currentPrice();
        double ontChange = ontStrategy.priceChange();
        double eosPrice = eosStrategy.currentPrice();
        double eosChange = eosStrategy.priceChange();

        StringBuilder sb = new StringBuilder();
        sb.append(new Date().toString()).append("\n");
        sb.append("BTC: ").append(btcPrice).append(", ").append(btcChange).append("\n");
        sb.append("ETH: ").append(ethPrice).append(", ").append(ethChange).append("\n");
        sb.append("EOS: ").append(eosPrice).append(", ").append(eosChange).append("\n");
        sb.append("ONT: ").append(ontPrice).append(", ").append(ontChange);
        dingtalkRobot.notify(sb.toString());

        skipPrice = SKIP_PRICE;
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    public void balance() {
        log.debug("balance job");

        ontStrategy.balance();
        eosStrategy.balance();

        if (0 < skipBalance) {
            skipBalance--;
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(new Date().toString()).append("\n");
        sb.append("EOS: ").append(eosStrategy.getCoinBalance()).append(", ").append(eosStrategy.getUsdtBalance()).append("\n");
        sb.append("ONT: ").append(ontStrategy.getCoinBalance()).append(", ").append(ontStrategy.getUsdtBalance());
        dingtalkRobot.notify(sb.toString());

        skipBalance = SKIP_BALANCE;
    }

}
