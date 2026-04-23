package com.ozz.atlas.supply.lot.repository;

import com.ozz.atlas.supply.lot.domain.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {
    Optional<Lot> findByPublicId(String publicId);
    List<Lot> findBySupplierPublicId(String supplierPublicId);
    List<Lot> findBySourcePoItemPublicId(String sourcePoItemPublicId);
    List<Lot> findByItemPublicId(String itemPublicId);
    boolean existsByLotNumber(String lotNumber);
}