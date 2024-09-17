package com.study.resttemplatetest.client;

import com.study.spring6restclient.client.BeerClient;
import com.study.spring6restclient.model.BeerDto;
import com.study.spring6restclient.model.BeerStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * For these tests to run, Authorization Server and Resource Server should be deployed
 */
@SpringBootTest
class BeerClientImplTest {

    @Autowired
    private BeerClient beerClient;

    private BeerDto testBeerDto;

    @BeforeEach
    void setUp() {
        testBeerDto = beerClient.getAllBeers().getContent().getFirst();
    }

    @Test
    void testGetAllBeersWithBeerName() {
        var beerPagedModel = beerClient.getAllBeers("ALE");

        assertThat(beerPagedModel.getContent()).isNotEmpty();
    }

    @Test
    void testGetAllBeersWithNoBeerName() {
        var beerPagedModel = beerClient.getAllBeers(null);

        assertThat(beerPagedModel.getContent()).isNotEmpty();
    }

    @Test
    void testGetBeerById() {
        var beerById = beerClient.getBeerById(testBeerDto.getId());
        assertNotNull(beerById);
    }

    @Test
    void testCreateBeer() {
        var newBeerDto = BeerDto.builder()
                .beerName("Mango Bobs")
                .beerStyle(BeerStyle.IPA)
                .quantityOnHand(323)
                .upc("123321")
                .price(new BigDecimal("10.99"))
                .build();

        var savedDto = beerClient.createBeer(newBeerDto);
        assertNotNull(savedDto);
    }

    @Test
    void testUpdateBeer() {
        var newBeerDto = BeerDto.builder()
                .beerName("Mango Bobs - 2")
                .beerStyle(BeerStyle.IPA)
                .quantityOnHand(323)
                .upc("123321")
                .price(new BigDecimal("10.99"))
                .build();

        var savedDto = beerClient.createBeer(newBeerDto);
        final String newBeerName = "Mango Bobs - 3 - updated";

        savedDto.setBeerName(newBeerName);
        savedDto = beerClient.updateBeer(savedDto);

        assertEquals(newBeerName, savedDto.getBeerName());
    }

    @Test
    void testDeleteBeer() {
        var newBeerDto = BeerDto.builder()
                .beerName("Mango Bobs 2")
                .beerStyle(BeerStyle.IPA)
                .quantityOnHand(323)
                .upc("123321")
                .price(new BigDecimal("10.99"))
                .build();

        var savedDto = beerClient.createBeer(newBeerDto);
        var savedDtoId = savedDto.getId();

        beerClient.deleteBeer(savedDtoId);

        assertThrows(HttpClientErrorException.class, () ->
                beerClient.getBeerById(savedDtoId));
    }
}
