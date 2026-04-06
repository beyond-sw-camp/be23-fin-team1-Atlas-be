package com.ozz.atlas.supply.logistics.service;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.dtos.CreateLogisticsNodeRequestDto;
import com.ozz.atlas.supply.logistics.dtos.LogisticsNodeResponseDto;
import com.ozz.atlas.supply.logistics.dtos.UpdateLogisticsNodeRequestDto;
import com.ozz.atlas.supply.logistics.exception.LogisticsNodeErrorCode;
import com.ozz.atlas.supply.logistics.exception.LogisticsNodeException;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LogisticsNodeService {

    private final LogisticsNodeRepository logisticsNodeRepository;

    public LogisticsNodeService(LogisticsNodeRepository logisticsNodeRepository) {
        this.logisticsNodeRepository = logisticsNodeRepository;
    }

//    창고 생성
    public LogisticsNodeResponseDto createLogisticsNode(CreateLogisticsNodeRequestDto dto){
        if (logisticsNodeRepository.existsByNodeCode(dto.getNodeCode())){
            throw new LogisticsNodeException(LogisticsNodeErrorCode.NODE_CODE_ALREADY_EXISTS);
        }

        LogisticsNode saveNode = logisticsNodeRepository.save(dto.toEntity());
        return LogisticsNodeResponseDto.from(saveNode);
    }

//    창고 목록 조회
    @Transactional(readOnly = true)
    public Page<LogisticsNodeResponseDto> getLogisticsNodes(Pageable pageable){
        return logisticsNodeRepository.findAll(pageable).map(LogisticsNodeResponseDto::from);
    }

//    창고 상세 조회
    @Transactional(readOnly = true)
    public LogisticsNodeResponseDto getLogisticsNode(String publicId){
        LogisticsNode node = logisticsNodeRepository.findByPublicId(publicId)
                .orElseThrow(()->new LogisticsNodeException(LogisticsNodeErrorCode.NODE_NOT_FOUND));

        return LogisticsNodeResponseDto.from(node);
    }

//    창고 수정
    public LogisticsNodeResponseDto updateLogisticsNode(String publicId, UpdateLogisticsNodeRequestDto dto){
        LogisticsNode node = logisticsNodeRepository.findByPublicId(publicId)
                .orElseThrow(()->new LogisticsNodeException(LogisticsNodeErrorCode.NODE_NOT_FOUND));

        if (logisticsNodeRepository.existsByNodeCodeAndPublicIdNot(dto.getNodeCode(), publicId)){
            throw new LogisticsNodeException(LogisticsNodeErrorCode.NODE_CODE_ALREADY_EXISTS);
        }

        node.update(
                dto.getNodeCode(),
                dto.getNodeName(),
                dto.getNodeType(),
                dto.getAddress(),
                dto.getLatitude(),
                dto.getLongitude()
        );

        return LogisticsNodeResponseDto.from(node);
    }

//    창고 활성화
    public LogisticsNodeResponseDto activateLogisticsNode(String publicId){
        LogisticsNode node = logisticsNodeRepository.findByPublicId(publicId)
                .orElseThrow(()->new LogisticsNodeException(LogisticsNodeErrorCode.NODE_NOT_FOUND));

        node.activate();
        return LogisticsNodeResponseDto.from(node);
    }

//    창고 비활성화
    public LogisticsNodeResponseDto deactivateLogisticsNode(String publicId){
        LogisticsNode node = logisticsNodeRepository.findByPublicId(publicId)
                .orElseThrow(()->new LogisticsNodeException(LogisticsNodeErrorCode.NODE_NOT_FOUND));

        node.deactivate();
        return LogisticsNodeResponseDto.from(node);
    }
}
