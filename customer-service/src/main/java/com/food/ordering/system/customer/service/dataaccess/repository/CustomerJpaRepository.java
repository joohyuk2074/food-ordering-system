package com.food.ordering.system.customer.service.dataaccess.repository;

import com.food.ordering.system.customer.service.dataaccess.entity.CustomerEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {


}
