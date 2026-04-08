package com.ozz.atlas.supply.lot.linemapping.repository;

import com.ozz.atlas.supply.lot.linemapping.domain.LotLineMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LotLineMappingRepository extends JpaRepository<LotLineMapping, Long> {
    List<LotLineMapping> findAllByLotPublicIdOrderByLotLineMappingIdDesc(String lotPublicId);
}