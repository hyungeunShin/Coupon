package com.example.couponcore;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class MyTest extends TestConfig {
    @Test
    void test() {
        A a = new A();
        a.a();
    }

    static class A {
        private static final Logger log = LoggerFactory.getLogger(A.class);

        //@Transactional
        public void a() {
            log.info("a 호출");
            b();
            boolean r = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("a의 트랜잭션 {}", r);
        }

        @Transactional
        public void b() {
            log.info("b 호출");
            boolean r = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("b의 트랜잭션 {}", r);
        }
    }
}
