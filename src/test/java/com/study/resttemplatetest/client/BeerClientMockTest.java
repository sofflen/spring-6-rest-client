package com.study.resttemplatetest.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.spring6restclient.client.BeerClient;
import com.study.spring6restclient.client.BeerClientImpl;
import com.study.spring6restclient.config.OAuthClientInterceptor;
import com.study.spring6restclient.config.RestTemplateBuilderConfig;
import com.study.spring6restclient.model.BeerDto;
import com.study.spring6restclient.model.BeerDtoPage;
import com.study.spring6restclient.model.BeerDtoPagedModel;
import com.study.spring6restclient.model.BeerStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.study.spring6restclient.client.BeerClientImpl.BEER_ID_PATH;
import static com.study.spring6restclient.client.BeerClientImpl.BEER_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withAccepted;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


@RestClientTest(BeerClientImpl.class)
@Import(RestTemplateBuilderConfig.class)
class BeerClientMockTest {

    static final String URL = "http://localhost:8080";
    static final String BEARER_TEST = "Bearer test";
    static final String REG_ID = "auth-server";

    @Autowired
    BeerClient beerClient;
    @Autowired
    MockRestServiceServer mockRestServiceServer;
    @Autowired
    RestTemplateBuilder restTemplateBuilder;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ClientRegistrationRepository registrationRepository;

    @MockBean
    OAuth2AuthorizedClientManager authorizedClientManager;

    BeerDto testDto;
    UUID testDtoId;
    String testDtoJsonString;
    ClientRegistration testRegistration;
    OAuth2AccessToken accessToken;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        testRegistration = registrationRepository.findByRegistrationId(REG_ID);
        accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "test",
                Instant.now(), Instant.now().plusSeconds(60L));

        when(authorizedClientManager.authorize(any()))
                .thenReturn(new OAuth2AuthorizedClient(testRegistration, "test", accessToken));

        testDto = getBeerDto();
        testDtoId = testDto.getId();
        testDtoJsonString = objectMapper.writeValueAsString(testDto);
    }

    @Test
    void testGetAllBeers() throws JsonProcessingException {
        var response = objectMapper.writeValueAsString(getPagedModel());

        mockRestServiceServer.expect(
                        method(HttpMethod.GET))
                .andExpect(header(AUTHORIZATION, BEARER_TEST))
                .andExpect(requestTo(URL + BEER_PATH))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        var dtosPage = beerClient.getAllBeers();
        assertThat(dtosPage.getContent()).isNotEmpty();

        mockRestServiceServer.verify();
    }

    @Test
    void testGetAllBeersWithQueryParam() throws JsonProcessingException {
        var response = objectMapper.writeValueAsString(getPagedModel());
        String queryName = "beerName";
        String queryValue = "ALE";

        var uri = UriComponentsBuilder.fromUriString(URL + BEER_PATH)
                .queryParam(queryName, queryValue)
                .build().toUri();

        mockRestServiceServer.expect(
                        method(HttpMethod.GET))
                .andExpect(header(AUTHORIZATION, BEARER_TEST))
                .andExpect(requestTo(uri))
                .andExpect(queryParam(queryName, queryValue))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        var responsePage = beerClient.getAllBeers(queryValue);

        assertThat(responsePage.getContent()).isNotEmpty();

        mockRestServiceServer.verify();
    }

    @Test
    void testGetBeerById() {
        mockGetByIdRequest();

        var responseDto = beerClient.getBeerById(testDtoId);
        assertThat(responseDto.getId()).isEqualTo(testDtoId);

        mockRestServiceServer.verify();
    }

    @Test
    void testCreateBeer() {
        var uri = UriComponentsBuilder.fromPath(BEER_ID_PATH).build(testDtoId);

        mockRestServiceServer.expect(
                        method(HttpMethod.POST))
                .andExpect(header(AUTHORIZATION, BEARER_TEST))
                .andExpect(requestTo(URL + BEER_PATH))
                .andRespond(withAccepted().location(uri));

        mockGetByIdRequest();

        var responseDto = beerClient.createBeer(testDto);
        assertThat(responseDto.getId()).isEqualTo(testDtoId);

        mockRestServiceServer.verify();
    }

    @Test
    void testUpdateBeer() {
        mockRestServiceServer.expect(
                        method(HttpMethod.PUT))
                .andExpect(header(AUTHORIZATION, BEARER_TEST))
                .andExpect(requestToUriTemplate(URL + BEER_ID_PATH, testDtoId))
                .andRespond(withNoContent());

        mockGetByIdRequest();

        var responseDto = beerClient.updateBeer(testDto);
        assertThat(responseDto.getId()).isEqualTo(testDtoId);

        mockRestServiceServer.verify();
    }

    @Test
    void testDeleteBeer() {
        mockRestServiceServer.expect(
                        method(HttpMethod.DELETE))
                .andExpect(header(AUTHORIZATION, BEARER_TEST))
                .andExpect(requestToUriTemplate(URL + BEER_ID_PATH, testDtoId))
                .andRespond(withNoContent());

        beerClient.deleteBeer(testDtoId);

        mockRestServiceServer.verify();
    }

    @Test
    void testDeleteNonExistingBeer() {
        mockRestServiceServer.expect(
                        method(HttpMethod.DELETE))
                .andExpect(header(AUTHORIZATION, BEARER_TEST))
                .andExpect(requestToUriTemplate(URL + BEER_ID_PATH, testDtoId))
                .andRespond(withResourceNotFound());

        assertThrows(HttpClientErrorException.class,
                () -> beerClient.deleteBeer(testDtoId));

        mockRestServiceServer.verify();
    }

    private BeerDto getBeerDto() {
        return BeerDto.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("10.99"))
                .beerName("Mango Bobs")
                .beerStyle(BeerStyle.IPA)
                .quantityOnHand(333)
                .upc("123321")
                .build();
    }

    private BeerDtoPagedModel getPagedModel() {
        return new BeerDtoPagedModel(
                new BeerDtoPage(
                        List.of(getBeerDto()),
                        Map.of(
                                "number", 1,
                                "size", 25,
                                "totalElements", 1
                        )));
    }

    private void mockGetByIdRequest() {
        mockRestServiceServer.expect(
                        method(HttpMethod.GET))
                .andExpect(header(AUTHORIZATION, BEARER_TEST))
                .andExpect(requestToUriTemplate(URL + BEER_ID_PATH, testDtoId))
                .andRespond(withSuccess(testDtoJsonString, MediaType.APPLICATION_JSON));
    }

    @TestConfiguration
    public static class TestConfig {
        @Bean
        ClientRegistrationRepository clientRegistrationRepository() {
            return new InMemoryClientRegistrationRepository(ClientRegistration
                    .withRegistrationId(REG_ID)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .clientId("test")
                    .tokenUri("test")
                    .build());
        }

        @Bean
        OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
            return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
        }

        @Bean
        OAuthClientInterceptor oauthClientInterceptor(OAuth2AuthorizedClientManager authorizedClientManager,
                                                      ClientRegistrationRepository clientRegistrationRepository) {
            return new OAuthClientInterceptor(authorizedClientManager, clientRegistrationRepository);
        }
    }
}
