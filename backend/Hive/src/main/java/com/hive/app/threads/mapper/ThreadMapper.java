package com.hive.app.threads.mapper;


import com.hive.app.account.mapper.UserMapper;
import com.hive.app.threads.model.ThreadDTO;
import com.hive.app.threads.model.ThreadEntity;
import com.hive.app.threads.model.ThreadMessageEntity;
import com.hive.app.threads.model.response.ThreadMessageResponse;
import com.hive.app.threads.model.response.ThreadResponse;
import com.hive.app.threads.repository.ThreadView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class})
public interface ThreadMapper {
    @Mapping(source = "ownerId", target = "owner.id")
    ThreadMessageEntity dtoToEntity(ThreadMessageResponse dto);

    @Mapping(target = "ownerId", source = "owner.id")
    ThreadMessageResponse entityToDto(ThreadMessageEntity entity);

    @Mapping(source = "tags", target = "tags", qualifiedByName = "forumTagsEntity")
    @Mapping(source = "ownerId", target = "owner.id")
    ThreadEntity dtoToEntity(ThreadDTO dto);

    @Mapping(source = "tags", target = "tags", qualifiedByName = "forumTagsDTO")
    @Mapping(target = "ownerId", source = "owner.id")
    ThreadDTO entityToDto(ThreadEntity entity);

    ThreadResponse toResponse(ThreadView view);

    @Named("forumTagsEntity")
    default String serializeTags(List<String> interests) {
        if (interests == null || interests.isEmpty()) {
            return "";
        }
        return String.join(",", interests);
    }

    @Named("forumTagsDTO")
    default List<String> deserializeTags(String interests) {
        if (interests == null || interests.isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(interests.split(","));
    }

}
