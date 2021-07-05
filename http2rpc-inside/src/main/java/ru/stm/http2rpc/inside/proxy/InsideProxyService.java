package ru.stm.http2rpc.inside.proxy;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import ru.stm.dr.http2rpcapi.rpc.metrics.proxy.ProxyRequestHeaders;
import ru.stm.dr.http2rpcapi.rpc.metrics.proxy.ProxyRequestRpcRequest;
import ru.stm.dr.http2rpcapi.rpc.metrics.proxy.ProxyRequestRpcResponse;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

@Service
@Slf4j
public class InsideProxyService {

    private final Pattern addressPattern;
    private final Pattern addressPatternHttps;
    private final ProxyConfigurationProperties webConfigurationProperties;

    private final WebClientFactory webClientFactory;
    private final WebClient webClient;

    public InsideProxyService(ProxyConfigurationProperties webConfigurationProperties,
                              WebClientFactory webClientFactory) {
        this.webConfigurationProperties = webConfigurationProperties;

        this.webClientFactory = webClientFactory;
        this.webClient = simpleStreamClient();
        this.addressPattern = Pattern.compile(webConfigurationProperties.getProxyPattern());
        this.addressPatternHttps = Pattern.compile(webConfigurationProperties.getProxyPatternHttps());
    }

    public WebClient simpleStreamClient() {
        Assert.notNull(Strings.trimToNull(webConfigurationProperties.getProxyUrl()),
                "Не задан base url до рестов");
        log.info("Настраиваем Web Client для подключения к рест методу{}",
                webConfigurationProperties.getProxyUrl());
        var httpClient = HttpClient.create()
                .wiretap(webConfigurationProperties.getWebConfig().isWireTrapEnabled())
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(webConfigurationProperties.getWebConfig().getReadTimeout().toMillis(),
                                TimeUnit.MILLISECONDS)))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        (int) webConfigurationProperties.getWebConfig().getConnectTimeout().toMillis());
        var connector = new ReactorClientHttpConnector(httpClient);
        return webClientFactory.builder()
                .baseUrl(webConfigurationProperties.getProxyUrl())
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize((int) webConfigurationProperties.getWebConfig().getClientBufferSize().toBytes()))
                .clientConnector(connector)
                .build();
    }

    public Mono<ProxyRequestRpcResponse> doProxy(ProxyRequestRpcRequest request) {
        return Mono.defer(() -> {

            if (request.getMethod() == null) {
                return errorResponse(405);
            }

            var httpRequestHeadersAtomicReference = new AtomicReference<HttpHeaders>();

            var newUri = addressPattern.matcher(request.getUri()).replaceFirst(webConfigurationProperties.getProxyUrl());
            newUri = addressPatternHttps.matcher(newUri).replaceFirst(webConfigurationProperties.getProxyUrl());

            var uriBuilder = UriComponentsBuilder.fromUriString(newUri);
            var uri = uriBuilder.build(true).toUri();

            return webClient
                    .method(Objects.requireNonNull(HttpMethod.resolve(request.getMethod())))
                    .uri(uri).headers(httpHeaders -> {
                        httpHeaders.addAll(request.getHeaders());

                        webConfigurationProperties.getDeleteHeaders().forEach(httpHeaders::remove);
                        httpRequestHeadersAtomicReference.set(httpHeaders);

                    }).body(request.getBody() != null ? BodyInserters.fromValue(request.getBody()) : BodyInserters.empty())
                    .exchangeToMono(res -> res.toEntity(byte[].class))
                    .map(x -> {
                        ProxyRequestRpcResponse response = new ProxyRequestRpcResponse();

                        response.setHttpCode(x.getStatusCodeValue());
                        response.setHeaders(new ProxyRequestHeaders(x.getHeaders()));
                        response.setBody(x.getBody());

                        if (log.isTraceEnabled()) {
                            log.trace("Request {} uri={} headers={} |||| Response code={} headers={} body={}",
                                    uri, request.getMethod(),
                                    httpRequestHeadersAtomicReference.get(),
                                    x.getStatusCodeValue(),
                                    response.getHeaders(),
                                    (x.getBody() == null || !webConfigurationProperties.isLogBodyResponseInTrace()) ? null : new String(x.getBody()));
                        }

                        return response;
                    });
        });
    }

    private static Mono<ProxyRequestRpcResponse> errorResponse(int code) {
        ProxyRequestRpcResponse response = new ProxyRequestRpcResponse();
        response.setHttpCode(code);
        response.setHeaders(new ProxyRequestHeaders());
        return Mono.just(response);
    }
}
