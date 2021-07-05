package ru.stm.http2rpc.inside.proxy;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.util.Set;

@Getter
@Setter
@ConfigurationProperties(prefix = "stm.proxy")
@Component
public class ProxyConfigurationProperties {

    private SimpleStreamWebConfig webConfig = new SimpleStreamWebConfig();
    private boolean logBodyResponseInTrace = false;

    private String proxyUrl;

    private String proxyPattern = "http://[^/]+";
    private String proxyPatternHttps = "https://[^/]+";

    private Set<String> deleteHeaders = Set.of("Host", "Content-Length", "content-length");

    @Data
    public static class SimpleStreamWebConfig {
        /**
         * Размер буфера клиента. Может потребоваться для большой ленты чеков...
         */
        private DataSize clientBufferSize = DataSize.ofMegabytes(10);

        /**
         * Время ожидания коннекта
         */
        private Duration connectTimeout = Duration.ofSeconds(5);

        /**
         * Время ожидания ответа
         */
        private Duration readTimeout = Duration.ofSeconds(55);

        /**
         * Включить подробное логгирование HTTP трафика
         */
        private boolean wireTrapEnabled = false;

    }
}
