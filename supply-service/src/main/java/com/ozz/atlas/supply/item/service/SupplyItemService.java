package com.ozz.atlas.supply.item.service;

import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.domain.SupplyItemCategory;
import com.ozz.atlas.supply.item.dtos.CreateItemRequest;
import com.ozz.atlas.supply.item.dtos.ItemResponse;
import com.ozz.atlas.supply.item.dtos.UpdateItemRequest;
import com.ozz.atlas.supply.item.repository.SupplyItemCategoryRepository;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
@Transactional
public class SupplyItemService {

    private final SupplyItemRepository supplyItemRepository;
    private final SupplyItemCategoryRepository supplyItemCategoryRepository;

    public ItemResponse createItem(CreateItemRequest request) {

        if (supplyItemRepository.existsByItemCode(request.getItemCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item code already exists");
        }
        SupplyItemCategory category = supplyItemCategoryRepository.findById(request.getItemCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item category not found"));

        SupplyItem item = SupplyItem.create(
                category,
                request.getItemCode(),
                request.getItemName(),
                request.getUnit(),
                request.getSpec(),
                request.getShelfLifeDays()
        );
        return ItemResponse.from(supplyItemRepository.save(item));
    }

    public ItemResponse updateItem(String publicId, UpdateItemRequest request) {
        SupplyItem item = supplyItemRepository.findByPublicIdAndActiveYn(publicId, 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        SupplyItemCategory category = supplyItemCategoryRepository.findById(request.getItemCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item category not found"));

        if (supplyItemRepository.existsByItemCodeAndIdNot(request.getItemCode(), item.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item code already exists");
        }

        item.update(
                category,
                request.getItemCode(),
                request.getItemName(),
                request.getUnit(),
                request.getSpec(),
                request.getShelfLifeDays()
        );

        return ItemResponse.from(item);
    }

    public void deleteItem(String publicId) {
        SupplyItem item = supplyItemRepository.findByPublicIdAndActiveYn(publicId, 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        item.changeActiveYn(0);
    }

    @Transactional(readOnly = true)
    public ItemResponse getItem(String publicId) {
        SupplyItem item = supplyItemRepository.findByPublicIdAndActiveYn(publicId, 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        return ItemResponse.from(item);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> getItems(Pageable pageable) {
        Page<SupplyItem> itemPage = supplyItemRepository.findAllByActiveYn(1, pageable);
        return itemPage.map(ItemResponse::from);
    }
}
