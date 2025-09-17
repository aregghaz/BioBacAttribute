package com.biobac.attribute.request;

import com.biobac.attribute.entity.AttributeDataType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttributeDefRequest {
    private List<Long> attributeGroupIds;
    private String name;
    private AttributeDataType dataType;
    private List<OptionValueRequest> options;
}
