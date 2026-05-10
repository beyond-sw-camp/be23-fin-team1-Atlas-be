package com.ozz.atlas.supply.supplychain.dtos;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class SupplyChainResponse {
    private List<SupplyChainSummary> chains;
    private SupplyChainMetrics metrics;

    @Getter
    @Builder
    public static class SupplyChainMetrics {
        private int chainCount;
        private int directSegmentCount;
        private int extendedSupplierCount;
        private int priceVisibleSegmentCount;
        private int riskChainCount;
    }

    @Getter
    @Builder
    public static class SupplyChainSummary {
        private String chainId;
        private String rootOrderPublicId;
        private String rootOrderNumber;
        private String itemName;
        private String status;
        private String currentStep;
        private String viewerRole;
        private List<SupplyChainNode> nodes;
        private List<SupplyChainEdge> edges;
        private SupplyChainVisibility visibility;
    }

    @Getter
    @Builder
    public static class SupplyChainNode {
        private String nodeId;
        private String organizationPublicId;
        private String label;
        private String role;
        private String scope;
    }

    @Getter
    @Builder
    public static class SupplyChainEdge {
        private String edgeId;
        private String fromNodeId;
        private String toNodeId;
        private String documentPublicId;
        private String documentNumber;
        private String documentType;
        private String status;
        private String itemName;
        private Long orderedQty;
        private Long confirmedQty;
        private LocalDate expectedDueDate;
        private boolean directContract;
        private boolean priceVisible;
        private BigDecimal amount;
    }

    @Getter
    @Builder
    public static class SupplyChainVisibility {
        private List<String> visibleFields;
        private List<String> hiddenFields;
        private String reason;
    }
}
