package com.biobac.attribute.service;

import com.biobac.attribute.dto.PaginationMetadata;
import com.biobac.attribute.entity.AttributeDataType;
import com.biobac.attribute.request.AttributeDefRequest;
import com.biobac.attribute.request.AttributeDefUpdateRequest;
import com.biobac.attribute.request.FilterCriteria;
import com.biobac.attribute.response.AttributeDefResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AttributeService {
    AttributeDefResponse getById(Long id);
    AttributeDefResponse createAttributeDefinition(AttributeDefRequest request);

    AttributeDefResponse updateAttributeDefinition(Long id, AttributeDefUpdateRequest request);

    void deleteAttributeDefinition(Long id);

    List<AttributeDefResponse> getDefinitionsByGroups(List<Long> attributeGroupIds);

    Pair<List<AttributeDefResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                       Integer page,
                                                                       Integer size,
                                                                       String sortBy,
                                                                       String sortDir);

    Set<AttributeDataType> getAttributeDataTypes();
}
