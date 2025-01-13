package com.example.couponapi.service;

import com.example.couponapi.controller.dto.CouponIssueRequestDTO;
import com.example.couponcore.component.DistributeLockExecutor;
import com.example.couponcore.service.AsyncCouponIssueServiceV1;
import com.example.couponcore.service.AsyncCouponIssueServiceV2;
import com.example.couponcore.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueRequestService {
    private final CouponIssueService couponIssueService;

    private final DistributeLockExecutor executor;

    private final AsyncCouponIssueServiceV1 asyncCouponIssueServiceV1;

    private final AsyncCouponIssueServiceV2 asyncCouponIssueServiceV2;

    public void issueRequestV1(CouponIssueRequestDTO dto) {
        //동시성 문제 발생
//        couponIssueService.issue(dto.couponId(), dto.userId());
//        log.info("쿠폰 발급 완료 couponId : {}, userId : {}", dto.couponId(), dto.userId());

        //1. synchronized - 자바 어플리케이션 종속이라 서버가 확장되면 lock 관리가 불가
//        synchronized(this) {
//            couponIssueService.issue(dto.couponId(), dto.userId());
//            log.info("쿠폰 발급 완료 couponId : {}, userId : {}", dto.couponId(), dto.userId());
//        }

        //2. Redisson lock
//        String lockName = "lock_" + dto.couponId();
//        executor.execute(lockName, 10000, 10000, () -> couponIssueService.issue(dto.couponId(), dto.userId()));

        //3. mysql lock
        couponIssueService.issue(dto.couponId(), dto.userId());
        log.info("쿠폰 발급 완료 couponId : {}, userId : {}", dto.couponId(), dto.userId());
    }

    public void asyncIssueRequestV1(CouponIssueRequestDTO dto) {
        asyncCouponIssueServiceV1.issue(dto.couponId(), dto.userId());
        log.info("쿠폰 발급 완료 couponId : {}, userId : {}", dto.couponId(), dto.userId());
    }

    public void asyncIssueRequestV2(CouponIssueRequestDTO dto) {
        asyncCouponIssueServiceV2.issue(dto.couponId(), dto.userId());
        log.info("쿠폰 발급 완료 couponId : {}, userId : {}", dto.couponId(), dto.userId());
    }
}
