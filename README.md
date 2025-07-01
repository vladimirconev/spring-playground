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

Generate new JWT signing key using python (v3.12.x):
````
import os
import base64

# Generate a 512-bit (64-byte) key for HmacSHA512
secret_key_bytes = os.urandom(64) # 64 bytes * 8 bits/byte = 512 bits

# Encode the key to Base64 URL-safe for easy storage/use in configuration
secret_key_b64_urlsafe = base64.urlsafe_b64encode(secret_key_bytes).decode('utf-8')

print(f"Generated HmacSHA512 (HS512) Key (Base64 URL-safe):")
print(secret_key_b64_urlsafe)
````

Happy Coding!!! 
