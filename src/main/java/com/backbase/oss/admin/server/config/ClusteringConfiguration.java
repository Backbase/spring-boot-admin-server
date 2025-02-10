package com.backbase.oss.admin.server.config;

import static de.codecentric.boot.admin.server.config.AdminServerHazelcastAutoConfiguration.DEFAULT_NAME_EVENT_STORE_MAP;
import static de.codecentric.boot.admin.server.config.AdminServerHazelcastAutoConfiguration.DEFAULT_NAME_SENT_NOTIFICATIONS_MAP;

import com.hazelcast.config.AttributeConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.config.MergePolicyConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.spi.merge.PutIfAbsentMergePolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSession;
import org.springframework.session.hazelcast.HazelcastIndexedSessionRepository;
import org.springframework.session.hazelcast.HazelcastSessionSerializer;
import org.springframework.session.hazelcast.PrincipalNameExtractor;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

@EnableHazelcastHttpSession
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "spring.boot.admin.hazelcast", name = "start-embedded", matchIfMissing = true)
public class ClusteringConfiguration {

    @Bean
    public Config hazelcastConfig() {
        // This map is used to store the events.
        // It should be configured to reliably hold all the data,
        // Spring Boot Admin will compact the events, if there are too many
        MapConfig eventStoreMap = new MapConfig(DEFAULT_NAME_EVENT_STORE_MAP).setInMemoryFormat(InMemoryFormat.OBJECT)
            .setBackupCount(1)
            .setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));

        // This map is used to deduplicate the notifications.
        // If data in this map gets lost, it should not be a big issue as it will atmost
        // lead to the same notification to be sent by multiple instances
        MapConfig sentNotificationsMap = new MapConfig(DEFAULT_NAME_SENT_NOTIFICATIONS_MAP)
            .setInMemoryFormat(InMemoryFormat.OBJECT)
            .setBackupCount(1)
            .setEvictionConfig(
                new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU).setMaxSizePolicy(MaxSizePolicy.PER_NODE))
            .setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));

        Config config = new Config();
        config.setClusterName("spring-boot-admin-cluster");
        config.addMapConfig(eventStoreMap);
        config.addMapConfig(sentNotificationsMap);
        config.setProperty("hazelcast.jmx", "true");

        // Add this attribute to be able to query sessions by their PRINCIPAL_NAME_ATTRIBUTE's
        AttributeConfig attributeConfig = new AttributeConfig()
            .setName(HazelcastIndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE)
            .setExtractorClassName(PrincipalNameExtractor.class.getName());

        // Configure the session map
        config.getMapConfig(HazelcastIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME)
            .addAttributeConfig(attributeConfig).addIndexConfig(
                new IndexConfig(IndexType.HASH, HazelcastIndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE));

        // Use custom serializer to de/serialize sessions faster. This is optional.
        SerializerConfig serializerConfig = new SerializerConfig();
        serializerConfig.setImplementation(new HazelcastSessionSerializer()).setTypeClass(MapSession.class);
        config.getSerializationConfig().addSerializerConfig(serializerConfig);
        return config;
    }

}
