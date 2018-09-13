package org.jim.ticker.service;

import org.jim.ticker.dingtalk.DingtalkRobot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DingtalkRobotTest {

    @Autowired
    private DingtalkRobot dingtalkRobot;

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
    public void testNotify() {
        dingtalkRobot.notify("Test");
    }

}
