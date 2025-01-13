package com.example.couponcore.service;

import com.example.couponcore.entity.Coupon;
import com.example.couponcore.entity.CouponIssue;
import com.example.couponcore.event.CouponIssueCompleteEvent;
import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.repository.mysql.CouponIssueJpaRepository;
import com.example.couponcore.repository.mysql.CouponIssueRepository;
import com.example.couponcore.repository.mysql.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.couponcore.entity.QCoupon.coupon;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponIssueService {
    private final CouponJpaRepository couponJpaRepository;

    private final CouponIssueJpaRepository couponIssueJpaRepository;

    private final CouponIssueRepository couponIssueRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void issue(Long couponId, Long userId) {
//        Coupon coupon = findCoupon(couponId);
//        coupon.issue();
//        saveCouponIssue(couponId, userId);

        /*
        트랜잭션 시작

        lock 획득
        synchronized 블록
        lock 반납

        (여기가 문제) - 1번 요청이 lock을 반납하고 커밋하기 전에 2번 요청이 락을 획득하고 진입

        트랜잭션 종료

        제일 쉬운 방법은 트랜잭션 시작 전에 lock부터 획득 - CouponIssueRequestService에 synchronized 설정

        synchronized(this) {
            Coupon coupon = findCoupon(couponId);
            coupon.issue();
            saveCouponIssue(couponId, userId);
        }
        */

//        Coupon coupon = findCouponWithLock(couponId);
        Coupon coupon = findCoupon(couponId);
        coupon.issue();
        saveCouponIssue(couponId, userId);
        publishCouponEvent(coupon);
    }

    public Coupon findCoupon(Long couponId) {
        return couponJpaRepository.findById(couponId).orElseThrow(() -> new CouponIssueException(ErrorCode.COUPON_NOT_EXIST, "쿠폰이 존재하지 않습니다."));
    }

    public Coupon findCouponWithLock(Long couponId) {
        return couponJpaRepository.findCouponWithLock(couponId).orElseThrow(() -> new CouponIssueException(ErrorCode.COUPON_NOT_EXIST, "쿠폰이 존재하지 않습니다."));
    }

    @Transactional
    public CouponIssue saveCouponIssue(Long couponId, Long userId) {
        checkAlreadyIssue(couponId, userId);

        return couponIssueJpaRepository.save(CouponIssue.builder()
                                                        .couponId(couponId)
                                                        .userId(userId)
                                                        .build());
    }

    private void checkAlreadyIssue(Long couponId, Long userId) {
        CouponIssue issue = couponIssueRepository.findFirstCouponIssue(couponId, userId);
        if(issue != null) {
            throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "이미 발급된 쿠폰입니다.");
        }
    }

    private void publishCouponEvent(Coupon coupon) {
        if(coupon.isIssueComplete()) {
            eventPublisher.publishEvent(new CouponIssueCompleteEvent(coupon.getId()));
        }
    }
}
