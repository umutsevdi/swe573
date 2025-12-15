package com.hive.app.events.mapper;

import com.hive.app.account.mapper.UserMapper;
import com.hive.app.events.model.ReviewEntity;
import com.hive.app.events.model.request.ReviewRequest;
import com.hive.app.events.model.response.ReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class})
public interface ReviewMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ReviewEntity requestToEntity(ReviewRequest request);
    
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    ReviewResponse entityToResponse(ReviewEntity entity);
}