# Apache Camel Bash (Java 17, Gradle, Spring Boot + Apache Camel)

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

## Probar localmente:

Desplegar dos servicios localmente como mocks:

```bash
# Origen (puerto 8082)
docker run -d --name mock-source -p 8082:8080 wiremock/wiremock:3.8.0 --verbose
# Destino (puerto 8083)
docker run -d --name mock-target -p 8083:8080 wiremock/wiremock:3.8.0 --verbose
```

Ejecutar la siguiente sentencia para el 1er servicio:

```bash
# Stub del ORIGEN: GET /api/usuarios/123
curl -X POST http://localhost:8082/__admin/mappings -H 'Content-Type: application/json' -d '{
  "request": { "method": "GET", "url": "/api/usuarios/123" },
  "response": { "status": 200, "headers": {"Content-Type":"application/json"},
    "jsonBody": {"id":123,"name":"Juan","email":"juan@ejemplo.com","status":"active"} }
}'
  }
}'
```

Ejecuar la siguiente sentencia para el 2do servicio:

```bash
# Stub del DESTINO: POST /api/destino
curl -X POST http://localhost:8083/__admin/mappings -H 'Content-Type: application/json' -d '{
  "request": { "method": "POST", "url": "/api/destino" },
  "response": {
    "status": 200,
    "headers": { "Content-Type": "application/json" },
    "jsonBody": { "ok": true, "echo": "{{jsonPath request.body \"$\"}}" },
    "transformers": ["response-template"]
  }
}'
```
