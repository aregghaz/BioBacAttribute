package com.biobac.attribute.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttributeValueResponse extends AttributeDefResponse {
    private Object value;
}
