package com.ozz.atlas.supply.logistics.service;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
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

    private final LogisticsNodeRepository logisticsNodeRepository;
    private final OrganizationAliasClient organizationAliasClient;
    private final AddressGeocodingClient addressGeocodingClient;

    public LogisticsNodeService(LogisticsNodeRepository logisticsNodeRepository, OrganizationAliasClient organizationAliasClient, AddressGeocodingClient addressGeocodingClient) {
        this.logisticsNodeRepository = logisticsNodeRepository;
        this.organizationAliasClient = organizationAliasClient;
        this.addressGeocodingClient = addressGeocodingClient;
    }

//    창고 생성
    public LogisticsNodeResponseDto createLogisticsNode(String organizationPublicId, CreateLogisticsNodeRequestDto dto) {
        if (organizationPublicId == null || organizationPublicId.isBlank()) {
            throw new LogisticsNodeException(LogisticsNodeErrorCode.INVALID_INPUT_VALUE);
        }

        String organizationAlias = organizationAliasClient.getOrganizationAlias(organizationPublicId);
        String generatedNodeCode = generateNodeCode(organizationPublicId, organizationAlias);
        GeocodingPointDto point = geocodeRequiredAddress(dto.getAddress());

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

    //    창고 목록 조회
    @Transactional(readOnly = true)
    public Page<LogisticsNodeResponseDto> getLogisticsNodes(String organizationPublicId, Pageable pageable){
        validateOrganizationHeader(organizationPublicId);
        return logisticsNodeRepository
                .findByOrganizationPublicId(organizationPublicId, pageable)
                .map(LogisticsNodeResponseDto::from);
    }

    //    창고 상세 조회
    @Transactional(readOnly = true)
    public LogisticsNodeResponseDto getLogisticsNode(String organizationPublicId, String publicId){
        validateOrganizationHeader(organizationPublicId);
        LogisticsNode node = getOwnedLogisticsNode(publicId, organizationPublicId);

        return LogisticsNodeResponseDto.from(node);
    }


    //    출하에서 창고 조회용(요청 입력 처리)
//    요청 body에서 받은 node publicId -> 내부 entity로 변환
    @Transactional(readOnly = true)
    public LogisticsNode getLogisticsNodeEntityByPublicId(String publicId){
        return logisticsNodeRepository.findByPublicId(publicId)
                .orElseThrow(()->new LogisticsNodeException(LogisticsNodeErrorCode.NODE_NOT_FOUND));
    }

//    entity 조회(응답 변환)
//    내부 node id -> node publicId로 변경
    @Transactional(readOnly = true)
    public LogisticsNode getLogisticsNodeEntity(Long id){
        return logisticsNodeRepository.findById(id)
                .orElseThrow(()->new LogisticsNodeException(LogisticsNodeErrorCode.NODE_NOT_FOUND));
    }

//    node id를 모아서 목록 조회
    @Transactional(readOnly = true)
    public Map<Long, String> getNodePublicIdMap(Collection<Long> nodeIds){
        if (nodeIds == null || nodeIds.isEmpty()){
            return Collections.emptyMap();
        }

        return logisticsNodeRepository.findByIdIn(nodeIds).stream()
                .collect(Collectors.toMap(LogisticsNode::getId, LogisticsNode::getPublicId));
    }

//    창고 수정
    public LogisticsNodeResponseDto updateLogisticsNode(
            String organizationPublicId,
            String publicId,
            UpdateLogisticsNodeRequestDto dto
    ){
        validateOrganizationHeader(organizationPublicId);
        LogisticsNode node = getOwnedLogisticsNode(publicId, organizationPublicId);

        GeocodingPointDto point = geocodeRequiredAddress(dto.getAddress());

        node.update(
                dto.getNodeName(),
                dto.getNodeType(),
                dto.getAddress(),
                point.getLatitude(),
                point.getLongitude()
        );

        return LogisticsNodeResponseDto.from(node);
    }

    //    창고 활성화
    public LogisticsNodeResponseDto activateLogisticsNode(String organizationPublicId, String publicId){
        validateOrganizationHeader(organizationPublicId);
        LogisticsNode node = getOwnedLogisticsNode(publicId, organizationPublicId);

        node.activate();
        return LogisticsNodeResponseDto.from(node);
    }

    //    창고 비활성화
    public LogisticsNodeResponseDto deactivateLogisticsNode(String organizationPublicId, String publicId){
        validateOrganizationHeader(organizationPublicId);
        LogisticsNode node = getOwnedLogisticsNode(publicId, organizationPublicId);

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
        return logisticsNodeRepository.findByPublicIdAndOrganizationPublicId(publicId, organizationPublicId)
                .orElseThrow(() -> new LogisticsNodeException(LogisticsNodeErrorCode.NODE_NOT_FOUND));
    }

    private void validateOrganizationHeader(String organizationPublicId) {
        if (organizationPublicId == null || organizationPublicId.isBlank()) {
            throw new LogisticsNodeException(LogisticsNodeErrorCode.INVALID_INPUT_VALUE);
        }
    }
    // 주소는 필수로 받고, 저장 직전에 외부 지오코딩으로 좌표를 계산한다.
    private GeocodingPointDto geocodeRequiredAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new LogisticsNodeException(LogisticsNodeErrorCode.INVALID_INPUT_VALUE);
        }

        return addressGeocodingClient.geocode(address);
    }

}
