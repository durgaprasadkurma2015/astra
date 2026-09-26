package com.astra.config;
import org.springframework.context.annotation.*; import org.springframework.scheduling.annotation.EnableScheduling; import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
@Configuration @EnableScheduling public class SchedulingConfig { @Bean ThreadPoolTaskScheduler taskScheduler(){var s=new ThreadPoolTaskScheduler();s.setPoolSize(4);s.setThreadNamePrefix("astra-scheduler-");return s;} }
