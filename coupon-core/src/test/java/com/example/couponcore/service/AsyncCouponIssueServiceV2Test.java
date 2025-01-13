package com.example.couponcore.service;

import com.example.couponcore.TestConfig;
import com.example.couponcore.dto.CouponIssueRequest;
import com.example.couponcore.entity.Coupon;
import com.example.couponcore.entity.CouponType;
import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.repository.mysql.CouponJpaRepository;
import com.example.couponcore.util.CouponRedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.IntStream;

class AsyncCouponIssueServiceV2Test extends TestConfig {
    @Autowired
    AsyncCouponIssueServiceV2 service;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    CouponJpaRepository couponJpaRepository;

    @BeforeEach
    void beforeEach() {
        Collection<String> redisKeys = redisTemplate.keys("*");
        redisTemplate.delete(redisKeys);
    }

    @Test
    @DisplayName("쿠폰 존재 X")
    void test1() {
        //given
        long couponId = 1;
        long userId = 1;

        //when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> service.issue(couponId, userId));
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.COUPON_NOT_EXIST);
    }

    @Test
    @DisplayName("쿠폰 발급 수량 X")
    void test2() {
        //given
        long userId = 100;
        Coupon coupon = Coupon.builder()
                              .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                              .title("선착순 쿠폰")
                              .totalQuantity(10)
                              .issuedQuantity(0)
                              .dateIssueStart(LocalDateTime.now().minusDays(1))
                              .dateIssueEnd(LocalDateTime.now().plusDays(1))
                              .build();

        couponJpaRepository.save(coupon);
        IntStream.range(0, coupon.getTotalQuantity()).forEach(i -> {
            redisTemplate.opsForSet().add(CouponRedisUtil.getIssueRequestKey(coupon.getId()), String.valueOf(i));
        });

        //when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> service.issue(coupon.getId(), userId));
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("중복 발급")
    void test3() {
        //given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                              .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                              .title("선착순 쿠폰")
                              .totalQuantity(10)
                              .issuedQuantity(0)
                              .dateIssueStart(LocalDateTime.now().minusDays(1))
                              .dateIssueEnd(LocalDateTime.now().plusDays(1))
                              .build();

        couponJpaRepository.save(coupon);
        redisTemplate.opsForSet().add(CouponRedisUtil.getIssueRequestKey(coupon.getId()), String.valueOf(userId));

        //when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> service.issue(coupon.getId(), userId));
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("발급 기한 문제")
    void test4() {
        //given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                              .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                              .title("선착순 쿠폰")
                              .totalQuantity(10)
                              .issuedQuantity(0)
                              .dateIssueStart(LocalDateTime.now().plusDays(1))
                              .dateIssueEnd(LocalDateTime.now().plusDays(2))
                              .build();

        couponJpaRepository.save(coupon);
        redisTemplate.opsForSet().add(CouponRedisUtil.getIssueRequestKey(coupon.getId()), String.valueOf(userId));

        //when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> service.issue(coupon.getId(), userId));
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }

    @Test
    @DisplayName("쿠폰 발급")
    void test5() {
        //given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                              .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                              .title("선착순 쿠폰")
                              .totalQuantity(10)
                              .issuedQuantity(0)
                              .dateIssueStart(LocalDateTime.now().minusDays(1))
                              .dateIssueEnd(LocalDateTime.now().plusDays(2))
                              .build();

        couponJpaRepository.save(coupon);

        //when
        service.issue(coupon.getId(), userId);

        //then
        Boolean result = redisTemplate.opsForSet().isMember(CouponRedisUtil.getIssueRequestKey(coupon.getId()), String.valueOf(userId));
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("쿠폰 발급 큐 검증")
    void test6() throws JsonProcessingException {
        //given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                              .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                              .title("선착순 쿠폰")
                              .totalQuantity(10)
                              .issuedQuantity(0)
                              .dateIssueStart(LocalDateTime.now().minusDays(1))
                              .dateIssueEnd(LocalDateTime.now().plusDays(2))
                              .build();

        couponJpaRepository.save(coupon);
        CouponIssueRequest request = new CouponIssueRequest(coupon.getId(), userId);

        //when
        service.issue(coupon.getId(), userId);

        //then
        String savedIssueRequest = redisTemplate.opsForList().leftPop(CouponRedisUtil.getIssueRequestQueueKey());
        Assertions.assertEquals(new ObjectMapper().writeValueAsString(request), savedIssueRequest);
    }
}