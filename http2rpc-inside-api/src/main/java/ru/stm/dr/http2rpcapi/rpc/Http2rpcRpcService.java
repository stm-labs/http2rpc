package ru.stm.dr.http2rpcapi.rpc;

import reactor.core.publisher.Mono;
import ru.stm.dr.http2rpcapi.rpc.metrics.proxy.*;
import ru.stm.rpc.kafkaredis.ann.RemoteInterface;
import ru.stm.rpc.kafkaredis.ann.RemoteMethod;

@RemoteInterface
public interface Http2rpcRpcService {

    @RemoteMethod("Запроксировать метод")
    Mono<ProxyRequestRpcResponse> proxy(ProxyRequestRpcRequest proxy);

}
