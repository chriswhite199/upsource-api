package com.github.chriswhite199.upsourceapi.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.chriswhite199.upsourceapi.dto.RpcError;
import com.github.chriswhite199.upsourceapi.dto.RpcException;
import com.github.chriswhite199.upsourceapi.dto.RpcResult;
import com.github.chriswhite199.upsourceapi.dto.enums.RevisionStateEnum;
import com.github.chriswhite199.upsourceapi.dto.messages.*;
import com.github.chriswhite199.upsourceapi.service.UpsourceRPC;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import wiremock.org.apache.http.HttpHeaders;

import java.net.http.HttpClient;

public class UpsourceRpcHttpServiceTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    private UpsourceRPC rpc;

    @Before
    public void setUp() {
        this.rpc = UpsourceRpcHttpService.create(
                HttpClient.newBuilder().build(),
                "http://localhost:" + wireMockRule.port(), "user", "password");
    }

    @Test
    public void shouldTestApiCall() throws JsonProcessingException, RpcException {
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

        Assertions.assertThat(resp.getProject()).hasSize(1);
        Assertions.assertThat(resp.getProject()[0].getProjectId()).isEqualTo("proj1");
        Assertions.assertThat(resp.getProject()[0].getProjectName()).isEqualTo("Sample Project");
    }

    @Test
    public void shouldSerializeEnumsAsNumbers() throws RpcException, JsonProcessingException {
        final var expResp = new RpcResult<>(new RevisionInfoDTO()
                .withState(RevisionStateEnum.Found), null);

        WireMock.stubFor(WireMock.post(WireMock.urlPathEqualTo("/getHeadRevision"))
                .willReturn(WireMock.aResponse()
                        .withBody(new ObjectMapper()
                                .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
                                .writeValueAsBytes(expResp)
                        )));

        final var headRev = rpc.getHeadRevision(new ProjectIdDTO().withProjectId("proj1"));

        Assertions.assertThat(headRev.getState()).isEqualTo(RevisionStateEnum.Found);
    }

    @Test
    public void shouldTestApiCallWithError() throws JsonProcessingException {
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