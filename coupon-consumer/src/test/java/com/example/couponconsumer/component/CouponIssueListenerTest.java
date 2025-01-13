package com.example.couponconsumer.component;

import com.example.couponconsumer.TestConfig;
import com.example.couponcore.repository.redis.RedisRepository;
import com.example.couponcore.service.CouponIssueService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collection;

@Import(CouponIssueListener.class)
class CouponIssueListenerTest extends TestConfig {
    @Autowired
    CouponIssueListener listener;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    RedisRepository redisRepository;

    @MockitoBean
    CouponIssueService couponIssueService;

    @BeforeEach
    void beforeEach() {
        Collection<String> redisKeys = redisTemplate.keys("*");
        redisTemplate.delete(redisKeys);
    }

    @Test
    @DisplayName("쿠폰 발급 큐에 처리 대상이 없다면 발급 X")
    void test1() throws JsonProcessingException {
        //when
        listener.issue();

        //then
        Mockito.verify(couponIssueService, Mockito.never()).issue(ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("쿠폰 발급 큐에 처리 대상이 있다면 발급")
    void test2() throws JsonProcessingException {
        //given
        long couponId = 1;
        long userId = 1;
        int totalQuantity = Integer.MAX_VALUE;
        redisRepository.issueRequest(couponId, userId, totalQuantity);

        //when
        listener.issue();

        //then
        Mockito.verify(couponIssueService, Mockito.times(1)).issue(couponId, userId);
    }

    @Test
    @DisplayName("쿠폰 발급 요청 순서에 맞게 처리")
    void test3() throws JsonProcessingException {
        //given
        long couponId = 1;
        long userId1 = 1;
        long userId2 = 2;
        long userId3 = 3;
        int totalQuantity = Integer.MAX_VALUE;
        redisRepository.issueRequest(couponId, userId1, totalQuantity);
        redisRepository.issueRequest(couponId, userId2, totalQuantity);
        redisRepository.issueRequest(couponId, userId3, totalQuantity);

        //when
        listener.issue();

        //then
        InOrder inOrder = Mockito.inOrder(couponIssueService);
        inOrder.verify(couponIssueService, Mockito.times(1)).issue(couponId, userId1);
        inOrder.verify(couponIssueService, Mockito.times(1)).issue(couponId, userId2);
        inOrder.verify(couponIssueService, Mockito.times(1)).issue(couponId, userId3);
    }
}