package com.ozz.atlas.supply.supplychain.service;

import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.subpurchaseorder.domain.SubPoStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrderItem;
import com.ozz.atlas.supply.subpurchaseorder.repository.SubPurchaseOrderRepository;
import com.ozz.atlas.supply.supplychain.dtos.SupplyChainResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplyChainService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SubPurchaseOrderRepository subPurchaseOrderRepository;

    public SupplyChainResponse getSupplyChains(String organizationPublicId) {
        List<SupplyPurchaseOrder> readablePurchaseOrders =
                purchaseOrderRepository.findAllReadableForSupplyChain(organizationPublicId, PoStatus.DELETED);
        List<SupplySubPurchaseOrder> readableSubOrders =
                subPurchaseOrderRepository.findAllReadableForSupplyChain(organizationPublicId, SubPoStatus.DELETED);

        Map<String, SupplyPurchaseOrder> rootOrders = new LinkedHashMap<>();
        readablePurchaseOrders.forEach(order -> rootOrders.put(order.getPublicId(), order));
        readableSubOrders.forEach(subOrder ->
                rootOrders.putIfAbsent(subOrder.getParentPurchaseOrder().getPublicId(), subOrder.getParentPurchaseOrder())
        );

        List<SupplyChainResponse.SupplyChainSummary> chains = rootOrders.values().stream()
                .sorted(Comparator.comparing(SupplyPurchaseOrder::getOrderedAt).reversed())
                .map(order -> toChain(order, readableSubOrders, organizationPublicId))
                .filter(chain -> !chain.getEdges().isEmpty())
                .toList();

        int directSegmentCount = chains.stream()
                .mapToInt(chain -> (int) chain.getEdges().stream().filter(SupplyChainResponse.SupplyChainEdge::isDirectContract).count())
                .sum();
        int priceVisibleSegmentCount = chains.stream()
                .mapToInt(chain -> (int) chain.getEdges().stream().filter(SupplyChainResponse.SupplyChainEdge::isPriceVisible).count())
                .sum();
        Set<String> extendedSuppliers = new LinkedHashSet<>();
        chains.forEach(chain -> chain.getNodes().stream()
                .filter(node -> "INDIRECT".equals(node.getScope()))
                .map(SupplyChainResponse.SupplyChainNode::getOrganizationPublicId)
                .forEach(extendedSuppliers::add));
        int riskChainCount = (int) chains.stream()
                .filter(chain -> chain.getEdges().stream().anyMatch(edge -> isRiskStatus(edge.getStatus())))
                .count();

        return SupplyChainResponse.builder()
                .chains(chains)
                .metrics(SupplyChainResponse.SupplyChainMetrics.builder()
                        .chainCount(chains.size())
                        .directSegmentCount(directSegmentCount)
                        .extendedSupplierCount(extendedSuppliers.size())
                        .priceVisibleSegmentCount(priceVisibleSegmentCount)
                        .riskChainCount(riskChainCount)
                        .build())
                .build();
    }

    private SupplyChainResponse.SupplyChainSummary toChain(
            SupplyPurchaseOrder rootOrder,
            List<SupplySubPurchaseOrder> readableSubOrders,
            String viewerOrganizationPublicId
    ) {
        List<SupplySubPurchaseOrder> subOrders = readableSubOrders.stream()
                .filter(subOrder -> subOrder.getParentPurchaseOrder().getPublicId().equals(rootOrder.getPublicId()))
                .sorted(Comparator.comparing(SupplySubPurchaseOrder::getOrderedAt))
                .toList();

        Map<String, SupplyChainResponse.SupplyChainNode> nodes = new LinkedHashMap<>();
        List<SupplyChainResponse.SupplyChainEdge> edges = new ArrayList<>();

        SupplyChainResponse.SupplyChainNode buyerNode = organizationNode(
                rootOrder.getBuyerOrganizationPublicId(),
                "발주 조직",
                "발주사",
                scopeFor(rootOrder.getBuyerOrganizationPublicId(), viewerOrganizationPublicId, rootOrder)
        );
        SupplyChainResponse.SupplyChainNode supplierNode = supplierNode(
                rootOrder.getSupplier(),
                "1차 협력사",
                scopeFor(rootOrder.getSupplier().getOrganizationPublicId(), viewerOrganizationPublicId, rootOrder)
        );
        putNode(nodes, buyerNode);
        putNode(nodes, supplierNode);

        edges.add(toPurchaseOrderEdge(rootOrder, buyerNode, supplierNode, viewerOrganizationPublicId));

        for (SupplySubPurchaseOrder subOrder : subOrders) {
            SupplySupplier issuer = subOrder.getParentPurchaseOrder().getSupplier();
            SupplyChainResponse.SupplyChainNode issuerNode = supplierNode(
                    issuer,
                    "발주 협력사",
                    scopeFor(issuer.getOrganizationPublicId(), viewerOrganizationPublicId, subOrder)
            );
            SupplyChainResponse.SupplyChainNode receiverNode = supplierNode(
                    subOrder.getSupplier(),
                    "하위 협력사",
                    scopeFor(subOrder.getSupplier().getOrganizationPublicId(), viewerOrganizationPublicId, subOrder)
            );
            putNode(nodes, issuerNode);
            putNode(nodes, receiverNode);
            edges.add(toSubPurchaseOrderEdge(subOrder, issuerNode, receiverNode, viewerOrganizationPublicId));
        }

        List<SupplyChainResponse.SupplyChainNode> visibleNodes = trimNodesForViewer(
                new ArrayList<>(nodes.values()),
                edges,
                viewerOrganizationPublicId
        );
        Set<String> visibleNodeIds = visibleNodes.stream()
                .map(SupplyChainResponse.SupplyChainNode::getNodeId)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        List<SupplyChainResponse.SupplyChainEdge> visibleEdges = edges.stream()
                .filter(edge -> visibleNodeIds.contains(edge.getFromNodeId()) && visibleNodeIds.contains(edge.getToNodeId()))
                .toList();

        return SupplyChainResponse.SupplyChainSummary.builder()
                .chainId(rootOrder.getPublicId())
                .rootOrderPublicId(rootOrder.getPublicId())
                .rootOrderNumber(rootOrder.getPoNumber())
                .itemName(representativePurchaseOrderItemName(rootOrder))
                .status(rootOrder.getPoStatus().name())
                .currentStep(visibleEdges.isEmpty() ? rootOrder.getPoStatus().name() : visibleEdges.get(visibleEdges.size() - 1).getStatus())
                .viewerRole(resolveViewerRole(visibleNodes, viewerOrganizationPublicId))
                .nodes(visibleNodes)
                .edges(visibleEdges)
                .visibility(SupplyChainResponse.SupplyChainVisibility.builder()
                        .visibleFields(List.of("품목", "수량", "납기", "상태", "직접 계약 구간 금액"))
                        .hiddenFields(List.of("간접 구간 단가", "간접 구간 금액", "협력사 내부 메모"))
                        .reason("가격/금액은 조회 조직이 직접 계약한 구간에만 노출됩니다.")
                        .build())
                .build();
    }

    private SupplyChainResponse.SupplyChainEdge toPurchaseOrderEdge(
            SupplyPurchaseOrder order,
            SupplyChainResponse.SupplyChainNode from,
            SupplyChainResponse.SupplyChainNode to,
            String viewerOrganizationPublicId
    ) {
        boolean direct = isDirect(order.getBuyerOrganizationPublicId(), order.getSupplier().getOrganizationPublicId(), viewerOrganizationPublicId);
        SupplyPurchaseOrderItem item = representativePurchaseOrderItem(order);
        return SupplyChainResponse.SupplyChainEdge.builder()
                .edgeId(order.getPublicId())
                .fromNodeId(from.getNodeId())
                .toNodeId(to.getNodeId())
                .documentPublicId(order.getPublicId())
                .documentNumber(order.getPoNumber())
                .documentType("PURCHASE_ORDER")
                .status(order.getPoStatus().name())
                .itemName(item == null ? "-" : item.getItem().getItemName())
                .orderedQty(item == null ? null : item.getOrderedQty())
                .confirmedQty(item == null ? null : item.getConfirmedQty())
                .expectedDueDate(item == null ? null : item.getExpectedDueDate())
                .directContract(direct)
                .priceVisible(direct)
                .amount(direct ? order.getTotalAmount() : null)
                .build();
    }

    private SupplyChainResponse.SupplyChainEdge toSubPurchaseOrderEdge(
            SupplySubPurchaseOrder order,
            SupplyChainResponse.SupplyChainNode from,
            SupplyChainResponse.SupplyChainNode to,
            String viewerOrganizationPublicId
    ) {
        String issuerOrganizationPublicId = order.getParentPurchaseOrder().getSupplier().getOrganizationPublicId();
        String receiverOrganizationPublicId = order.getSupplier().getOrganizationPublicId();
        boolean direct = isDirect(issuerOrganizationPublicId, receiverOrganizationPublicId, viewerOrganizationPublicId);
        SupplySubPurchaseOrderItem item = representativeSubPurchaseOrderItem(order);
        return SupplyChainResponse.SupplyChainEdge.builder()
                .edgeId(order.getPublicId())
                .fromNodeId(from.getNodeId())
                .toNodeId(to.getNodeId())
                .documentPublicId(order.getPublicId())
                .documentNumber(order.getSubPoNumber())
                .documentType("SUB_PURCHASE_ORDER")
                .status(order.getSubPoStatus().name())
                .itemName(item == null ? "-" : item.getItem().getItemName())
                .orderedQty(item == null ? null : item.getOrderedQty())
                .confirmedQty(item == null ? null : item.getConfirmedQty())
                .expectedDueDate(item == null ? null : item.getExpectedDueDate())
                .directContract(direct)
                .priceVisible(direct)
                .amount(direct ? order.getTotalAmount() : null)
                .build();
    }

    private List<SupplyChainResponse.SupplyChainNode> trimNodesForViewer(
            List<SupplyChainResponse.SupplyChainNode> nodes,
            List<SupplyChainResponse.SupplyChainEdge> edges,
            String viewerOrganizationPublicId
    ) {
        if (nodes.stream().anyMatch(node -> viewerOrganizationPublicId.equals(node.getOrganizationPublicId()) && "발주사".equals(node.getRole()))) {
            return nodes;
        }

        int viewerIndex = -1;
        for (int i = 0; i < nodes.size(); i++) {
            if (viewerOrganizationPublicId.equals(nodes.get(i).getOrganizationPublicId())) {
                viewerIndex = i;
                break;
            }
        }
        if (viewerIndex <= 0) {
            return nodes;
        }
        return nodes.subList(viewerIndex - 1, nodes.size());
    }

    private SupplyChainResponse.SupplyChainNode organizationNode(String organizationPublicId, String label, String role, String scope) {
        return SupplyChainResponse.SupplyChainNode.builder()
                .nodeId("org-" + organizationPublicId)
                .organizationPublicId(organizationPublicId)
                .label(label)
                .role(role)
                .scope(scope)
                .build();
    }

    private SupplyChainResponse.SupplyChainNode supplierNode(SupplySupplier supplier, String role, String scope) {
        return SupplyChainResponse.SupplyChainNode.builder()
                .nodeId("supplier-" + supplier.getOrganizationPublicId())
                .organizationPublicId(supplier.getOrganizationPublicId())
                .label(supplier.getSupplierName())
                .role(role)
                .scope(scope)
                .build();
    }

    private void putNode(Map<String, SupplyChainResponse.SupplyChainNode> nodes, SupplyChainResponse.SupplyChainNode node) {
        nodes.putIfAbsent(node.getNodeId(), node);
    }

    private String scopeFor(String organizationPublicId, String viewerOrganizationPublicId, SupplyPurchaseOrder order) {
        if (viewerOrganizationPublicId.equals(organizationPublicId)) return "ME";
        if (isDirect(order.getBuyerOrganizationPublicId(), order.getSupplier().getOrganizationPublicId(), viewerOrganizationPublicId)) return "DIRECT";
        return "INDIRECT";
    }

    private String scopeFor(String organizationPublicId, String viewerOrganizationPublicId, SupplySubPurchaseOrder order) {
        if (viewerOrganizationPublicId.equals(organizationPublicId)) return "ME";
        String issuerOrganizationPublicId = order.getParentPurchaseOrder().getSupplier().getOrganizationPublicId();
        String receiverOrganizationPublicId = order.getSupplier().getOrganizationPublicId();
        if (isDirect(issuerOrganizationPublicId, receiverOrganizationPublicId, viewerOrganizationPublicId)) return "DIRECT";
        return "INDIRECT";
    }

    private boolean isDirect(String fromOrganizationPublicId, String toOrganizationPublicId, String viewerOrganizationPublicId) {
        return viewerOrganizationPublicId.equals(fromOrganizationPublicId) || viewerOrganizationPublicId.equals(toOrganizationPublicId);
    }

    private SupplyPurchaseOrderItem representativePurchaseOrderItem(SupplyPurchaseOrder order) {
        return order.getActiveItems().stream().findFirst().orElse(null);
    }

    private SupplySubPurchaseOrderItem representativeSubPurchaseOrderItem(SupplySubPurchaseOrder order) {
        return order.getActiveItems().stream().findFirst().orElse(null);
    }

    private String representativePurchaseOrderItemName(SupplyPurchaseOrder order) {
        SupplyPurchaseOrderItem item = representativePurchaseOrderItem(order);
        if (item == null) return "-";
        int extraCount = Math.max(order.getActiveItems().size() - 1, 0);
        return extraCount == 0 ? item.getItem().getItemName() : item.getItem().getItemName() + " 외 " + extraCount;
    }

    private String resolveViewerRole(List<SupplyChainResponse.SupplyChainNode> nodes, String viewerOrganizationPublicId) {
        return nodes.stream()
                .filter(node -> viewerOrganizationPublicId.equals(node.getOrganizationPublicId()))
                .map(SupplyChainResponse.SupplyChainNode::getRole)
                .findFirst()
                .orElse("관련 조직");
    }

    private boolean isRiskStatus(String status) {
        return "CREATED".equals(status)
                || "PARTIALLY_CONFIRMED".equals(status)
                || "REJECTED".equals(status)
                || "CANCELLED".equals(status);
    }
}
