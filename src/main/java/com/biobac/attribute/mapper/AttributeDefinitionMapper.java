package com.biobac.attribute.mapper;

import com.biobac.attribute.entity.AttributeDataType;
import com.biobac.attribute.entity.AttributeDefinition;
import com.biobac.attribute.entity.AttributeGroup;
import com.biobac.attribute.entity.OptionValue;
import com.biobac.attribute.response.AttributeDefResponse;
import com.biobac.attribute.response.AttributeOptionDefResponse;
import com.biobac.attribute.response.OptionValueResponse;
import org.mapstruct.Mapper;

import java.util.*;

@Mapper(componentModel = "spring")
public interface AttributeDefinitionMapper {
    AttributeDefResponse toDto(AttributeDefinition entity);

    OptionValueResponse toDto(OptionValue option);

    default AttributeDefResponse toDetailedDto(AttributeDefinition entity) {
        if (entity == null) {
            return null;
        }

        AttributeDataType type = entity.getDataType();
        if (EnumSet.of(AttributeDataType.SELECT, AttributeDataType.MULTISELECT).contains(type)) {
            AttributeOptionDefResponse resp = new AttributeOptionDefResponse();
            resp.setId(entity.getId());
            resp.setName(entity.getName());
            resp.setDataType(type);
            resp.setCreatedAt(entity.getCreatedAt());
            resp.setUpdatedAt(entity.getUpdatedAt());

            List<OptionValueResponse> options = Optional.ofNullable(entity.getOptions())
                    .orElseGet(Set::of)
                    .stream()
                    .filter(Objects::nonNull)
                    .map(this::toDto)
                    .toList();

            resp.setOptions(options);
            return resp;
        }

        return toDto(entity);
    }

    default List<Long> mapGroupIds(AttributeDefinition entity) {
        if (entity == null || entity.getGroups() == null || entity.getGroups().isEmpty()) {
            return List.of();
        }
        return entity.getGroups().stream()
                .filter(Objects::nonNull)
                .map(AttributeGroup::getId)
                .filter(Objects::nonNull)
                .sorted()
                .toList();
    }
}

