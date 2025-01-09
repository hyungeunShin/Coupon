package com.example.couponcore.repository.mysql;

import com.example.couponcore.entity.CouponIssue;
import com.example.couponcore.entity.QCouponIssue;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.example.couponcore.entity.QCouponIssue.couponIssue;

@Repository
@RequiredArgsConstructor
public class CouponIssueRepository {
    private final JPQLQueryFactory queryFactory;

    public CouponIssue findFirstCouponIssue(Long couponId, Long userId) {
        return queryFactory.selectFrom(couponIssue)
                           .where(couponIssue.couponId.eq(couponId))
                           .where(couponIssue.userId.eq(userId))
                           .fetchFirst();
    }
}
