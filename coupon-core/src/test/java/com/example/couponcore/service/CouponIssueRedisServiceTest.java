package com.example.couponcore.service;

import com.example.couponcore.TestConfig;
import com.example.couponcore.util.CouponRedisUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.stream.IntStream;

class CouponIssueRedisServiceTest extends TestConfig {
    @Autowired
    CouponIssueRedisService service;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void beforeEach() {
        Collection<String> redisKeys = redisTemplate.keys("*");
        redisTemplate.delete(redisKeys);
    }

    @Test
    @DisplayName("발급 가능 수량 존재")
    void test1() {
        //given
        int totalIssueQuantity = 10;
        long couponId = 1;

        //when
        Boolean result = service.availableTotalIssueQuantity(totalIssueQuantity, couponId);

        //then
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("발급 가능 수량 존재 X")
    void test2() {
        //given
        int totalIssueQuantity = 10;
        long couponId = 1;
        IntStream.range(0, totalIssueQuantity).forEach(userId -> redisTemplate.opsForSet().add(CouponRedisUtil.getIssueRequestKey(couponId), String.valueOf(userId)));

        //when
        Boolean result = service.availableTotalIssueQuantity(totalIssueQuantity, couponId);

        //then
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("쿠폰 중복 발급 검증")
    void test3() {
        //given
        long couponId = 1;
        long userId = 1;

        //when
        Boolean result = service.availableUserIssueQuantity(couponId, userId);

        //then
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("쿠폰 중복 발급 검증 X")
    void test4() {
        //given
        long couponId = 1;
        long userId = 1;
        redisTemplate.opsForSet().add(CouponRedisUtil.getIssueRequestKey(couponId), String.valueOf(userId));

        //when
        Boolean result = service.availableUserIssueQuantity(couponId, userId);

        //then
        Assertions.assertFalse(result);
    }
}