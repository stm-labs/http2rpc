package ru.stm.http2rpc.dmz.controller;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Component
@Data
@ConfigurationProperties(prefix = "stm.proxy")
@Valid
public class ProxyDmzConfigurationProperties {

    @NotNull
    private String systemCode;

}
