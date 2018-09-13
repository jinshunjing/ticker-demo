package org.jim.ticker.service;

import org.jim.ticker.deprecated.ExchangeService;
import org.jim.ticker.model.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ExchangeServiceTest {

    @Autowired
    private ExchangeService ontService;

    @Autowired
    private ExchangeService eosService;

    @Before
    public void setUp() throws Exception {
        Thread.sleep(5000L);
        System.out.println("");
        System.out.println("=====================================");
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("=====================================");
        System.out.println("");
        Thread.sleep(5000L);
    }

    @Test
    public void testPrice() {
        ontService.price();
        eosService.price();
    }

    @Test
    public void testBalance() {
        ontService.balance();
    }

    @Test
    public void testPlaceOrder() {
        Order order = new Order();
        order.accountId = "4247636";
        order.symbol = "ontusdt";
        order.type = "sell-limit";//buy-limit
        order.amount = "1.0";
        order.price = "2.0";

        ontService.placeOrder(order);
    }

    @Test
    public void testCancelOrder() {
        String orderId = "12270447724";
        ontService.cancelOrder(orderId);
    }

}
