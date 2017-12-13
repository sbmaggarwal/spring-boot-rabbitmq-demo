# Spring Boot RabbitMQ Demo
This application is a demonstration on how to configure multiple RabbitMQ queues in Spring Boot and publish/listen on them simultaneously.

## Getting Started
Running this maven based app is easy.

### Prerequisites
Few points to look out for:
* This app runs on port `8075` so make sure no other application is using the same port.
* Running local instance of RabbitMQ with default credentials as `guest/guest` on `Port 5672`.

### Do a Maven install
Do collect dependencies, run a maven install:
```
mvn clean install
```
### Wave the run wand
As the app includes maven plugin, this can be run now using following command:
```
mvn spring-boot:run
```
### Testing the app
Now, just hit a GET request on:
```
localhost:8075/message/{message}
```
This request will send the `message` on first queue. Here is the flow of the `message` after that:
* Message arrives at controller API which publishes the message on first queue.
* Same app listens for messages on **first** queue and when it arrives, it logs them and publishes the message on **second** queue.
* Same app listens for messages on **second** queue as well and when it arrives, it just logs them.

Read more about me [here](https://sbmaggarwal.github.io/).
