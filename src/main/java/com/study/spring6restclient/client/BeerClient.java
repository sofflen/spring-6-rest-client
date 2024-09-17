package com.study.spring6restclient.client;

import com.study.spring6restclient.model.BeerDto;
import com.study.spring6restclient.model.BeerStyle;
import org.springframework.data.web.PagedModel;

import java.util.UUID;

public interface BeerClient {

    BeerDto getBeerById(UUID beerId);

    PagedModel<BeerDto> getAllBeers();

    PagedModel<BeerDto> getAllBeers(String beerName);

    PagedModel<BeerDto> getAllBeers(String beerName, BeerStyle beerStyle, Boolean showInventory,
                              Integer pageNumber, Integer pageSize);

    BeerDto createBeer(BeerDto beerDto);

    BeerDto updateBeer(BeerDto newBeerDto);

    void deleteBeer(UUID beerId);
}
