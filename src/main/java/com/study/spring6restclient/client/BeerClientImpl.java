package com.study.spring6restclient.client;

import com.study.spring6restclient.model.BeerDto;
import com.study.spring6restclient.model.BeerDtoPagedModel;
import com.study.spring6restclient.model.BeerStyle;
import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Service
public class BeerClientImpl implements BeerClient {

    public static final String BEER_PATH = "/api/v1/beer";
    public static final String BEER_ID_PATH = BEER_PATH + "/{beerId}";

    private final RestClient restClient;

    public BeerClientImpl(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public BeerDto getBeerById(UUID beerId) {
        return restClient.get()
                .uri(BEER_ID_PATH, beerId)
                .retrieve()
                .body(BeerDto.class);
    }

    @Override
    public PagedModel<BeerDto> getAllBeers(String beerName, BeerStyle beerStyle, Boolean showInventory,
                                           Integer pageNumber, Integer pageSize) {
        var builtUriString = buildQueryParam(beerName, beerStyle, showInventory, pageNumber, pageSize);

        return restClient.get()
                .uri(builtUriString)
                .retrieve()
                .body(BeerDtoPagedModel.class);
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
        var location = restClient.post()
                .uri(BEER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(beerDto)
                .retrieve()
                .toBodilessEntity()
                .getHeaders()
                .getLocation();

        assert location != null;
        return restClient.get()
                .uri(location.getPath())
                .retrieve()
                .body(BeerDto.class);
    }

    @Override
    public BeerDto updateBeer(BeerDto beerDto, UUID beerId) {
        restClient.put()
                .uri(BEER_ID_PATH, beerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(beerDto)
                .retrieve()
                .toBodilessEntity();

        return this.getBeerById(beerId);
    }

    @Override
    public void deleteBeer(UUID beerId) {
        restClient.delete()
                .uri(BEER_ID_PATH, beerId)
                .retrieve()
                .toBodilessEntity();
    }

    private String buildQueryParam(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize) {
        var uriComponentsBuilder = UriComponentsBuilder.fromPath(BEER_PATH);

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

        return uriComponentsBuilder.toUriString();
    }
}
