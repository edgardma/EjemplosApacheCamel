
# Camel Pipeline (Java 17, Gradle, Spring Boot + Apache Camel)

Pipeline que consume un **servicio REST (GET)**, **transforma** el JSON y lo **envía** a otro servicio **REST (POST)**.

## Requisitos
- Java 17
- Gradle (o wrapper si lo agregas)
- Docker (opcional para contenedor)
- Kubernetes (opcional para k8s)

## Ejecutar local
```bash
# Variables opcionales
export APP_SOURCE_URL="localhost:8082/api/usuarios/123"
export APP_TARGET_URL="localhost:8083/api/destino"
export APP_POLL_PERIOD="10000"

./gradlew bootRun   # o: gradle bootRun
```

## Construir JAR
```bash
./gradlew clean build
```

El jar quedará en `build/libs/camel-pipeline-1.0.0.jar`.

## Docker
```bash
# Construir imagen
docker build -f docker/Dockerfile -t camel-pipeline:1.0.0 .

# Ejecutar contenedor
docker run --rm -p 8080:8080 \

  -e APP_SOURCE_URL="localhost:8082/api/usuarios/123" \

  -e APP_TARGET_URL="localhost:8083/api/destino" \

  -e APP_POLL_PERIOD="10000" \

  camel-pipeline:1.0.0
```

### Docker Compose
```bash
docker compose -f docker/docker-compose.yml up --build
```

## Kubernetes (opcional)
```bash
kubectl apply -f k8s/config.yaml
kubectl apply -f k8s/deployment.yaml
```

## Notas
- Ajusta las expresiones del JSON en `PipelineRoute` según el payload real del servicio origen.
- Si el destino requiere autenticación Bearer, define `TARGET_BEARER_TOKEN` (secret en k8s o env en Docker) y descomenta la línea correspondiente en la ruta.
