package com.biobac.attribute.service.impl;

import com.biobac.attribute.entity.*;
import com.biobac.attribute.exception.InvalidDataException;
import com.biobac.attribute.exception.NotFoundException;
import com.biobac.attribute.repository.AttributeDefinitionRepository;
import com.biobac.attribute.repository.AttributeValueRepository;
import com.biobac.attribute.repository.OptionValueRepository;
import com.biobac.attribute.request.AttributeUpsertRequest;
import com.biobac.attribute.response.AttributeDefResponse;
import com.biobac.attribute.response.AttributeOptionValueResponse;
import com.biobac.attribute.response.AttributeValueResponse;
import com.biobac.attribute.response.OptionValueResponse;
import com.biobac.attribute.service.AttributeValueService;
import com.biobac.attribute.utils.AttributeValueUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeValueServiceImpl implements AttributeValueService {
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final OptionValueRepository optionValueRepository;

    @Override
    @Transactional
    public List<AttributeDefResponse> createAttributeValues(Long targetId, String targetType, List<AttributeUpsertRequest> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return List.of();
        }

        AttributeTargetType resolvedType = AttributeTargetType.valueOf(targetType.toUpperCase());

        for (AttributeUpsertRequest req : attributes) {
            AttributeDefinition def = attributeDefinitionRepository.findById(req.getId())
                    .orElseThrow(() -> new NotFoundException("AttributeDefinition not found: " + req.getId()));

            String rawValue = convertValueToString(def, req.getValue());

            AttributeValueUtil.validateOrThrow(def.getDataType(), rawValue);

            validateOptions(def, rawValue);

            AttributeValue attrValue = attributeValueRepository
                    .findByDefinitionIdAndTargetTypeAndTargetId(def.getId(), resolvedType, targetId)
                    .orElseGet(AttributeValue::new);

            attrValue.setDefinition(def);
            attrValue.setTargetType(resolvedType);
            attrValue.setTargetId(targetId);
            attrValue.setValue(rawValue);

            attributeValueRepository.save(attrValue);
        }

        List<AttributeValue> updatedValues = attributeValueRepository.findByTargetTypeAndTargetId(resolvedType, targetId);

        return updatedValues.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeDefResponse> getAttributeValues(Long targetId, String targetType) {
        List<AttributeValue> values = attributeValueRepository.findByTargetTypeAndTargetId(
                AttributeTargetType.valueOf(targetType), targetId
        );

        return values.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAttributeValues(Long targetId, String targetType) {
        AttributeTargetType resolvedType = AttributeTargetType.valueOf(targetType.toUpperCase());

        List<AttributeValue> values = attributeValueRepository.findByTargetTypeAndTargetId(resolvedType, targetId);

        if (!values.isEmpty()) {
            attributeValueRepository.deleteAll(values);
        }
    }

    private AttributeDefResponse toResponse(AttributeValue v) {
        AttributeDataType type = v.getDefinition().getDataType();

        if (type == AttributeDataType.SELECT || type == AttributeDataType.MULTISELECT) {
            List<Long> optionIds = new ArrayList<>();
            String raw = v.getValue();
            if (raw != null && !raw.isBlank()) {
                try {
                    if (type == AttributeDataType.SELECT) {
                        optionIds.add(Long.valueOf(raw.trim()));
                    } else {
                        optionIds.addAll(AttributeValueUtil.parseMultiSelect(raw));
                    }
                } catch (Exception ignored) {
                }
            }
            List<OptionValueResponse> optionResponses = Collections.emptyList();
            if (!optionIds.isEmpty()) {
                List<OptionValue> options = optionValueRepository.findAllById(optionIds);
                optionResponses = options.stream().map(o -> {
                    OptionValueResponse or = new OptionValueResponse();
                    or.setId(o.getId());
                    or.setLabel(o.getLabel());
                    or.setValue(o.getValue());
                    return or;
                }).toList();
            }

            AttributeOptionValueResponse resp = new AttributeOptionValueResponse();
            resp.setId(v.getDefinition().getId());
            resp.setCreatedAt(v.getCreatedAt());
            resp.setUpdatedAt(v.getUpdatedAt());
            resp.setName(v.getDefinition().getName());
            resp.setDataType(type);
            resp.setValue(optionResponses);
            return resp;
        } else {
            AttributeValueResponse resp = new AttributeValueResponse();
            resp.setId(v.getDefinition().getId());
            resp.setCreatedAt(v.getCreatedAt());
            resp.setUpdatedAt(v.getUpdatedAt());
            resp.setName(v.getDefinition().getName());
            resp.setDataType(type);
            if (AttributeValueUtil.isValid(type, v.getValue())) {
                resp.setValue(AttributeValueUtil.parse(type, v.getValue()));
            } else {
                resp.setValue(null);
            }
            return resp;
        }
    }

    private String convertValueToString(AttributeDefinition def, Object value) {
        if (value == null) return null;

        return switch (def.getDataType()) {
            case MULTISELECT -> {
                if (!(value instanceof Collection<?> col)) {
                    throw new InvalidDataException("MULTISELECT requires a collection of option IDs");
                }
                yield col.stream().map(Object::toString).collect(Collectors.joining(","));
            }
            case SELECT -> {
                if (value instanceof Collection<?> col) {
                    if (col.size() != 1) {
                        throw new InvalidDataException("SELECT requires exactly one option ID");
                    }
                    yield col.iterator().next().toString();
                }
                yield String.valueOf(value);
            }
            default -> String.valueOf(value);
        };
    }

    private void validateOptions(AttributeDefinition def, String rawValue) {
        if (rawValue == null) return;

        if (def.getDataType() == AttributeDataType.SELECT) {
            Long optionId = Long.valueOf(rawValue);
            if (!optionValueRepository.existsByIdAndAttributeDefinitionId(optionId, def.getId())) {
                throw new InvalidDataException("Invalid SELECT option ID: " + optionId);
            }
        } else if (def.getDataType() == AttributeDataType.MULTISELECT) {
            List<Long> ids = AttributeValueUtil.parseMultiSelect(rawValue);
            for (Long optionId : ids) {
                if (!optionValueRepository.existsByIdAndAttributeDefinitionId(optionId, def.getId())) {
                    throw new InvalidDataException("Invalid MULTISELECT option ID: " + optionId);
                }
            }
        }
    }

    private List<OptionValueResponse> getOptionResponses(AttributeValue v) {
        AttributeDataType type = v.getDefinition().getDataType();
        String raw = v.getValue();

        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }

        List<Long> optionIds = new ArrayList<>();
        try {
            if (type == AttributeDataType.SELECT) {
                optionIds.add(Long.valueOf(raw.trim()));
            } else if (type == AttributeDataType.MULTISELECT) {
                optionIds.addAll(AttributeValueUtil.parseMultiSelect(raw));
            }
        } catch (Exception ignored) {
            return Collections.emptyList();
        }

        if (optionIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<OptionValue> options = optionValueRepository.findAllById(optionIds);

        return options.stream().map(o -> {
            OptionValueResponse resp = new OptionValueResponse();
            resp.setId(o.getId());
            resp.setLabel(o.getLabel());
            resp.setValue(o.getValue());
            return resp;
        }).toList();
    }
}