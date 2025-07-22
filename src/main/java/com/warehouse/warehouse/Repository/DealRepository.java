package com.warehouse.warehouse.Repository;

import com.warehouse.warehouse.models.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    boolean existsByDealId(String dealId);
}
