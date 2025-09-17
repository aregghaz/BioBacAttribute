package com.biobac.attribute.dto;

import com.biobac.attribute.request.FilterCriteria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginationMetadata {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private Map<String, FilterCriteria> filter;
    private String sortDir;
    private String sortBy;
    private String table;
}
