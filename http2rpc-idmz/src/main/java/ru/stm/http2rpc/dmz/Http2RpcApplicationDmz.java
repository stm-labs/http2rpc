package ru.stm.http2rpc.dmz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
		ValidationAutoConfiguration.class,
		MongoDataAutoConfiguration.class,
		WebMvcAutoConfiguration.class,
		SecurityAutoConfiguration.class,
		MongoReactiveDataAutoConfiguration.class,
		RedisAutoConfiguration.class,
		RedisRepositoriesAutoConfiguration.class,
		DataSourceAutoConfiguration.class,
		FlywayAutoConfiguration.class,
		ReactiveUserDetailsServiceAutoConfiguration.class
})
@ComponentScan(value = "ru.stm")
public class Http2RpcApplicationDmz {

	public static void main(String[] args) {
		SpringApplication.run(Http2RpcApplicationDmz.class, args);
	}

}
