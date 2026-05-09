package com.ozz.atlas.supply.common.init;

import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class InitialDataLoad implements CommandLineRunner {

    private static final List<SeedSupplier> SEED_SUPPLIERS = List.of(
            new SeedSupplier(
                    "ORG_ATLAS_SUPPLIER_TIER1",
                    "TIER1",
                    "Atlas Supplier Tier1",
                    "Manager Tier1",
                    "tier1-org@atlas.com",
                    "010-2222-0000"
            ),
            new SeedSupplier(
                    "ORG_ATLAS_SUPPLIER_TIER2",
                    "TIER2",
                    "Atlas Supplier Tier2",
                    "Manager Tier2",
                    "tier2-org@atlas.com",
                    "010-3333-0000"
            ),
            new SeedSupplier(
                    "ORG_ATLAS_SUPPLIER_TIER3",
                    "TIER3",
                    "Atlas Supplier Tier3",
                    "Manager Tier3",
                    "tier3-org@atlas.com",
                    "010-4444-0000"
            )
    );

    private final SupplierRepository supplierRepository;

    public InitialDataLoad(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    public void run(String... args) {
        SEED_SUPPLIERS.forEach(this::createSupplierIfAbsent);
    }

    private void createSupplierIfAbsent(SeedSupplier seedSupplier) {
        if (supplierRepository.existsByOrganizationPublicId(seedSupplier.organizationPublicId())) {
            return;
        }

        SupplySupplier supplier = SupplySupplier.create(
                seedSupplier.organizationPublicId(),
                seedSupplier.supplierCode(),
                seedSupplier.supplierName(),
                seedSupplier.primaryContactName(),
                seedSupplier.primaryContactEmail(),
                seedSupplier.primaryContactPhone()
        );
        supplier.activate();

        supplierRepository.save(supplier);
    }

    private record SeedSupplier(
            String organizationPublicId,
            String supplierCode,
            String supplierName,
            String primaryContactName,
            String primaryContactEmail,
            String primaryContactPhone
    ) {
    }
}
