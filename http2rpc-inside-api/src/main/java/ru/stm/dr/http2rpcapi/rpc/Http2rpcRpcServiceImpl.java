package ru.stm.dr.http2rpcapi.rpc;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.stm.dr.http2rpcapi.rpc.metrics.proxy.ProxyRequestRpcRequest;
import ru.stm.dr.http2rpcapi.rpc.metrics.proxy.ProxyRequestRpcResponse;
import ru.stm.rpc.kafkaredis.ann.RemoteService;
import ru.stm.rpc.kafkaredis.service.RpcProvider;

import static ru.stm.dr.http2rpcapi.rpc.Http2rpcDmzKafkaTopics.HTTP2RPC_TOPIC;

@RemoteService(namespace = NamespacesConstants.NAMESPACE_DMZ, topic = HTTP2RPC_TOPIC, useSpelForTopic = true)
@RequiredArgsConstructor
public class Http2rpcRpcServiceImpl implements Http2rpcRpcService {

    private final RpcProvider provider;

    @Override
    public Mono<ProxyRequestRpcResponse> proxy(ProxyRequestRpcRequest proxy) {
        return provider.call(proxy, ProxyRequestRpcResponse.class);
    }

}
