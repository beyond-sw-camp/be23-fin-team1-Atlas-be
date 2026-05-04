package com.ozz.atlas.supply.sidebar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "supply_detail_view_states",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_supply_detail_view_state",
                columnNames = {"user_public_id", "organization_public_id", "menu_key", "detail_public_id"}
        )
)
public class SupplyDetailViewState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_public_id", nullable = false, length = 64)
    private String userPublicId;

    @Column(name = "organization_public_id", nullable = false, length = 64)
    private String organizationPublicId;

    @Column(name = "menu_key", nullable = false, length = 64)
    private String menuKey;

    @Column(name = "detail_public_id", nullable = false, length = 64)
    private String detailPublicId;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    public SupplyDetailViewState(
            String userPublicId,
            String organizationPublicId,
            String menuKey,
            String detailPublicId
    ) {
        this.userPublicId = userPublicId;
        this.organizationPublicId = organizationPublicId;
        this.menuKey = menuKey;
        this.detailPublicId = detailPublicId;
        this.viewedAt = LocalDateTime.now();
    }

    public void touch() {
        this.viewedAt = LocalDateTime.now();
    }
}
