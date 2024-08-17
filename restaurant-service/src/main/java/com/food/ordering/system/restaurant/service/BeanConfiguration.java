package com.food.ordering.system.restaurant.service;

import com.food.ordering.system.restaurant.service.domain.RestaurantDomainService;
import com.food.ordering.system.restaurant.service.domain.RestaurantDomainServiceImpl;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    public RestaurantDomainService restaurantDomainService() {
        return new RestaurantDomainServiceImpl();
    }
}
