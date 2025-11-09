package com.hive.app.events.mapper;

import com.hive.app.account.mapper.UserMapper;
import com.hive.app.events.model.EventDTO;
import com.hive.app.events.model.EventEntity;
import com.hive.app.events.model.EventRequest;
import com.hive.app.events.model.EventResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class})
public interface EventMapper {
    @Mapping(source = "locationName", target = "location.name")
    @Mapping(source = "latitude", target = "location.latitude")
    @Mapping(source = "longitude", target = "location.longitude")
    @Mapping(source = "tags", target = "tags", qualifiedByName = "dtoTag")
    EventDTO entityToDTO(EventEntity event);

    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "latitude", source = "location.latitude")
    @Mapping(target = "longitude", source = "location.longitude")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "entityTag")
    EventEntity dtoToEntity(EventDTO dto);

    EventResponse dtoToResponse(EventDTO dto);

    @Mapping(source = "locationName", target = "location.name")
    @Mapping(source = "latitude", target = "location.latitude")
    @Mapping(source = "longitude", target = "location.longitude")
    @Mapping(source = "tags", target = "tags", qualifiedByName = "dtoTag")
    @Mapping(source = "owner.id", target = "ownerId")
    EventResponse entityToResponse(EventEntity dto);


    EventRequest dtoToRequest(EventDTO dto);

    EventDTO requestToDto(EventRequest dto);

    @Named("entityTag")
    default String serializeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(",", tags);
    }

    @Named("dtoTag")
    default List<String> deserializeTags(String tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(tags.split(","));
    }
}
