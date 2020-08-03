package com.github.chriswhite199.upsourceapi.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chriswhite199.upsourceapi.dto.messages.ProjectIdListDTO;
import com.github.chriswhite199.upsourceapi.dto.messages.ShortProjectInfoDTO;
import com.github.chriswhite199.upsourceapi.dto.messages.ShortProjectInfoListDTO;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import wiremock.org.apache.http.HttpHeaders;

import java.net.http.HttpClient;

public class UpsourceRpcHttpServiceTest {
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(0);

  @Test
  public void shouldTestApiCall() throws JsonProcessingException {
    final var rpc = UpsourceRpcHttpService.create(
            HttpClient.newBuilder().build(),
            "http://localhost:" + wireMockRule.port(), "user", "password");

    final var expectedResp = new ShortProjectInfoListDTO()
            .withProject(new ShortProjectInfoDTO[]{
                    new ShortProjectInfoDTO()
                            .withProjectId("proj1")
                            .withProjectName("Sample Project")
            });

    WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/getAllProjects"))
            .withBasicAuth("user", "password")
            .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo("application/json"))
            .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo("application/json"))
            .willReturn(
                    WireMock.aResponse()
                            .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                            .withBody(new ObjectMapper().writeValueAsBytes(expectedResp))
            ));

    final var resp = rpc.getAllProjects(new ProjectIdListDTO());

    Assertions.assertThat(resp.project).hasSize(1);
    Assertions.assertThat(resp.project[0].projectId).isEqualTo("proj1");
    Assertions.assertThat(resp.project[0].projectName).isEqualTo("Sample Project");
  }
}