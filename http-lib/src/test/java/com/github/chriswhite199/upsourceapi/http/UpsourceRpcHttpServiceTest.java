package com.github.chriswhite199.upsourceapi.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chriswhite199.upsourceapi.dto.RpcError;
import com.github.chriswhite199.upsourceapi.dto.RpcException;
import com.github.chriswhite199.upsourceapi.dto.RpcResult;
import com.github.chriswhite199.upsourceapi.dto.messages.*;
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
  public void shouldTestApiCall() throws JsonProcessingException, RpcException {
    final var rpc = UpsourceRpcHttpService.create(
            HttpClient.newBuilder().build(),
            "http://localhost:" + wireMockRule.port(), "user", "password");

    final var expectedResp = new RpcResult<>(new ShortProjectInfoListDTO()
            .withProject(new ShortProjectInfoDTO[]{
                    new ShortProjectInfoDTO()
                            .withProjectId("proj1")
                            .withProjectName("Sample Project")
            }), null);

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

  @Test
  public void shouldTestApiCallWithError() throws JsonProcessingException {
    final var rpc = UpsourceRpcHttpService.create(
            HttpClient.newBuilder().build(),
            "http://localhost:" + wireMockRule.port(), "user", "password");

    final var expectedResp = new RpcResult<DiscussionInFileDTO>(null,
            new RpcError(108, "Internal error: ..."));

    WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/createDiscussion"))
            .willReturn(
                    WireMock.aResponse()
                            .withStatus(500)
                            .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                            .withBody(new ObjectMapper().writeValueAsBytes(expectedResp))
            ));

    Assertions.assertThatThrownBy(() -> rpc.createDiscussion(new CreateDiscussionRequestDTO()))
            .isInstanceOf(RpcException.class)
            .hasMessage("RPC Error: code=108, message=Internal error: ...");
  }
}