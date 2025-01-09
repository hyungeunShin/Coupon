package com.example.couponcore.service;

import com.example.couponcore.TestConfig;
import com.example.couponcore.entity.Coupon;
import com.example.couponcore.entity.CouponIssue;
import com.example.couponcore.entity.CouponType;
import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.example.couponcore.repository.mysql.CouponIssueRepository;
import com.example.couponcore.repository.mysql.CouponJpaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

class CouponIssueServiceTest extends TestConfig {
    @Autowired
    CouponIssueService service;

    @Autowired
    CouponJpaRepository couponJpaRepository;

    @Autowired
    CouponIssueJpaRepository couponIssueJpaRepository;

    @Autowired
    CouponIssueRepository couponIssueRepository;

//    @BeforeEach
//    void beforeEach() {
//        couponJpaRepository.deleteAllInBatch();
//        couponIssueJpaRepository.deleteAllInBatch();
//    }

    @Test
    @DisplayName("쿠폰 발급 내역이 존재하면 예외 반환")
    void test1() {
        //given
        CouponIssue couponIssue = CouponIssue.builder()
                                             .couponId(1L)
                                             .userId(1L)
                                             .build();

        couponIssueJpaRepository.save(couponIssue);

        //when & then
        CouponIssueException e = Assertions.assertThrows(CouponIssueException.class, () -> service.saveCouponIssue(couponIssue.getCouponId(), couponIssue.getUserId()));
        Assertions.assertEquals(e.getErrorCode(), ErrorCode.DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("쿠폰 발급 내역이 존재하지 않으면 쿠폰 발급")
    void test2() {
        //given
        long couponId = 1L;
        long userId = 1L;

        //when
        CouponIssue issue = service.saveCouponIssue(couponId, userId);

        //then
        Assertions.assertTrue(couponIssueJpaRepository.findById(issue.getId()).isPresent());
    }

    @Test
    @DisplayName("발급 수량, 기한, 중복 발급 문제 X")
    void test3() {
        //given
        long userId = 1L;

        Coupon coupon = Coupon.builder()
                              .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                              .title("선착순 쿠폰")
                              .totalQuantity(100)
                              .issuedQuantity(0)
                              .dateIssueStart(LocalDateTime.now().minusDays(1))
                              .dateIssueEnd(LocalDateTime.now().plusDays(1))
                              .build();

        couponJpaRepository.save(coupon);

        //when
        service.issue(coupon.getId(), userId);

        //then
        Coupon result = couponJpaRepository.findById(coupon.getId()).get();
        Assertions.assertEquals(result.getIssuedQuantity(), 1);

        CouponIssue issue = couponIssueRepository.findFirstCouponIssue(coupon.getId(), userId);
        Assertions.assertNotNull(issue);
    }

    @Test
    @DisplayName("발급 수량, 기한, 중복 발급 문제 O")
    void test4() {
        //given
        long userId = 1L;

        Coupon coupon = Coupon.builder()
                              .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                              .title("선착순 쿠폰")
                              .totalQuantity(100)
                              .issuedQuantity(100)
                              .dateIssueStart(LocalDateTime.now().minusDays(1))
                              .dateIssueEnd(LocalDateTime.now().plusDays(1))
                              .build();

        couponJpaRepository.save(coupon);

        //when & then
        CouponIssueException e = Assertions.assertThrows(CouponIssueException.class, () -> service.issue(coupon.getId(), userId));
        Assertions.assertEquals(e.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @Test
    @DisplayName("발급 수량, 기한, 중복 발급 문제 O")
    void test5() {
        //given
        long userId = 1L;

        Coupon coupon = Coupon.builder()
                              .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                              .title("선착순 쿠폰")
                              .totalQuantity(100)
                              .issuedQuantity(0)
                              .dateIssueStart(LocalDateTime.now().minusDays(2))
                              .dateIssueEnd(LocalDateTime.now().minusDays(1))
                              .build();

        couponJpaRepository.save(coupon);

        //when & then
        CouponIssueException e = Assertions.assertThrows(CouponIssueException.class, () -> service.issue(coupon.getId(), userId));
        Assertions.assertEquals(e.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }

    @Test
    @DisplayName("발급 수량, 기한, 중복 발급 문제 O")
    void test6() {
        //given
        long userId = 1L;

        Coupon coupon = Coupon.builder()
                              .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                              .title("선착순 쿠폰")
                              .totalQuantity(100)
                              .issuedQuantity(0)
                              .dateIssueStart(LocalDateTime.now().minusDays(2))
                              .dateIssueEnd(LocalDateTime.now().plusDays(1))
                              .build();

        couponJpaRepository.save(coupon);

        CouponIssue couponIssue = CouponIssue.builder()
                                             .couponId(coupon.getId())
                                             .userId(userId)
                                             .build();

        couponIssueJpaRepository.save(couponIssue);

        //when & then
        CouponIssueException e = Assertions.assertThrows(CouponIssueException.class, () -> service.issue(coupon.getId(), userId));
        Assertions.assertEquals(e.getErrorCode(), ErrorCode.DUPLICATED_COUPON_ISSUE);
    }

    @Test
    @DisplayName("쿠폰 존재 X")
    void test7() {
        //given
        long userId = 1L;
        long couponId = 1L;

        //when & then
        CouponIssueException e = Assertions.assertThrows(CouponIssueException.class, () -> service.issue(couponId, userId));
        Assertions.assertEquals(e.getErrorCode(), ErrorCode.COUPON_NOT_EXIST);
    }
}