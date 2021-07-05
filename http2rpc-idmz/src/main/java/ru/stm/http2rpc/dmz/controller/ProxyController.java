package ru.stm.http2rpc.dmz.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.stm.dr.http2rpcapi.rpc.Http2rpcRpcService;
import ru.stm.dr.http2rpcapi.rpc.metrics.proxy.ProxyRequestHeaders;
import ru.stm.dr.http2rpcapi.rpc.metrics.proxy.ProxyRequestRpcRequest;
import ru.stm.dr.http2rpcapi.rpc.metrics.proxy.ProxyRequestRpcResponse;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProxyController {

    private final Http2rpcRpcService http2rpcRpcService;

    @RequestMapping("/**")
    public Mono<ResponseEntity<byte[]>> redirect(ServerHttpRequest request) {
        return mapRequest(request).map(r -> ResponseEntity.status(
                HttpStatus.valueOf(r.getHttpCode()))
                .headers(new HttpHeaders(r.getHeaders()))
                .body(r.getBody()));
    }

    private Mono<ProxyRequestRpcResponse> mapRequest(ServerHttpRequest serverHttpRequest) {
        return DataBufferUtils.join(serverHttpRequest.getBody()).map(buffer -> {
            try {
                return StreamUtils.copyToByteArray(buffer.asInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).map(body -> {
            String uri = serverHttpRequest.getURI().toString();
            String method = Objects.requireNonNull(serverHttpRequest.getMethod()).name();
            HttpHeaders headers = serverHttpRequest.getHeaders();

            ProxyRequestRpcRequest request = new ProxyRequestRpcRequest();
            request.setBody(body);
            request.setHeaders(new ProxyRequestHeaders(headers));
            request.setMethod(method);
            request.setUri(uri);

            return request;
        }).switchIfEmpty(Mono.fromCallable(() -> {
            String uri = serverHttpRequest.getURI().toString();
            String method = Objects.requireNonNull(serverHttpRequest.getMethod()).name();
            HttpHeaders headers = serverHttpRequest.getHeaders();
            return new ProxyRequestRpcRequest(uri, method, new ProxyRequestHeaders(headers), null);
        })).flatMap(x -> http2rpcRpcService.proxy(x));
    }

}