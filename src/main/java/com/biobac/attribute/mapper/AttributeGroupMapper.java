package com.biobac.attribute.mapper;

import com.biobac.attribute.entity.AttributeDefinition;
import com.biobac.attribute.entity.AttributeGroup;
import com.biobac.attribute.request.AttributeGroupRequest;
import com.biobac.attribute.response.AttributeDefResponse;
import com.biobac.attribute.response.AttributeGroupResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AttributeGroupMapper {
    @Mapping(source = "definitions", target = "attributes")
    AttributeGroupResponse toDto(AttributeGroup entity);

    @Mapping(target = "attributeGroupIds", expression = "java(mapGroupIds(entity))")
    AttributeDefResponse toDto(AttributeDefinition entity);

    AttributeGroup toEntity(AttributeGroupRequest request);

    void update(@MappingTarget AttributeGroup entity, AttributeGroupRequest request);

    default List<Long> mapGroupIds(AttributeDefinition entity) {
        if (entity.getGroups() == null) {
            return new ArrayList<>();
        }
        return entity.getGroups().stream()
                .map(AttributeGroup::getId)
                .collect(Collectors.toList());
    }
}