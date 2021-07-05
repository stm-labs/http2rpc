package ru.stm.dr.http2rpcapi.rpc;

/**
 * Список топиков в Kafka iDMZ, которые слушает микросервис RPC Proxy
 */
public interface Http2rpcDmzKafkaTopics {

    String HTTP2RPC_TOPIC = "#{systemEnvironment['STM_PROXY_SYSTEM_CODE']}";

}
