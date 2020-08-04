package com.github.chriswhite199.upsourceapi.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chriswhite199.upsourceapi.dto.RpcException;
import com.github.chriswhite199.upsourceapi.dto.RpcResult;
import com.github.chriswhite199.upsourceapi.service.UpsourceRPC;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class UpsourceRpcHttpService implements InvocationHandler {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final HttpClient httpClient;
  private final String baseUrl;
  private final String username;
  private final String password;

  protected UpsourceRpcHttpService(HttpClient httpClient, String baseUrl, String username, String password) {
    this.httpClient = httpClient;
    this.baseUrl = baseUrl;
    this.username = username;
    this.password = password;
  }

  public static UpsourceRPC create(HttpClient httpClient, String baseUrl, String username, String password) {
    return (UpsourceRPC) Proxy.newProxyInstance(
            UpsourceRpcHttpService.class.getClassLoader(),
            new Class[]{UpsourceRPC.class},
            new UpsourceRpcHttpService(httpClient, baseUrl, username, password));
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    final var httpReq = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofByteArray(objectMapper.writeValueAsBytes(args[0])))
            .uri(new URI(this.baseUrl + "/" + method.getName()))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Authorization",
                    "Basic " + Base64.getEncoder().encodeToString(
                            String.format("%s:%s", this.username, this.password).getBytes()))
            .build();

    final var httpResp = this.httpClient.send(httpReq, HttpResponse.BodyHandlers.ofByteArray());

    final var resultWrapper = objectMapper.readValue(httpResp.body(), new TypeReference<RpcResult<Object>>() {
    });

    if (resultWrapper.error != null) {
      throw new RpcException(resultWrapper.error);
    } else {
      return objectMapper.convertValue(resultWrapper.result, method.getReturnType());
    }
  }
}
