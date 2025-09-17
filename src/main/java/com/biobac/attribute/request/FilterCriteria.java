package com.biobac.attribute.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterCriteria {
    private String operator;
    private Object value;
}
