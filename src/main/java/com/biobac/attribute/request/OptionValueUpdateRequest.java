package com.biobac.attribute.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionValueUpdateRequest {
    private Long id;
    private String label;
    private String value;
}
