# Spring Boot Admin Server

### Packaging the image and publishing it to Docker Registry

```shell
mvn clean package -Pjib-push
```

### Packaging the image and push it to local docker daemon for local testing purpose

```shell
mvn clean package -Pjib-local
```

### Deploy to [local backend environment](https://github.com/Backbase/local-backend-setup)

Add the following docker compose snippet 
```yaml
services:
    spring-boot-admin-eureka:
        image: local/spring-boot-admin-server:3.4.3
        ports:
            - "9000:8080"
        environment:
            <<: *common-variables
            spring.profiles.active: local
        links:
            - registry
```
Assumeing the common-variables has captured the properties required by both spring boot admin server and services being monitored

```yaml
  eureka.client.serviceUrl.defaultZone: http://registry:8080/eureka
  eureka.client.registry-fetch-interval-seconds: 15
  eureka.client.instance-info-replication-interval-seconds: 15
  eureka.client.healthcheck.enabled: true
  eureka.instance.non-secure-port: 8080
  eureka.instance.prefer-ip-address: true
  eureka.instance.initialStatus: STARTING
  management.endpoints.web.exposure.include: '*'
  management.endpoints.enabled-by-default: true
  management.security.roles: ANONYMOUS,ACTUATOR
  management.security.enabled: false
  management.endpoint.env.show-values: ALWAYS
  management.endpoint.env.post.enabled: true
  management.endpoint.configprops.show-values: ALWAYS
  management.endpoint.health.show-details: ALWAYS
  management.endpoint.health.show-components: ALWAYS
```

