package com.biobac.attribute.service;

import com.biobac.attribute.dto.PaginationMetadata;
import com.biobac.attribute.request.AttributeGroupRequest;
import com.biobac.attribute.request.FilterCriteria;
import com.biobac.attribute.response.AttributeGroupResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface AttributeGroupService {

    @Transactional(readOnly = true)
    List<AttributeGroupResponse> getAll();

    @Transactional(readOnly = true)
    Pair<List<AttributeGroupResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                         Integer page,
                                                                         Integer size,
                                                                         String sortBy,
                                                                         String sortDir);

    @Transactional(readOnly = true)
    AttributeGroupResponse getById(Long id);

    @Transactional
    AttributeGroupResponse create(AttributeGroupRequest group);

    @Transactional
    AttributeGroupResponse update(Long id, AttributeGroupRequest group);

    @Transactional
    void delete(Long id);
}
