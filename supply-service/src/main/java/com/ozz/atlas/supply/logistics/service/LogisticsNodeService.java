package com.ozz.atlas.supply.logistics.service;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.supply.inventory.repository.SupplyItemInventoryRepository;
import com.ozz.atlas.supply.kafka.context.SupplyChainContext;
import com.ozz.atlas.supply.kafka.event.SupplyDomainEventFactory;
import com.ozz.atlas.supply.kafka.outbox.OutboxEventAppender;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeCapacityStatus;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeType;
import com.ozz.atlas.supply.logistics.dtos.CreateLogisticsNodeRequestDto;
import com.ozz.atlas.supply.logistics.dtos.GeocodingPointDto;
import com.ozz.atlas.supply.logistics.dtos.LogisticsNodeResponseDto;
import com.ozz.atlas.supply.logistics.dtos.UpdateLogisticsNodeRequestDto;
import com.ozz.atlas.supply.logistics.exception.LogisticsNodeErrorCode;
import com.ozz.atlas.supply.logistics.exception.LogisticsNodeException;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class LogisticsNodeService {

    private static final String ADMIN_ORGANIZATION_TYPE = "ADMIN";
    private static final String ADMIN_ROLE = "ADMIN";

    private final LogisticsNodeRepository logisticsNodeRepository;
    private final OrganizationAliasClient organizationAliasClient;
    private final AddressGeocodingClient addressGeocodingClient;
    private final OutboxEventAppender outboxEventAppender;
    private final SupplyDomainEventFactory supplyDomainEventFactory;
    private final SupplyItemInventoryRepository inventoryRepository;

    public LogisticsNodeService(
            LogisticsNodeRepository logisticsNodeRepository,
            OrganizationAliasClient organizationAliasClient,
            AddressGeocodingClient addressGeocodingClient,
            OutboxEventAppender outboxEventAppender,
            SupplyDomainEventFactory supplyDomainEventFactory, SupplyItemInventoryRepository inventoryRepository
    ) {
        this.logisticsNodeRepository = logisticsNodeRepository;
        this.organizationAliasClient = organizationAliasClient;
        this.addressGeocodingClient = addressGeocodingClient;
        this.outboxEventAppender = outboxEventAppender;
        this.supplyDomainEventFactory = supplyDomainEventFactory;
        this.inventoryRepository = inventoryRepository;
    }

    // 물류거점 생성
    public LogisticsNodeResponseDto createLogisticsNode(
            String organizationPublicId,
            String organizationType,
            String userRole,
            CreateLogisticsNodeRequestDto dto
    ) {
        validateLogisticsNodeActor(organizationPublicId, organizationType, userRole);
        validateWarehouseOnly(dto.getNodeType());

        String organizationAlias = organizationAliasClient.getOrganizationAlias(organizationPublicId);
        String generatedNodeCode = generateNodeCode(organizationPublicId, organizationAlias);
        GeocodingPointDto point = geocodeRequiredAddress(dto.getBaseAddress());

        LogisticsNode savedNode = logisticsNodeRepository.save(
                dto.toEntity(
                        organizationPublicId,
                        generatedNodeCode,
                        point.getLatitude(),
                        point.getLongitude()
                )
        );

        return LogisticsNodeResponseDto.from(savedNode);
    }

    // 물류거점 목록 조회
    @Transactional(readOnly = true)
    public Page<LogisticsNodeResponseDto> getLogisticsNodes(
            String organizationPublicId,
            String organizationType,
            String userRole,
            Pageable pageable
    ) {
        validateLogisticsNodeActor(organizationPublicId, organizationType, userRole);

        return logisticsNodeRepository
                .findByOrganizationPublicIdAndNodeType(
                        organizationPublicId,
                        LogisticsNodeType.WAREHOUSE,
                        pageable
                )
                .map(LogisticsNodeResponseDto::from);
    }

    // 물류거점 상세 조회
    @Transactional(readOnly = true)
    public LogisticsNodeResponseDto getLogisticsNode(
            String organizationPublicId,
            String organizationType,
            String userRole,
            String publicId
    ) {
        validateLogisticsNodeActor(organizationPublicId, organizationType, userRole);

        LogisticsNode node = getOwnedLogisticsNode(publicId, organizationPublicId);

        return LogisticsNodeResponseDto.from(node);
    }

    // 출하에서 물류거점 publicId로 entity 조회할 때 사용
    @Transactional(readOnly = true)
    public LogisticsNode getLogisticsNodeEntityByPublicId(String publicId) {
        return logisticsNodeRepository.findByPublicId(publicId)
                .orElseThrow(() -> new LogisticsNodeException(LogisticsNodeErrorCode.NODE_NOT_FOUND));
    }

    // 출하에서 물류거점 id로 entity 조회할 때 사용
    @Transactional(readOnly = true)
    public LogisticsNode getLogisticsNodeEntity(Long id) {
        return logisticsNodeRepository.findById(id)
                .orElseThrow(() -> new LogisticsNodeException(LogisticsNodeErrorCode.NODE_NOT_FOUND));
    }

    // node id 목록을 publicId map으로 변환
    @Transactional(readOnly = true)
    public Map<Long, String> getNodePublicIdMap(Collection<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return logisticsNodeRepository.findByIdIn(nodeIds).stream()
                .collect(Collectors.toMap(LogisticsNode::getId, LogisticsNode::getPublicId));
    }

    // 물류거점 수정
    public LogisticsNodeResponseDto updateLogisticsNode(
            String organizationPublicId,
            String organizationType,
            String userRole,
            String publicId,
            String actorUserPublicId,
            UpdateLogisticsNodeRequestDto dto
    ) {
        validateLogisticsNodeActor(organizationPublicId, organizationType, userRole);
        validateWarehouseOnly(dto.getNodeType());

        LogisticsNode node = getOwnedLogisticsNode(publicId, organizationPublicId);
        LogisticsNodeCapacityStatus beforeCapacityStatus = node.getCapacityStatus();
        GeocodingPointDto point = geocodeRequiredAddress(dto.getBaseAddress());
        String displayAddress = buildDisplayAddress(dto.getBaseAddress(), dto.getDetailAddress());

        node.update(
                dto.getNodeName(),
                LogisticsNodeType.WAREHOUSE,
                dto.getBaseAddress(),
                dto.getDetailAddress(),
                displayAddress,
                point.getLatitude(),
                point.getLongitude(),
                dto.getCapacityStatus()
        );

        if (dto.getCapacityStatus() != null && dto.getCapacityStatus() != beforeCapacityStatus) {
            appendCapacityStatusChangedEvent(node, actorUserPublicId, organizationPublicId);
        }

        return LogisticsNodeResponseDto.from(node);
    }

    // 물류거점 활성화
    public LogisticsNodeResponseDto activateLogisticsNode(
            String organizationPublicId,
            String organizationType,
            String userRole,
            String publicId
    ) {
        validateLogisticsNodeActor(organizationPublicId, organizationType, userRole);

        LogisticsNode node = getOwnedLogisticsNode(publicId, organizationPublicId);

        node.activate();

        return LogisticsNodeResponseDto.from(node);
    }

    // 물류거점 비활성화
    public LogisticsNodeResponseDto deactivateLogisticsNode(
            String organizationPublicId,
            String organizationType,
            String userRole,
            String publicId
    ) {
        validateLogisticsNodeActor(organizationPublicId, organizationType, userRole);

        LogisticsNode node = getOwnedLogisticsNode(publicId, organizationPublicId);
        // 물류거점 비활성화 불가 검증
        if (inventoryRepository.existsLiveInventoryInNode(organizationPublicId, publicId)) {
            throw new LogisticsNodeException(LogisticsNodeErrorCode.NODE_HAS_REMAINING_INVENTORY);
        }
        node.deactivate();

        return LogisticsNodeResponseDto.from(node);
    }

    // 물류거점 코드는 WH-{organizationAlias}-{일련번호} 규칙으로 자동 생성한다.
    private String generateNodeCode(String organizationPublicId, String organizationAlias) {
        long nextSequence = logisticsNodeRepository.countByOrganizationPublicId(organizationPublicId) + 1L;

        String candidate = buildNodeCode(organizationAlias, nextSequence);

        while (logisticsNodeRepository.existsByNodeCode(candidate)) {
            nextSequence++;
            candidate = buildNodeCode(organizationAlias, nextSequence);
        }

        return candidate;
    }

    private String buildNodeCode(String organizationAlias, long sequence) {
        return "WH-" + organizationAlias + "-" + String.format("%03d", sequence);
    }

    // 물류거점은 로그인한 조직 자신의 데이터만 조회/수정할 수 있다.
    private LogisticsNode getOwnedLogisticsNode(String publicId, String organizationPublicId) {
        return logisticsNodeRepository
                .findByPublicIdAndOrganizationPublicIdAndNodeType(
                        publicId,
                        organizationPublicId,
                        LogisticsNodeType.WAREHOUSE
                )
                .orElseThrow(() -> new LogisticsNodeException(LogisticsNodeErrorCode.NODE_NOT_FOUND));
    }

    private void validateOrganizationHeader(String organizationPublicId) {
        if (organizationPublicId == null || organizationPublicId.isBlank()) {
            throw new LogisticsNodeException(LogisticsNodeErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // 플랫폼 관리자는 물류거점을 관리하지 않는다. 실제 조직만 자기 거점을 관리할 수 있다.
    private void validateLogisticsNodeActor(
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        validateOrganizationHeader(organizationPublicId);

        if (organizationType == null || organizationType.isBlank()) {
            throw new LogisticsNodeException(LogisticsNodeErrorCode.INVALID_INPUT_VALUE);
        }

        if (ADMIN_ORGANIZATION_TYPE.equals(organizationType) || ADMIN_ROLE.equals(userRole)) {
            throw new LogisticsNodeException(LogisticsNodeErrorCode.ACCESS_DENIED);
        }
    }
    private void validateWarehouseOnly(LogisticsNodeType nodeType) {
        if (nodeType != null && nodeType != LogisticsNodeType.WAREHOUSE) {
            throw new LogisticsNodeException(LogisticsNodeErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // 주소는 필수로 받고, 저장 직전에 외부 지오코딩 API로 좌표를 계산한다.
    private GeocodingPointDto geocodeRequiredAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new LogisticsNodeException(LogisticsNodeErrorCode.INVALID_INPUT_VALUE);
        }

        return addressGeocodingClient.geocode(address);
    }

    private String buildDisplayAddress(String baseAddress, String detailAddress) {
        if (baseAddress == null || baseAddress.isBlank()) {
            throw new LogisticsNodeException(LogisticsNodeErrorCode.INVALID_INPUT_VALUE);
        }

        if (detailAddress == null || detailAddress.isBlank()) {
            return baseAddress;
        }

        return baseAddress + " " + detailAddress.trim();
    }

    private void appendCapacityStatusChangedEvent(
            LogisticsNode node,
            String actorUserPublicId,
            String organizationPublicId
    ) {
        outboxEventAppender.append(
                supplyDomainEventFactory.create(
                        KafkaTopics.SUPPLY_LOGISTICS_NODE,
                        EventTypes.LOGISTICS_NODE_CAPACITY_STATUS_CHANGED,
                        AggregateType.LOGISTICS_NODE,
                        node.getPublicId(),
                        actorUserPublicId,
                        organizationPublicId,
                        SupplyChainContext.empty(),
                        supplyDomainEventFactory.payload(
                                node.getPublicId(),
                                node.getNodeCode(),
                                node.getCapacityStatus().name(),
                                "물류 거점 용량 상태 변경",
                                "물류 거점 용량 상태가 변경되었습니다.",
                                null
                        )
                )
        );
    }
}
