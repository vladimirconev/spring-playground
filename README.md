# Description
Sample Spring-boot application using spring security alongside JWT. Serves as playground on trying new features in Spring ecosystem and/or Java. 

# Pre-requisites 
- JDK 24 + 
- Maven 
- Docker / Docker Desktop

# Running locally

**Using Docker Compose:**
````
docker compose up -f compose.yaml --build -d
````

````
docker compose down
````
Spinning up just postgres DB for debugging:

````
docker compose up -f infra.yaml -d
````

To Build:
````
mvn clean verify -T 4C
````

To format code:
````
mvn com.spotify.fmt:fmt-maven-plugin:format
````
Details can be found on https://github.com/spotify/fmt-maven-plugin. 

Check up Swagger UI on: `http://localhost:8080/swagger-ui.html`.

Obtaining JWT via HTTP POST `/api/v1/login`. 

Happy Coding!!! 
