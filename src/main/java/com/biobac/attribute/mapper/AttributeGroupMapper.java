package com.biobac.attribute.mapper;

import com.biobac.attribute.entity.AttributeDefinition;
import com.biobac.attribute.entity.AttributeGroup;
import com.biobac.attribute.request.AttributeGroupRequest;
import com.biobac.attribute.response.AttributeDefResponse;
import com.biobac.attribute.response.AttributeGroupResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AttributeGroupMapper {
    @Mapping(source = "definitions", target = "attributes")
    AttributeGroupResponse toDto(AttributeGroup entity);

    AttributeDefResponse toDto(AttributeDefinition entity);

    AttributeGroup toEntity(AttributeGroupRequest request);

    void update(@MappingTarget AttributeGroup entity, AttributeGroupRequest request);
}