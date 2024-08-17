package com.food.ordering.system.dataaccess.restaurant.repository;

import com.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntityId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, RestaurantEntityId> {

    Optional<List<RestaurantEntity>> findByRestaurantIdAndProductIdIn(UUID restaurantId, List<UUID> productIds);

    List<RestaurantEntity> findAllByRestaurantIdAndProductIdIn(UUID restaurantId, List<UUID> productIds);
}
