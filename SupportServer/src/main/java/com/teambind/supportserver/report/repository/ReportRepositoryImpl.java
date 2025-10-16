package com.teambind.supportserver.report.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teambind.supportserver.report.dto.request.ReportSearchRequest;
import com.teambind.supportserver.report.entity.Report;
import com.teambind.supportserver.report.entity.enums.ReferenceType;
import com.teambind.supportserver.report.entity.enums.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.teambind.supportserver.report.entity.QReport.report;

/**
 * 신고 커스텀 리포지토리 구현체
 */
@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Report> findReportsWithCursor(ReportSearchRequest searchRequest) {
        // 동적 where 절 구성
        BooleanExpression predicate = buildPredicate(searchRequest);

        // 정렬 기준 구성
        OrderSpecifier<?>[] orderSpecifiers = buildOrderSpecifiers(searchRequest);

        // 쿼리 실행 (size + 1개를 조회하여 다음 페이지 존재 여부 확인)
        return queryFactory
                .selectFrom(report)
                .where(predicate)
                .orderBy(orderSpecifiers)
                .limit(searchRequest.getSize() + 1)
                .fetch();
    }

    @Override
    public List<Report> findByReporterId(String reporterId) {
        return queryFactory
                .selectFrom(report)
                .where(report.reporterId.eq(reporterId))
                .orderBy(report.reportedAt.desc())
                .fetch();
    }

    @Override
    public List<Report> findByReportedId(String reportedId) {
        return queryFactory
                .selectFrom(report)
                .where(report.reportedId.eq(reportedId))
                .orderBy(report.reportedAt.desc())
                .fetch();
    }

    /**
     * 검색 조건에 따른 Where 절 구성
     */
    private BooleanExpression buildPredicate(ReportSearchRequest searchRequest) {
        BooleanExpression predicate = null;

        // 상태 필터
        if (searchRequest.getStatus() != null) {
            predicate = addAnd(predicate, statusEq(searchRequest.getStatus()));
        }

        // 대상 타입 필터
        if (searchRequest.getReferenceType() != null) {
            predicate = addAnd(predicate, referenceTypeEq(searchRequest.getReferenceType()));
        }

        // 신고 카테고리 필터
        if (searchRequest.getReportCategory() != null && !searchRequest.getReportCategory().isBlank()) {
            predicate = addAnd(predicate, reportCategoryEq(searchRequest.getReportCategory()));
        }

        // 커서 조건 (페이징)
        if (searchRequest.getCursor() != null && !searchRequest.getCursor().isBlank()) {
            predicate = addAnd(predicate, buildCursorCondition(searchRequest));
        }

        return predicate;
    }

    /**
     * 커서 조건 구성
     */
    private BooleanExpression buildCursorCondition(ReportSearchRequest searchRequest) {
        String cursor = searchRequest.getCursor();
        ReportSearchRequest.SortType sortType = searchRequest.getSortType();
        ReportSearchRequest.SortDirection direction = searchRequest.getSortDirection();

        try {
            if (sortType == ReportSearchRequest.SortType.REPORTED_AT) {
                // 신고일 기준 커서
                LocalDateTime cursorDateTime = LocalDateTime.parse(cursor);
                return direction == ReportSearchRequest.SortDirection.DESC
                        ? report.reportedAt.lt(cursorDateTime)
                        : report.reportedAt.gt(cursorDateTime);
            } else if (sortType == ReportSearchRequest.SortType.STATUS) {
                // 상태 기준 커서 (reportId를 보조 키로 사용)
                return direction == ReportSearchRequest.SortDirection.DESC
                        ? report.reportId.lt(cursor)
                        : report.reportId.gt(cursor);
            }
        } catch (Exception e) {
            // 커서 파싱 실패 시 무시
            return null;
        }

        return null;
    }

    /**
     * 정렬 조건 구성
     */
    private OrderSpecifier<?>[] buildOrderSpecifiers(ReportSearchRequest searchRequest) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        ReportSearchRequest.SortType sortType = searchRequest.getSortType();
        ReportSearchRequest.SortDirection direction = searchRequest.getSortDirection();

        if (sortType == ReportSearchRequest.SortType.STATUS) {
            // 상태 기준 정렬
            orders.add(direction == ReportSearchRequest.SortDirection.DESC
                    ? report.status.desc()
                    : report.status.asc());
            // 보조 정렬 (reportId)
            orders.add(direction == ReportSearchRequest.SortDirection.DESC
                    ? report.reportId.desc()
                    : report.reportId.asc());
        } else {
            // 신고일 기준 정렬 (기본값)
            orders.add(direction == ReportSearchRequest.SortDirection.DESC
                    ? report.reportedAt.desc()
                    : report.reportedAt.asc());
            // 보조 정렬 (reportId)
            orders.add(direction == ReportSearchRequest.SortDirection.DESC
                    ? report.reportId.desc()
                    : report.reportId.asc());
        }

        return orders.toArray(new OrderSpecifier[0]);
    }

    /**
     * BooleanExpression AND 연결
     */
    private BooleanExpression addAnd(BooleanExpression predicate, BooleanExpression condition) {
        if (condition == null) {
            return predicate;
        }
        return predicate == null ? condition : predicate.and(condition);
    }

    /**
     * 상태 조건
     */
    private BooleanExpression statusEq(ReportStatus status) {
        return status != null ? report.status.eq(status) : null;
    }

    /**
     * 대상 타입 조건
     */
    private BooleanExpression referenceTypeEq(ReferenceType referenceType) {
        return referenceType != null ? report.referenceType.eq(referenceType) : null;
    }

    /**
     * 신고 카테고리 조건
     */
    private BooleanExpression reportCategoryEq(String reportCategory) {
        return reportCategory != null && !reportCategory.isBlank()
                ? report.reportCategory.eq(reportCategory)
                : null;
    }
}
