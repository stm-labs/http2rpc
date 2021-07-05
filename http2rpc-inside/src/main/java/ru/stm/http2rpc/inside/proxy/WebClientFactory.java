package ru.stm.http2rpc.inside.proxy;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

/**
 * Помошник по созданию {@link org.springframework.web.reactive.function.client.WebClient}
 * В зависимости от настроек включает прокси
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebClientFactory {

    private final WebClientFactoryProperties props;

    public WebClient.Builder builder() {
        WebClient.Builder builder;
        if (props.proxyEnabled) {
            var httpClient = HttpClient.create()
                    .tcpConfiguration(tcpClient ->
                            tcpClient.proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP).host(props.proxyHost).port(props.proxyPort)));
            var connector = new ReactorClientHttpConnector(httpClient);
            builder = WebClient.builder()
                    .clientConnector(connector);
        } else {
            builder = WebClient.builder();
        }
        return builder;
    }

    @Component
    @ConfigurationProperties(prefix = "kktreg.webclient")
    @Data
    public static class WebClientFactoryProperties {
        /**
         * <p>Включает проксирование всех запросов. Используется исключительно для разработки</p>
         * <p>Например можно включить Fiddler для просмотра всех исходящих http запросов</p>
         * <b>Не забудь включить сам прокси ))</b>
         */
        private boolean proxyEnabled = false;
        private String proxyHost = "127.0.0.1";
        private int proxyPort = 8888;
    }

}
