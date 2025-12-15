package com.hive.app.async.notification.mapper;

import com.hive.app.async.notification.model.NotificationEntity;
import com.hive.app.async.notification.model.response.NotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NotificationMapper {
    
    @Mapping(source = "ownerId", target = "ownerId")
    @Mapping(source = "redirectId", target = "redirect.id")
    @Mapping(source = "redirectType", target = "redirect.type")
    NotificationResponse entityToResponse(NotificationEntity entity);
}
