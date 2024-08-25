package com.food.ordering.system.order.service;

import com.food.ordering.system.order.service.domain.OrderDomainService;
import com.food.ordering.system.order.service.domain.OrderDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class BeanConfiguration {

    @Bean
    @Profile("!test")
    public OrderDomainService orderDomainService() {
        return new OrderDomainServiceImpl();
    }
}
