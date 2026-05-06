package com.ozz.atlas.supply.settlement.repository;

import com.ozz.atlas.supply.settlement.domain.Settlement;
import com.ozz.atlas.supply.settlement.domain.SettlementStatus;
import com.ozz.atlas.supply.settlement.domain.SettlementTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

//    정산 중복 체크
    boolean existsByTargetTypeAndTargetPublicIdAndSettlementStatusNot(
            SettlementTargetType targetType,
            String targetPublicId,
            SettlementStatus settlementStatus
    );
    
    Optional<Settlement> findByTargetTypeAndTargetPublicIdAndSettlementStatusNot(
            SettlementTargetType targetType,
            String targetPublicId,
            SettlementStatus settlementStatus
    );

    Optional<Settlement> findByPublicId(String publicId);

    @Query("""
        select s
        from Settlement s
        where s.buyerOrganizationPublicId = :organizationPublicId
           or s.supplierOrganizationPublicId = :organizationPublicId
        """)
    Page<Settlement> findReadableByOrganizationPublicId(
            @Param("organizationPublicId") String organizationPublicId,
            Pageable pageable
    );

    // 로그인한 조직이 발주사 또는 협력사로 연결된 정산을 전체 조회
    // 엑셀 내보내기는 페이지 단위가 아니라 파일 하나로 내려가야 해서 List로 조회
    @Query("""
    select s
    from Settlement s
    where s.buyerOrganizationPublicId = :organizationPublicId
       or s.supplierOrganizationPublicId = :organizationPublicId
    order by s.id desc
""")
    List<Settlement> findAllReadableByOrganizationPublicId(
            @Param("organizationPublicId") String organizationPublicId
    );

    @Query("""
    select s
    from Settlement s
    where (
        s.buyerOrganizationPublicId = :organizationPublicId
        or s.supplierOrganizationPublicId = :organizationPublicId
    )
      and (:startDate is null or s.settlementPeriodStart >= :startDate)
      and (:endDate is null or s.settlementPeriodStart <= :endDate)
    order by s.id desc
""")
    List<Settlement> findAllReadableByOrganizationPublicIdAndPeriod(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );



    @Query("""
        select s
        from Settlement s
        where s.publicId = :publicId
          and (
              s.buyerOrganizationPublicId = :organizationPublicId
              or s.supplierOrganizationPublicId = :organizationPublicId
          )
        """)
    Optional<Settlement> findReadableByPublicId(
            @Param("publicId") String publicId,
            @Param("organizationPublicId") String organizationPublicId
    );
    // 연도별 정산 총액과 건수를 조회
    @Query("""
    select year(s.createdAt), coalesce(sum(s.amount), 0), count(s)
    from Settlement s
    where (
        s.buyerOrganizationPublicId = :organizationPublicId
        or s.supplierOrganizationPublicId = :organizationPublicId
    )
    group by year(s.createdAt)
    order by year(s.createdAt)
    """)
    List<Object[]> findYearlyAmountStats(
            @Param("organizationPublicId") String organizationPublicId
    );

    // 기준 연도의 월별 정산 총액과 건수를 조회
    @Query("""
    select month(s.createdAt), coalesce(sum(s.amount), 0), count(s)
    from Settlement s
    where year(s.createdAt) = :year
      and (
          s.buyerOrganizationPublicId = :organizationPublicId
          or s.supplierOrganizationPublicId = :organizationPublicId
      )
    group by month(s.createdAt)
    order by month(s.createdAt)
    """)
    List<Object[]> findMonthlyAmountStats(
            @Param("year") Integer year,
            @Param("organizationPublicId") String organizationPublicId
    );

    // 기준 연도의 상태별 정산 총액과 건수를 조회
    @Query("""
    select s.settlementStatus, coalesce(sum(s.amount), 0), count(s)
    from Settlement s
    where year(s.createdAt) = :year
      and (
          s.buyerOrganizationPublicId = :organizationPublicId
          or s.supplierOrganizationPublicId = :organizationPublicId
      )
    group by s.settlementStatus
    order by s.settlementStatus
    """)
    List<Object[]> findStatusAmountStats(
            @Param("year") Integer year,
            @Param("organizationPublicId") String organizationPublicId
    );

    // 기준 연도의 정산 대상 유형별 총액과 건수를 조회
    @Query("""
    select s.targetType, coalesce(sum(s.amount), 0), count(s)
    from Settlement s
    where year(s.createdAt) = :year
      and (
          s.buyerOrganizationPublicId = :organizationPublicId
          or s.supplierOrganizationPublicId = :organizationPublicId
      )
    group by s.targetType
    order by s.targetType
    """)
    List<Object[]> findTargetTypeAmountStats(
            @Param("year") Integer year,
            @Param("organizationPublicId") String organizationPublicId
    );

    // 특정 조직이 지급해야 하는 월별 정산액을 조회
    @Query("""
    select month(s.createdAt), coalesce(sum(s.amount), 0), count(s)
    from Settlement s
    where year(s.createdAt) = :year
      and s.buyerOrganizationPublicId = :organizationPublicId
    group by month(s.createdAt)
    order by month(s.createdAt)
    """)
    List<Object[]> findMonthlyPayableAmountStats(
            @Param("year") Integer year,
            @Param("organizationPublicId") String organizationPublicId
    );

    // 특정 조직이 받을 월별 정산액을 조회
    @Query("""
    select month(s.createdAt), coalesce(sum(s.amount), 0), count(s)
    from Settlement s
    where year(s.createdAt) = :year
      and s.supplierOrganizationPublicId = :organizationPublicId
    group by month(s.createdAt)
    order by month(s.createdAt)
    """)
    List<Object[]> findMonthlyReceivableAmountStats(
            @Param("year") Integer year,
            @Param("organizationPublicId") String organizationPublicId
    );

    @Query("""
        select count(s)
        from Settlement s
        where s.settlementStatus = :settlementStatus
          and (
              s.buyerOrganizationPublicId = :organizationPublicId
              or s.supplierOrganizationPublicId = :organizationPublicId
          )
    """)
    long countReadableByOrganizationPublicIdAndStatus(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("settlementStatus") SettlementStatus settlementStatus
    );

    @Query("""
        select s.publicId
        from Settlement s
        where s.settlementStatus = :settlementStatus
          and (
              s.buyerOrganizationPublicId = :organizationPublicId
              or s.supplierOrganizationPublicId = :organizationPublicId
          )
    """)
    List<String> findReadablePublicIdsByOrganizationPublicIdAndStatus(
            @Param("organizationPublicId") String organizationPublicId,
            @Param("settlementStatus") SettlementStatus settlementStatus
    );

}
