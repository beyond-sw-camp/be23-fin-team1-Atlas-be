package com.ozz.atlas.supply.batch.domain;

import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

// 협력사별 일별 납기 준수율 KPI 저장
@Getter
@Entity
@Table(
        name = "supplier_delivery_daily_kpi",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_supplier_delivery_daily_kpi_date_supplier",
                        columnNames = {"kpi_date", "supplier_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupplierDeliveryDailyKpi extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kpi_date", nullable = false)
    private LocalDate kpiDate; // 어떤 날짜 기준인지

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "supplier_public_id", nullable = false, length = 26)
    private String supplierPublicId;

    @Column(name = "supplier_code", nullable = false, length = 50)
    private String supplierCode;

    @Column(name = "supplier_name", nullable = false, length = 100)
    private String supplierName;

    @Column(name = "total_shipment_count", nullable = false)
    private Long totalShipmentCount; // 총 도착 완료 출하 건수

    @Column(name = "arrived_shipment_count", nullable = false)
    private Long arrivedShipmentCount; // 실제 도착 처리 건수

    @Column(name = "on_time_shipment_count", nullable = false)
    private Long onTimeShipmentCount; // 제시간에 도착한 건수

    @Column(name = "late_shipment_count", nullable = false)
    private Long lateShipmentCount; // 늦은 건수

    @Column(name = "total_delay_minutes", nullable = false)
    private Long totalDelayMinutes; // 늦은 건들의 지연 시간 전부 더한 값

    @Column(name = "on_time_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal onTimeRate; // 정시율

    @Column(name = "avg_delay_minutes", nullable = false, precision = 10, scale = 2)
    private BigDecimal avgDelayMinutes; // 평균 지연 시간

    public SupplierDeliveryDailyKpi(
            LocalDate kpiDate,
            Long supplierId,
            String supplierPublicId,
            String supplierCode,
            String supplierName
    ) {
        this.kpiDate = kpiDate;
        this.supplierId = supplierId;
        this.supplierPublicId = supplierPublicId;
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.totalShipmentCount = 0L;
        this.arrivedShipmentCount = 0L;
        this.onTimeShipmentCount = 0L;
        this.lateShipmentCount = 0L;
        this.totalDelayMinutes = 0L;
        this.onTimeRate = BigDecimal.ZERO;
        this.avgDelayMinutes = BigDecimal.ZERO;
    }

    public void accumulate(boolean onTime, long delayMinutes) {
        this.totalShipmentCount += 1;
        this.arrivedShipmentCount += 1;

        if (onTime) {
            this.onTimeShipmentCount += 1;
        } else {
            this.lateShipmentCount += 1;
            this.totalDelayMinutes += delayMinutes;
        }

        recalculate();
    }

    private void recalculate() {
        if (this.arrivedShipmentCount > 0) {
            this.onTimeRate = BigDecimal.valueOf(this.onTimeShipmentCount * 100.0)
                    .divide(BigDecimal.valueOf(this.arrivedShipmentCount), 2, RoundingMode.HALF_UP);
        } else {
            this.onTimeRate = BigDecimal.ZERO;
        }

        if (this.lateShipmentCount > 0) {
            this.avgDelayMinutes = BigDecimal.valueOf(this.totalDelayMinutes)
                    .divide(BigDecimal.valueOf(this.lateShipmentCount), 2, RoundingMode.HALF_UP);
        } else {
            this.avgDelayMinutes = BigDecimal.ZERO;
        }
    }
}
