package com.biobac.attribute.service.impl;

import com.biobac.attribute.dto.PaginationMetadata;
import com.biobac.attribute.entity.AttributeDataType;
import com.biobac.attribute.entity.AttributeDefinition;
import com.biobac.attribute.entity.AttributeGroup;
import com.biobac.attribute.entity.OptionValue;
import com.biobac.attribute.exception.DuplicateException;
import com.biobac.attribute.exception.InvalidDataException;
import com.biobac.attribute.exception.NotFoundException;
import com.biobac.attribute.mapper.AttributeDefinitionMapper;
import com.biobac.attribute.repository.AttributeDefinitionRepository;
import com.biobac.attribute.repository.AttributeGroupRepository;
import com.biobac.attribute.repository.AttributeValueRepository;
import com.biobac.attribute.repository.OptionValueRepository;
import com.biobac.attribute.request.*;
import com.biobac.attribute.response.AttributeDefResponse;
import com.biobac.attribute.service.AttributeService;
import com.biobac.attribute.utils.specifications.AttributeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeServiceImpl implements AttributeService {

    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final OptionValueRepository optionValueRepository;
    private final AttributeGroupRepository attributeGroupRepository;
    private final AttributeDefinitionMapper attributeDefinitionMapper;

    @Override
    @Transactional(readOnly = true)
    public AttributeDefResponse getById(Long id) {
        AttributeDefinition def = attributeDefinitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attribute definition not found"));
        return attributeDefinitionMapper.toDetailedDto(def);
    }

    @Override
    @Transactional
    public AttributeDefResponse createAttributeDefinition(AttributeDefRequest request) {
        if (attributeDefinitionRepository.findAttributeDefinitionByName(request.getName()) != null) {
            throw new DuplicateException("Attribute with name '" + request.getName() + "' already exists");
        }

        AttributeDefinition attribute = new AttributeDefinition();
        attribute.setName(request.getName());
        attribute.setDataType(request.getDataType());

        Set<AttributeGroup> groups = Optional.ofNullable(request.getAttributeGroupIds())
                .orElse(Collections.emptyList())
                .stream()
                .map(id -> attributeGroupRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Attribute group not found: " + id)))
                .collect(Collectors.toSet());
        attribute.setGroups(groups);

        attribute = attributeDefinitionRepository.save(attribute);

        if (EnumSet.of(AttributeDataType.SELECT, AttributeDataType.MULTISELECT, AttributeDataType.RADIO)
                .contains(request.getDataType())) {
            if (request.getOptions() == null || request.getOptions().isEmpty()) {
                throw new InvalidDataException("Options are required for SELECT, RADIO and MULTISELECT attributes");
            }
            applyOptions(attribute, request.getOptions());
        }

        return attributeDefinitionMapper.toDetailedDto(attribute);
    }

    @Override
    @Transactional
    public AttributeDefResponse updateAttributeDefinition(Long id, AttributeDefUpdateRequest request) {
        AttributeDefinition attribute = attributeDefinitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attribute definition not found"));

        boolean hasValues = attributeValueRepository.existsByDefinition(attribute);

        if (!hasValues) {
            Optional.ofNullable(request.getName()).filter(n -> !n.isBlank()).ifPresent(attribute::setName);
            Optional.ofNullable(request.getDataType()).ifPresent(attribute::setDataType);
        } else {
            if (request.getName() != null && !request.getName().equals(attribute.getName()))
                throw new InvalidDataException("Cannot change attribute name because values exist");
            if (request.getDataType() != null && request.getDataType() != attribute.getDataType())
                throw new InvalidDataException("Cannot change attribute data type because values exist");
        }

        if (request.getAttributeGroupIds() != null) {
            Set<AttributeGroup> groups = request.getAttributeGroupIds().stream()
                    .map(idVal -> attributeGroupRepository.findById(idVal)
                            .orElseThrow(() -> new NotFoundException("Attribute group not found: " + idVal)))
                    .collect(Collectors.toSet());
            attribute.setGroups(groups);
        }

        if (EnumSet.of(AttributeDataType.SELECT, AttributeDataType.MULTISELECT, AttributeDataType.RADIO)
                .contains(attribute.getDataType()) && request.getOptions() != null) {
            updateAttributeOptions(attribute, request.getOptions());
        }

        AttributeDefinition saved = attributeDefinitionRepository.save(attribute);
        return attributeDefinitionMapper.toDetailedDto(saved);
    }

    private void updateAttributeOptions(AttributeDefinition attribute, List<OptionValueUpdateRequest> optionsRequest) {
        Map<Long, OptionValue> existingOptions = attribute.getOptions().stream()
                .collect(Collectors.toMap(OptionValue::getId, Function.identity()));

        for (OptionValueUpdateRequest optReq : optionsRequest) {
            if (optReq.getId() == null) {
                OptionValue newOpt = new OptionValue();
                newOpt.setLabel(optReq.getLabel());
                newOpt.setValue(optReq.getValue());
                newOpt.setAttributeDefinition(attribute);
                optionValueRepository.save(newOpt);
                attribute.getOptions().add(newOpt);
            } else {
                OptionValue existingOpt = existingOptions.get(optReq.getId());
                if (existingOpt == null) {
                    throw new NotFoundException("Option not found: " + optReq.getId());
                }

                boolean optionUsed = attributeValueRepository.existsByDefinitionAndOption(attribute, existingOpt);
                if (optionUsed) {
                    if (!Objects.equals(existingOpt.getLabel(), optReq.getLabel()) ||
                            !Objects.equals(existingOpt.getValue(), optReq.getValue())) {
                        throw new InvalidDataException("Cannot modify option in use: " + existingOpt.getLabel());
                    }
                } else {
                    existingOpt.setLabel(optReq.getLabel());
                    existingOpt.setValue(optReq.getValue());
                }

                existingOptions.remove(optReq.getId());
            }
        }

        for (OptionValue remaining : existingOptions.values()) {
            boolean optionUsed = attributeValueRepository.existsByDefinitionAndOption(attribute, remaining);
            if (optionUsed) {
                throw new InvalidDataException("Cannot delete option in use: " + remaining.getLabel());
            }
            optionValueRepository.delete(remaining);
        }
    }

    @Override
    @Transactional
    public void deleteAttributeDefinition(Long id) {
        AttributeDefinition attribute = attributeDefinitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attribute definition not found"));

        if (attributeValueRepository.existsByDefinition(attribute)) {
            throw new InvalidDataException("Cannot delete attribute definition because values exist");
        }

        if (EnumSet.of(AttributeDataType.SELECT, AttributeDataType.MULTISELECT)
                .contains(attribute.getDataType()) && attribute.getOptions() != null) {
            optionValueRepository.deleteAll(attribute.getOptions());
        }

        attributeDefinitionRepository.delete(attribute);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeDefResponse> getDefinitionsByGroups(List<Long> attributeGroupIds) {
        List<AttributeDefinition> defs = (attributeGroupIds == null || attributeGroupIds.isEmpty())
                ? attributeDefinitionRepository.findAll()
                : attributeDefinitionRepository.findByGroups(attributeGroupIds);

        return defs.stream()
                .map(attributeDefinitionMapper::toDetailedDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<AttributeDefResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<AttributeDefinition> spec = AttributeSpecification.buildSpecification(filters);

        Page<AttributeDefinition> pageResult = attributeDefinitionRepository.findAll(spec, pageable);

        List<AttributeDefResponse> content = pageResult.getContent().stream()
                .map(attributeDefinitionMapper::toDetailedDto)
                .toList();

        PaginationMetadata metadata = new PaginationMetadata(
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse("id"),
                "attributeDefinitionTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<AttributeDataType> getAttributeDataTypes() {
        return EnumSet.allOf(AttributeDataType.class);
    }

    private Pageable buildPageable(Integer page, Integer size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(Optional.ofNullable(sortDir).orElse("ASC")), Optional.ofNullable(sortBy).orElse("id"));
        return PageRequest.of(Optional.ofNullable(page).orElse(0), Optional.ofNullable(size).orElse(20), sort);
    }

    private void applyOptions(AttributeDefinition def, List<OptionValueRequest> options) {
        if (def == null) return;
        if (options == null) return;
        def.getOptions().clear();
        for (OptionValueRequest o : options) {
            String label = o.getLabel();
            String value = o.getValue();
            if (label == null || label.isEmpty()) {
                throw new InvalidDataException("Option label is required");
            }
            if (value == null || value.isEmpty()) {
                value = label;
            }
            OptionValue ov = new OptionValue();
            ov.setLabel(label);
            ov.setValue(value);
            ov.setAttributeDefinition(def);
            def.getOptions().add(ov);
            optionValueRepository.save(ov);
        }
    }
}
