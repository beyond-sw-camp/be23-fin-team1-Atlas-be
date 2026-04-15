package com.ozz.atlas.supply.batch.repository;

import com.ozz.atlas.supply.batch.domain.LotExpiryAggregation;
import com.ozz.atlas.supply.batch.domain.LotExpiryBucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface LotExpiryAggregationRepository extends JpaRepository<LotExpiryAggregation, Long> {

    // 해당 집계 row가 이미 있는지
    Optional<LotExpiryAggregation> findByAggregationDateAndSupplierPublicIdAndItemPublicIdAndBucket(
            LocalDate aggregationDate,
            String supplierPublicId,
            String itemPublicId,
            LotExpiryBucket bucket
    );

    // 특정 날짜 집계 결과를 통째로 삭제
    @Modifying
    @Query("delete from LotExpiryAggregation a where a.aggregationDate = :aggregationDate")
    void deleteByAggregationDate(@Param("aggregationDate") LocalDate aggregationDate);

}
