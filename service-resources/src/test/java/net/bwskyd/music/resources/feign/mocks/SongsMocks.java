package net.bwskyd.music.resources.feign.mocks;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class SongsMocks {

    public static void setupMockBooksResponse(WireMockServer mockService) {
        mockService.stubFor(WireMock.post(WireMock.urlEqualTo("/songs"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"id\": \"1\"}")));

        mockService.stubFor(WireMock.delete(WireMock.urlMatching("/songs.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"ids\": [\"1\", \"2\", \"3\"]}")));
    }

}