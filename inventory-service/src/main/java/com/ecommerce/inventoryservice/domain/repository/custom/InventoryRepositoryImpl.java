package com.ecommerce.inventoryservice.domain.repository.custom;

import com.ecommerce.inventoryservice.domain.entity.Inventory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ecommerce.inventoryservice.domain.entity.QInventory.inventory;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Inventory> findInventoriesByProductIdForLock(List<Long> productIds) {
        return jpaQueryFactory.selectFrom(inventory)
                .where(inventory.productId.in(productIds))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .orderBy(inventory.productId.asc())
                .fetch();
    }
}
