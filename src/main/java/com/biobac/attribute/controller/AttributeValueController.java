package com.biobac.attribute.controller;

import com.biobac.attribute.request.AttributeUpsertRequest;
import com.biobac.attribute.response.ApiResponse;
import com.biobac.attribute.response.AttributeDefResponse;
import com.biobac.attribute.service.AttributeValueService;
import com.biobac.attribute.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attribute-value")
@RequiredArgsConstructor
public class AttributeValueController {
    private final AttributeValueService attributeValueService;

    @PostMapping
    public ApiResponse<List<AttributeDefResponse>> createValues(
            @RequestParam Long targetId,
            @RequestParam String targetType,
            @RequestBody List<AttributeUpsertRequest> attributes
    ) {
        List<AttributeDefResponse> updatedAttributes =
                attributeValueService.createAttributeValues(targetId, targetType, attributes);

        return ResponseUtil.success("Attribute values saved successfully", updatedAttributes);
    }

    @PutMapping
    public ApiResponse<List<AttributeDefResponse>> updateValues(
            @RequestParam Long targetId,
            @RequestParam String targetType,
            @RequestParam List<Long> attributeGroupIds,
            @RequestBody List<AttributeUpsertRequest> attributes
    ) {
        List<AttributeDefResponse> updatedAttributes =
                attributeValueService.updateAttributeValues(targetId, targetType, attributes, attributeGroupIds);

        return ResponseUtil.success("Attribute values updated successfully", updatedAttributes);
    }

    @GetMapping("/{targetId}/{targetType}")
    public ApiResponse<List<AttributeDefResponse>> getValues(@PathVariable Long targetId, @PathVariable String targetType) {
        List<AttributeDefResponse> attributes = attributeValueService.getAttributeValues(targetId, targetType);
        return ResponseUtil.success("Attribute values retrieved successfully", attributes);
    }

    @DeleteMapping("/{targetId}/{targetType}")
    public ApiResponse<String> deleteValues(@PathVariable Long targetId, @PathVariable String targetType) {
        attributeValueService.deleteAttributeValues(targetId, targetType);
        return ResponseUtil.success("Attribute values deleted successfully");
    }
}
