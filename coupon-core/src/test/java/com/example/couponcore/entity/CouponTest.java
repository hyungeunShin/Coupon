package com.example.couponcore.entity;

import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class CouponTest {
    @Test
    @DisplayName("최대 수량 > 발급 수량")
    void test1() {
        //given
        Coupon coupon = Coupon.builder()
                              .totalQuantity(100)
                              .issuedQuantity(99)
                              .build();

        //when
        boolean result = coupon.availableIssueQuantity();

        //then
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("최대 수량 == 발급 수량")
    void test2() {
        //given
        Coupon coupon = Coupon.builder()
                              .totalQuantity(100)
                              .issuedQuantity(100)
                              .build();

        //when
        boolean result = coupon.availableIssueQuantity();

        //then
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("최대 수량 = null")
    void test3() {
        //given
        Coupon coupon = Coupon.builder()
                              .totalQuantity(null)
                              .issuedQuantity(100)
                              .build();

        //when
        boolean result = coupon.availableIssueQuantity();

        //then
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("발급 기한 시작 X")
    void test4() {
        //given
        Coupon coupon = Coupon.builder()
                              .dateIssueStart(LocalDateTime.now().plusDays(1))
                              .dateIssueEnd(LocalDateTime.now().plusDays(2))
                              .build();

        //when
        boolean result = coupon.availableIssueDate();

        //then
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("발급 기한 시작 O")
    void test5() {
        //given
        Coupon coupon = Coupon.builder()
                              .dateIssueStart(LocalDateTime.now().minusDays(1))
                              .dateIssueEnd(LocalDateTime.now().plusDays(2))
                              .build();

        //when
        boolean result = coupon.availableIssueDate();

        //then
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("발급 기한 종료")
    void test6() {
        //given
        Coupon coupon = Coupon.builder()
                              .dateIssueStart(LocalDateTime.now().minusDays(3))
                              .dateIssueEnd(LocalDateTime.now().minusDays(1))
                              .build();

        //when
        boolean result = coupon.availableIssueDate();

        //then
        Assertions.assertFalse(result);
    }

    @Test
    @DisplayName("발급 수량 만족, 발급 기한 만족")
    void test7() {
        //given
        Coupon coupon = Coupon.builder()
                              .totalQuantity(100)
                              .issuedQuantity(99)
                              .dateIssueStart(LocalDateTime.now().minusDays(3))
                              .dateIssueEnd(LocalDateTime.now().plusDays(2))
                              .build();

        //when
        coupon.issue();

        //then
        Assertions.assertEquals(coupon.getIssuedQuantity(), 100);
    }

    @Test
    @DisplayName("발급 수량 만족 X, 발급 기한 만족")
    void test8() {
        //given
        Coupon coupon = Coupon.builder()
                              .totalQuantity(100)
                              .issuedQuantity(100)
                              .dateIssueStart(LocalDateTime.now().minusDays(3))
                              .dateIssueEnd(LocalDateTime.now().plusDays(2))
                              .build();

        //when & then
        CouponIssueException e = Assertions.assertThrows(CouponIssueException.class, coupon::issue);
        Assertions.assertEquals(e.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("발급 수량 만족, 발급 기한 만족 X")
    void test9() {
        //given
        Coupon coupon = Coupon.builder()
                              .totalQuantity(100)
                              .issuedQuantity(99)
                              .dateIssueStart(LocalDateTime.now().minusDays(3))
                              .dateIssueEnd(LocalDateTime.now().minusDays(1))
                              .build();

        //when & then
        CouponIssueException e = Assertions.assertThrows(CouponIssueException.class, coupon::issue);
        Assertions.assertEquals(e.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }
}