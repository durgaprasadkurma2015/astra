package com.astra.config;
import org.springframework.context.annotation.*; import org.springframework.scheduling.annotation.EnableAsync; import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor; import java.util.concurrent.Executor;
@Configuration @EnableAsync public class AsyncConfig { @Bean("astraTaskExecutor") Executor executor(){var e=new ThreadPoolTaskExecutor();e.setCorePoolSize(4);e.setMaxPoolSize(16);e.setQueueCapacity(500);e.setThreadNamePrefix("astra-async-");e.initialize();return e;} }
