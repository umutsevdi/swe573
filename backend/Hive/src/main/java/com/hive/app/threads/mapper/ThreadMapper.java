package com.hive.app.threads.mapper;


import com.hive.app.account.mapper.UserMapper;
import com.hive.app.threads.model.ThreadDTO;
import com.hive.app.threads.model.ThreadEntity;
import com.hive.app.threads.model.ThreadMessageDTO;
import com.hive.app.threads.model.ThreadMessageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UserMapper.class}
)
public interface ThreadMapper {
    ThreadMapper INSTANCE = Mappers.getMapper(ThreadMapper.class);

    @Mapping(source = "owner.interests", target = "owner.interests", qualifiedByName = "entity")
    ThreadMessageEntity dtoToEntity(ThreadMessageDTO dto);

    @Mapping(target = "owner.interests", source = "owner.interests", qualifiedByName = "dto")
    ThreadMessageDTO entityToDto(ThreadMessageEntity entity);

    @Mapping(source = "tags", target = "tags", qualifiedByName = "forumTagsEntity")
    @Mapping(source = "owner.interests", target = "owner.interests", qualifiedByName = "entity")
    ThreadEntity dtoToEntity(ThreadDTO dto);

    @Mapping(source = "tags", target = "tags", qualifiedByName = "forumTagsDTO")
    @Mapping(target = "owner.interests", source = "owner.interests", qualifiedByName = "dto")
    ThreadDTO entityToDto(ThreadEntity entity);

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
