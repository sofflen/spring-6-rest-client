package com.study.spring6restclient.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true, value = "pageable")
public class BeerDtoPage extends PageImpl<BeerDto> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public BeerDtoPage(@JsonProperty("content") List<BeerDto> content,
                       @JsonProperty("page") Map<String, Integer> pageMap) {
        super(content, PageRequest.of(pageMap.get("number"), pageMap.get("size")), pageMap.get("totalElements"));
    }
}
