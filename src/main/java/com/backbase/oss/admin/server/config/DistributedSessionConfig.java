package com.backbase.oss.admin.server.config;

import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.infinispan.spring.embedded.session.configuration.EnableInfinispanEmbeddedHttpSession;
import org.infinispan.spring.starter.embedded.InfinispanGlobalConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableCaching
@Configuration(proxyBeanMethods = false)
@EnableInfinispanEmbeddedHttpSession(cacheName = "default")
@ConditionalOnProperty(value = "infinispan.embedded.enabled", matchIfMissing = true)
public class DistributedSessionConfig {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(value = "infinispan.jgroups.configurationFile")
    static class CustomJGroupsAutoConfiguration {

        @Bean
        public InfinispanGlobalConfigurationCustomizer infinispanGlobalConfigurationCustomizer(
            @Value("${infinispan.jgroups.configurationFile}") String jgroupsConfigurationFile) {
            return builder -> builder.transport()
                .defaultTransport()
                .addProperty(JGroupsTransport.CONFIGURATION_FILE, jgroupsConfigurationFile);
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
    @ConditionalOnMissingBean(InfinispanGlobalConfigurationCustomizer.class)
    @ConditionalOnProperty(value = "infinispan.jgroups.configurationFile", matchIfMissing = true)
    static class KubernetesJGroupsAutoConfiguration {

        @Bean
        public InfinispanGlobalConfigurationCustomizer infinispanGlobalConfigurationCustomizer() {
            return builder -> builder.transport()
                .defaultTransport()
                .addProperty(JGroupsTransport.CONFIGURATION_FILE, "default-configs/default-jgroups-kubernetes.xml");
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(InfinispanGlobalConfigurationCustomizer.class)
    @ConditionalOnProperty(value = "infinispan.jgroups.configurationFile", matchIfMissing = true)
    @AutoConfigureAfter(KubernetesJGroupsAutoConfiguration.class)
    static class DefaultJGroupsAutoConfiguration {

        @Bean
        public InfinispanGlobalConfigurationCustomizer infinispanGlobalConfigurationCustomizer() {
            return builder -> builder.transport()
                .defaultTransport()
                .addProperty(JGroupsTransport.CONFIGURATION_FILE, "default-configs/default-jgroups-udp.xml");
        }

    }

}
