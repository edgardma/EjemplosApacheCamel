# Camel API (Java 17, Gradle, Spring Boot + Apache Camel)

Pipeline que consume un **servicio REST (GET)**, **transforma** el JSON y lo **envía** a otro servicio **REST (POST)**.

## Requisitos

- Java 17
- Gradle (o wrapper si lo agregas)
- Docker (opcional para contenedor)
- Kubernetes (opcional para k8s)

## Ejecutar Test

Ejecutar:

La primera vez, ejecutar:

```bash
./gradlew --refresh-dependencies clean test
```

Luego, ejecutar:

```bash
./gradlew clean test
```


