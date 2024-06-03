package com.backbase.oss.admin.server.config;

import static org.springframework.cloud.kubernetes.commons.discovery.KubernetesDiscoveryConstants.HTTP;
import static org.springframework.cloud.kubernetes.commons.discovery.KubernetesDiscoveryConstants.HTTPS;

import de.codecentric.boot.admin.server.cloud.discovery.KubernetesServiceInstanceConverter;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.kubernetes.commons.discovery.KubernetesDiscoveryProperties;
import org.springframework.cloud.kubernetes.commons.discovery.KubernetesServiceInstance;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("istio")
public class IstioServiceInstanceConverter extends KubernetesServiceInstanceConverter {

    private static final Logger log = LoggerFactory.getLogger(IstioServiceInstanceConverter.class);

    public IstioServiceInstanceConverter(
        KubernetesDiscoveryProperties discoveryProperties) {
        super(discoveryProperties);
        log.debug("Loading IstioServiceInstanceConverter");
    }

    @Override
    protected URI getServiceUrl(ServiceInstance instance) {
        if (instance instanceof KubernetesServiceInstance) {
            log.trace("Replacing the pod ip by its serviceId");
            return createUri(instance.isSecure() ? HTTPS : HTTP, instance.getServiceId(), instance.getPort());
        }
        return super.getServiceUrl(instance);
    }

    private URI createUri(String scheme, String host, int port) {
        // assume ExternalName type of service
        if (port == -1) {
            return URI.create(host);
        }
        // assume an endpoint without ports
        if (port == 0) {
            return URI.create(scheme + "://" + host);
        }
        return URI.create(scheme + "://" + host + ":" + port);
    }
}
