package com.hive.app.async.transaction.mapper;

import com.hive.app.async.transaction.model.TransactionEntity;
import com.hive.app.async.transaction.model.TransactionRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {
    @Mapping(source = "source.id", target = "srcId")
    @Mapping(source = "source.type", target = "srcType")
    @Mapping(source = "destination.id", target = "destId")
    @Mapping(source = "destination.type", target = "destType")
    TransactionEntity toEntity(TransactionRecord record);

    @Mapping(target = "source.id", source = "srcId")
    @Mapping(target = "source.type", source = "srcType")
    @Mapping(target = "destination.id", source = "destId")
    @Mapping(target = "destination.type", source = "destType")
    TransactionRecord toRecord(TransactionEntity entity);
}
