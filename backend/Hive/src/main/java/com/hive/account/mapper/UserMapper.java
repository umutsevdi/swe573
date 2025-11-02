package com.hive.account.mapper;


import com.hive.account.model.RegisterRequest;
import com.hive.account.model.UserCardResponse;
import com.hive.account.model.UserDTO;
import com.hive.account.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "interests", target = "interests", qualifiedByName = "entity")
    UserEntity dtoToEntity(UserDTO user);

    @Mapping(source = "interests", target = "interests", qualifiedByName = "dto")
    UserDTO entityToDto(UserEntity user);

    UserDTO requestToDto(RegisterRequest user);

    UserCardResponse dtoToCard(UserDTO user);

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
