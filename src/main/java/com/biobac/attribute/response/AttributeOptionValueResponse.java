package com.biobac.attribute.response;

import com.biobac.attribute.response.OptionValueResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttributeOptionValueResponse extends AttributeDefResponse{
    private List<OptionValueResponse> value;
}
