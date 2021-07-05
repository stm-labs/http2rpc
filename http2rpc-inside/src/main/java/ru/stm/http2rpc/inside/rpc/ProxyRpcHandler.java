package ru.stm.http2rpc.inside.rpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.stm.dr.http2rpcapi.rpc.metrics.proxy.ProxyRequestRpcRequest;
import ru.stm.dr.http2rpcapi.rpc.metrics.proxy.ProxyRequestRpcResponse;
import ru.stm.dr.http2rpcapi.rpc.NamespacesConstants;
import ru.stm.http2rpc.inside.proxy.InsideProxyService;
import ru.stm.rpc.core.Rpc;
import ru.stm.rpc.core.RpcHandler;
import ru.stm.rpc.types.MethodType;

import static ru.stm.dr.http2rpcapi.rpc.Http2rpcDmzKafkaTopics.HTTP2RPC_TOPIC;

@Component
@Rpc(topic = HTTP2RPC_TOPIC, namespace = NamespacesConstants.NAMESPACE_DMZ, useSpelForTopic = true)
@Slf4j
@RequiredArgsConstructor
public class ProxyRpcHandler {

    private final InsideProxyService insideProxyService;

    @RpcHandler(value = "Запроксировать метод", type = MethodType.POST)
    public Mono<ProxyRequestRpcResponse> proxy(ProxyRequestRpcRequest request) {
        return insideProxyService.doProxy(request);
    }

}
