package com.study.spring6restclient.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;

public class BeerDtoPagedModel extends PagedModel<BeerDto> {
    /**
     * Creates a new {@link PagedModel} for the given {@link Page}.
     *
     * @param page must not be {@literal null}.
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public BeerDtoPagedModel(BeerDtoPage page) {
        super(page);
    }
}
