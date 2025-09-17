package com.biobac.attribute.response;

import com.biobac.attribute.entity.AttributeDataType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AttributeDefResponse {
    private Long id;
    private String name;
    private AttributeDataType dataType;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
