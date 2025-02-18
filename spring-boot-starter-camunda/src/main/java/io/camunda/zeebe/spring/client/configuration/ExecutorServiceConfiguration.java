package io.camunda.zeebe.spring.client.configuration;

import io.camunda.zeebe.spring.client.jobhandling.ZeebeClientExecutorService;
import io.camunda.zeebe.spring.client.properties.ZeebeClientConfigurationProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnMissingBean(ZeebeClientExecutorService.class)
public class ExecutorServiceConfiguration {

  private final ZeebeClientConfigurationProperties configurationProperties;
  public ExecutorServiceConfiguration(ZeebeClientConfigurationProperties configurationProperties) {
    this.configurationProperties = configurationProperties;
  }

  @Bean
  public ZeebeClientExecutorService zeebeClientThreadPool(@Autowired(required = false) MeterRegistry meterRegistry) {
    ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(configurationProperties.getNumJobWorkerExecutionThreads());
    if (meterRegistry != null) {
      MeterBinder threadPoolMetrics = new ExecutorServiceMetrics(
        threadPool, "zeebe_client_thread_pool", Collections.emptyList());
      threadPoolMetrics.bindTo(meterRegistry);
    }
    configurationProperties.setScheduledExecutorService(threadPool);
    configurationProperties.setOwnsJobWorkerExecutor(true);
    return new ZeebeClientExecutorService(threadPool);
  }
}
