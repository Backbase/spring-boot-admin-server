# Spring Boot Admin Server

### Packaging the image and publishing it to Docker Registry

```shell
mvn clean package -Pjib-push
```

### Packaging the image and push it to local docker daemon for local BE environment

```shell
mvn clean package -Pjib-local
```
