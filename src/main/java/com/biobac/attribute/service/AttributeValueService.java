package com.biobac.attribute.service;

import com.biobac.attribute.request.AttributeUpsertRequest;
import com.biobac.attribute.response.AttributeDefResponse;

import java.util.List;

public interface AttributeValueService {
    List<AttributeDefResponse> createAttributeValues(Long targetId, String targetType, List<AttributeUpsertRequest> attributes);

    List<AttributeDefResponse> getAttributeValues(Long targetId, String targetType);

    void deleteAttributeValues(Long targetId, String targetType);
}
