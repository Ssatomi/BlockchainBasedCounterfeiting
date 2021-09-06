package com.dce.blockchain;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
class DceBlockchainApplicationTests {

	@Before
    public void init() {
        System.out.println("开始单元测试-----------------");
    }
 
    @After
    public void after() {
        System.out.println("单元测试结束-----------------");
    }

}
