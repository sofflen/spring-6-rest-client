package com.study.spring6restclient.client;

import com.study.spring6restclient.model.BeerDto;
import com.study.spring6restclient.model.BeerDtoPagedModel;
import com.study.spring6restclient.model.BeerStyle;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Service
public class BeerClientImpl implements BeerClient {

    public static final String BEER_PATH = "/api/v1/beer";
    public static final String BEER_ID_PATH = BEER_PATH + "/{beerId}";

    private final RestTemplate restTemplate;

    public BeerClientImpl(RestTemplateBuilder restTemplateBuilder) {
        restTemplate = restTemplateBuilder.build();
    }

    @Override
    public BeerDto getBeerById(UUID beerId) {
        return restTemplate.getForObject(BEER_ID_PATH, BeerDto.class, beerId);
    }

    @Override
    public PagedModel<BeerDto> getAllBeers(String beerName, BeerStyle beerStyle, Boolean showInventory,
                                     Integer pageNumber, Integer pageSize) {
        var uriComponentsBuilder = UriComponentsBuilder.fromPath(BEER_PATH);

        buildQueryParam(uriComponentsBuilder, beerName, beerStyle, showInventory, pageNumber, pageSize);

        var pageResponseEntity = restTemplate.getForEntity(uriComponentsBuilder.toUriString(), BeerDtoPagedModel.class);

        return pageResponseEntity.getBody();
    }

    @Override
    public PagedModel<BeerDto> getAllBeers() {
        return getAllBeers(null, null, null, null, null);
    }

    @Override
    public PagedModel<BeerDto> getAllBeers(String beerName) {
        return getAllBeers(beerName, null, null, null, null);
    }

    @Override
    public BeerDto createBeer(BeerDto beerDto) {
        var uri = restTemplate.postForLocation(BEER_PATH, beerDto);

        return restTemplate.getForObject(uri.getPath(), BeerDto.class);
    }

    @Override
    public BeerDto updateBeer(BeerDto newBeerDto) {
        restTemplate.put(BEER_ID_PATH, newBeerDto, newBeerDto.getId());

        return getBeerById(newBeerDto.getId());
    }

    @Override
    public void deleteBeer(UUID beerId) {
        restTemplate.delete(BEER_ID_PATH, beerId);
    }

    private void buildQueryParam(UriComponentsBuilder uriComponentsBuilder, String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize) {
        if (beerName != null) {
            uriComponentsBuilder.queryParam("beerName", beerName);
        }
        if (beerStyle != null) {
            uriComponentsBuilder.queryParam("beerStyle", beerStyle.name());
        }
        if (showInventory != null) {
            uriComponentsBuilder.queryParam("showInventory", showInventory);
        }
        if (pageNumber != null) {
            uriComponentsBuilder.queryParam("pageNumber", pageNumber);
        }
        if (pageSize != null) {
            uriComponentsBuilder.queryParam("pageSize", pageSize);
        }
    }
}
