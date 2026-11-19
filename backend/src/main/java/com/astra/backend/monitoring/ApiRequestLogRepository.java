
package com.astra.backend.monitoring;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApiRequestLogRepository
        extends JpaRepository<ApiRequestLog, Long> {

    @Query("""
        SELECT l FROM ApiRequestLog l
        WHERE (:source IS NULL OR l.source = :source)
          AND (:status IS NULL OR l.statusCode = :status)
          AND (
              :search IS NULL
              OR LOWER(l.endpoint) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(l.httpMethod) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(l.errorMessage) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(l.requestId) LIKE LOWER(CONCAT('%', :search, '%'))
          )
          AND (:fromDate IS NULL OR l.requestTimestamp >= :fromDate)
          AND (:toDate IS NULL OR l.requestTimestamp <= :toDate)
        ORDER BY l.requestTimestamp DESC
        """)
    Page<ApiRequestLog> searchLogs(
            @Param("source") String source,
            @Param("status") Integer status,
            @Param("search") String search,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(l) FROM ApiRequestLog l
        WHERE l.requestTimestamp >= :fromDate
        """)
    long countSince(@Param("fromDate") LocalDateTime fromDate);

    @Query("""
        SELECT COUNT(l) FROM ApiRequestLog l
        WHERE l.requestTimestamp >= :fromDate
          AND l.success = true
        """)
    long countSuccessfulSince(@Param("fromDate") LocalDateTime fromDate);

    @Query("""
        SELECT COUNT(l) FROM ApiRequestLog l
        WHERE l.requestTimestamp >= :fromDate
          AND l.success = false
        """)
    long countFailedSince(@Param("fromDate") LocalDateTime fromDate);

    @Query("""
        SELECT COALESCE(AVG(l.durationMs), 0)
        FROM ApiRequestLog l
        WHERE l.requestTimestamp >= :fromDate
        """)
    Double averageDurationSince(
            @Param("fromDate") LocalDateTime fromDate
    );

    long countBySourceAndRequestTimestampGreaterThanEqual(
            String source,
            LocalDateTime fromDate
    );
}
