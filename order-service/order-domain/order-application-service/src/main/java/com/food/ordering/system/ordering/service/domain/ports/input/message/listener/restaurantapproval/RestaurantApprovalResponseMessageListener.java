package com.food.ordering.system.ordering.service.domain.ports.input.message.listener.restaurantapproval;

import com.food.ordering.system.ordering.service.domain.dto.message.RestaurantApprovalResponse;

public interface RestaurantApprovalResponseMessageListener {

    void orderApproved(RestaurantApprovalResponse restaurantApprovalResponse);

    void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse);
}
