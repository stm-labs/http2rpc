package ru.stm.dr.http2rpcapi.rpc.metrics.proxy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.stm.rpc.types.RpcRequest;

import javax.validation.Valid;

@Data
@Valid
@AllArgsConstructor
@NoArgsConstructor
public class ProxyRequestRpcRequest implements RpcRequest {

    private String uri;
    private String method;
    private ProxyRequestHeaders headers;
    private byte[] body;

}
