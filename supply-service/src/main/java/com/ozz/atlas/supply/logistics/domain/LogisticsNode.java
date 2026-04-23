package com.ozz.atlas.supply.logistics.domain;

import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
public class LogisticsNode extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 26)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @Column(nullable = false, length = 26)
    private String organizationPublicId;

    @Column(nullable = false, length = 50)
    private String nodeCode;

    @Column(nullable = false, length = 100)
    private String nodeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LogisticsNodeType nodeType;

    @Column(length = 255)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, columnDefinition = "varchar(30) default 'EMPTY'")
    @Builder.Default
    private LogisticsNodeCapacityStatus capacityStatus = LogisticsNodeCapacityStatus.EMPTY;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @PrePersist
    public void prePersist(){
        if (this.publicId == null || this.publicId.isBlank()){
            this.publicId = PublicIdGenerator.next();
        }
    }

    public void update(
            String nodeName,
            LogisticsNodeType nodeType,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            LogisticsNodeCapacityStatus capacityStatus
    ){
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacityStatus = capacityStatus;
    }

    public void activate(){
        this.active = true;
    }

    public void deactivate(){
        this.active = false;
    }
}
