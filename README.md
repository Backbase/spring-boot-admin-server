# Spring Boot Admin Server

### Packaging the image and publishing it to official Docker Registry 
> **Note:** This is for contributor of the repo to push images to the [official Docker Registry](https://docker.io/backbasecs) which requires the necessary permissions. 

```shell
mvn clean package -Pjib-push
```

### Deploy the admin server to monitor [local backend environment](https://github.com/Backbase/local-backend-setup)
> **Note:** Everyone having a local bankend environment could adopt the admin server by following the instrucitons below

1. Adding the following docker compose snippet to your docker compose file. 
```yaml
  spring-boot-admin-server:
    image: docker.io/backbasecs/spring-boot-admin-server:3.4.2
    ports:
      - "9000:8080"
    environment:
      eureka.client.enabled: true
      eureka.client.serviceUrl.defaultZone: http://registry:8080/eureka
      eureka.instance.non-secure-port: 8080
      eureka.instance.prefer-ip-address: true
      spring.security.user.name: admin
      spring.security.user.password: admin
      spring.boot.admin.ui.hide-instance-url: true
      spring.boot.admin.hazelcast.start-embedded: false
    depends_on:
      registry:
        condition: service_healthy
```

2. Open the admin web console at at http://localhost:9000/ and login with "admin/admin"

