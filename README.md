# BiSecur2MQTT

MQTT bridge for BiSecur Gateway

[![Docker Hub](https://img.shields.io/badge/%20-DockerHub-blue?logo=docker&style=plastic)](https://hub.docker.com/r/petrvlcek/bisecur2mqtt)
![Docker Image Size (latest by date)](https://img.shields.io/docker/image-size/petrvlcek/bisecur2mqtt?sort=date&style=plastic)


## Usage

### Docker Compose

```yaml
version: '3'
services:
  bisecur2mqtt:
    container_name: bisecur2mqtt
    image: petrvlcek/bisecur2mqtt:latest
    restart: unless-stopped
    env_file:
      - .env
```

### Configuration

All necessary configuration is passed to the container with environment variables.

| Variable          | Description                                   |
|-------------------|-----------------------------------------------|
| GATEWAY_ID        | ID for identification purposes                |
| GATEWAY_ADDRESS   | IP address where BiSecur Gateway is running   |
| GATEWAY_SENDER_ID | Just use `000000000000`                       |
| GATEWAY_USERNAME  | Username configured within BiSecur Gateway    |
| GATEWAY_PASSWORD  | Username configured within BiSecur Gateway    |
| MQTT_SERVER_URI   | IP address where MQTT Server is running       |
| MQTT_CLIENT_ID    | ID for identification purposes                |
| MQTT_USERNAME     | Username (if configured in the MQTT server)   |
| MQTT_PASSWORD     | Password (if configured)                      |

### Using MQTT bridge

This bridge connects to a Hörmann BiSecur Gateway and publishes its state to topics prefixed with `bisecur2mqtt/*`.
Each registered door in BiSecur Gateway is identified by a group name. Every group has 2 topics that can be used to
getting state and changing state of a door. For example if you have a group named `garagedoor` registered in the Gateway
you can use these 3 topics:

* `bisecur2mqtt/garagedoor/get` - by publishing anything to this topic you can trigger the retrieval of the latest state
  of a garage door from the Gateway
* `bisecur2mqtt/garagedoor/set` - by publishing to this topic you can send an impulse to the garage door in the Gateway
  just like if you pushed the button on your remote
* `bisecur2mqtt/garagedoor/state` - you can consume this topic to get results of `get/set` actions published to two
  topics above

### State

This is an excerpt of a schema of a payload that can be consumed from the `bisecur2mqtt/*/state` topic:

  ```
  data class State(
    val groupName: String,
    val isOpen: Boolean,
    val stateInPercent: Int,
    val isError: Boolean = false,
    val autoClose: Boolean = false
  )
  ```

## Building from source

### Prerequisites

* Java 17, Gradle 7.4.2 (both can be installed with [sdkman.io](https://sdkman.io))
* Docker

### Build steps

1. Build jar file
   ```
   ./gradlew build shadowJar
   ```
2. Build and push Docker images
   ```
   docker buildx build --push --tag petrvlcek/bisecur2mqtt:latest --platform linux/arm/v7,linux/arm64/v8,linux/amd64 .
   ```

## Other resources

> ℹ️ Work on this repository is in progress. Please head
> to [Discussions](https://github.com/petrvlcek/bisecur2mqtt/discussions) if you have any questions.

## Credits

This project is based on great library [bisdk/sdk](https://github.com/bisdk/sdk).
