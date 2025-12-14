package com.hive.app.account.mapper;


import com.hive.app.account.model.UserDTO;
import com.hive.app.account.model.UserEntity;
import com.hive.app.account.model.request.RegisterRequest;
import com.hive.app.account.model.response.UserCardResponse;
import com.hive.app.account.model.response.UserProfileResponse;
import com.hive.app.account.repository.UserCardView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(source = "interests", target = "interests", qualifiedByName = "entity")
    UserEntity dtoToEntity(UserDTO user);

    @Mapping(source = "interests", target = "interests", qualifiedByName = "dto")
    UserDTO entityToDto(UserEntity user);

    UserDTO requestToDto(RegisterRequest user);

    UserCardResponse viewToCard(UserCardView user);

    UserCardResponse dtoToCard(UserDTO user);

    UserProfileResponse dtoToProfile(UserDTO dto);

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
