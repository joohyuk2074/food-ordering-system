package com.food.ordering.system.restaurant.service.dataaccess.repository;

import com.food.ordering.system.restaurant.service.dataaccess.entity.OrderApprovalEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderApprovalJpaRepository extends JpaRepository<OrderApprovalEntity, UUID> {


}