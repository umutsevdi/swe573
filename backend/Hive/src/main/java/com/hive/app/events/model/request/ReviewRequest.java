package com.hive.app.events.model.request;

import com.hive.app.events.model.DeliveryStatus;
import lombok.Data;

@Data
public class ReviewRequest {
    private Integer rating;
    private DeliveryStatus deliveryStatus;
    private String message;
}