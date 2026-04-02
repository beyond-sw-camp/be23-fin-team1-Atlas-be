package com.ozz.atlas.supply.item.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.domain.SupplyItemCategory;
import com.ozz.atlas.supply.item.dtos.CreateItemRequest;
import com.ozz.atlas.supply.item.dtos.ItemResponse;
import com.ozz.atlas.supply.item.dtos.UpdateItemRequest;
import com.ozz.atlas.supply.item.exception.ItemErrorCode;
import com.ozz.atlas.supply.item.exception.ItemException;
import com.ozz.atlas.supply.item.repository.SupplyItemCategoryRepository;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class SupplyItemService {

    private final SupplyItemRepository supplyItemRepository;
    private final SupplyItemCategoryRepository supplyItemCategoryRepository;

    public ItemResponse createItem(CreateItemRequest request) {

        if (supplyItemRepository.existsByItemCode(request.getItemCode())) {
            throw new ItemException(ItemErrorCode.ITEM_CODE_ALREADY_EXISTS);
        }
        SupplyItemCategory category = supplyItemCategoryRepository.findById(request.getItemCategoryId())
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_CATEGORY_NOT_FOUND));

        SupplyItem item = SupplyItem.create(
                category,
                request.getItemCode(),
                request.getItemName(),
                request.getUnit(),
                request.getSpec(),
                request.getShelfLifeDays()
        );
        return ItemResponse.fromEntity(supplyItemRepository.save(item));
    }

    public ItemResponse updateItem(Long itemId, UpdateItemRequest request) {
        SupplyItem item = supplyItemRepository.findByIdAndStatusIn(itemId, List.of(Status.ACTIVE, Status.DEACTIVE))
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_NOT_FOUND));

        SupplyItemCategory category = supplyItemCategoryRepository.findById(request.getItemCategoryId())
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_CATEGORY_NOT_FOUND));

        if (supplyItemRepository.existsByItemCodeAndIdNot(request.getItemCode(), item.getId())) {
            throw new ItemException(ItemErrorCode.ITEM_CODE_ALREADY_EXISTS);
        }

        item.update(
                category,
                request.getItemCode(),
                request.getItemName(),
                request.getUnit(),
                request.getSpec(),
                request.getShelfLifeDays()
        );

        return ItemResponse.fromEntity(item);
    }

    public void deleteItem(Long itemId) {
        SupplyItem item = supplyItemRepository.findByIdAndStatusIn(itemId, List.of(Status.ACTIVE, Status.DEACTIVE))
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_NOT_FOUND));

        item.changeActiveYn(Status.DELETE);
    }

    @Transactional(readOnly = true)
    public ItemResponse getItem(Long itemId) {
        SupplyItem item = supplyItemRepository.findByIdAndStatusIn(itemId, List.of(Status.ACTIVE))
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_NOT_FOUND));

        return ItemResponse.fromEntity(item);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> getItemList(Pageable pageable) {
        Page<SupplyItem> itemPage = supplyItemRepository.findAllByStatus(Status.ACTIVE, pageable);
        return itemPage.map(ItemResponse::fromEntity);
    }
}
