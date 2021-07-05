package ru.stm.dr.http2rpcapi.rpc.metrics.proxy;

import lombok.Data;
import lombok.ToString;
import ru.stm.rpc.types.RpcResultType;

@Data
@ToString(exclude = "body")
public class ProxyRequestRpcResponse implements RpcResultType {

    private ProxyRequestHeaders headers;

    private byte[] body;

    private int httpCode;

}
