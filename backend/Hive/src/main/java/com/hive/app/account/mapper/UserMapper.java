package com.hive.app.account.mapper;


import com.hive.app.account.model.UserDTO;
import com.hive.app.account.model.UserEntity;
import com.hive.app.account.model.request.RegisterRequest;
import com.hive.app.account.model.response.UserCardResponse;
import com.hive.app.account.model.response.UserProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "interests", target = "interests", qualifiedByName = "entity")
    UserEntity dtoToEntity(UserDTO user);

    @Mapping(source = "interests", target = "interests", qualifiedByName = "dto")
    UserDTO entityToDto(UserEntity user);

    UserDTO requestToDto(RegisterRequest user);

    UserCardResponse dtoToCard(UserDTO user);

    @Mapping(source = "timeBank", target = "remainingTime")
    UserProfileResponse dtoToUserProfileResponse(UserDTO user);

    @Named("entity")
    default String serializeInterests(List<String> interests) {
        if (interests == null || interests.isEmpty()) {
            return "";
        }
        return String.join(",", interests);
    }

    @Named("dto")
    default List<String> deserializeInterests(String interests) {
        if (interests == null || interests.isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(interests.split(","));
    }

}
