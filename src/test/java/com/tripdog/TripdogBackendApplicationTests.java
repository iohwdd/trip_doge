package com.tripdog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest

class TripdogBackendApplicationTests {

    @Test
    void contextLoads() {
        String s = "123456";
        System.out.println(s.lastIndexOf("7"));
    }

}
