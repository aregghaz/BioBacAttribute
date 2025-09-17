package com.biobac.attribute.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttributeGroupRequest {
    private String name;
    private String description;
    private List<Long> attributeIds;
}
