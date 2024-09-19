# WYDOT ODE Data Logger
![ODE Mongo Logger Architecture Diagram](./docs/diagrams/ode-mongo-logger-architecture.drawio.png)

The `ODE Mongo Logger` module listens for messages on a specified Kafka topic and writes them to a MongoDB database. The module is designed to be deployed as a Docker container and is part of the WyoCV Suite of applications.

## Table of Contents
- [Installation](#installation)
- [Deployment](#deployment)
- [Configuration](#configuration)
- [Testing](#testing)
- [Usage](#usage)

## Installation
(TBD)

## Deployment
This application is deployed using Docker, and is part of the larger WyoCVApplication suite. The associated Dockerfile is configured for the development ODE environment. See the main [README](../README.md) for the project and associated [docker-compose](../docker-compose.yml), and [sample.env](../sample.env) file for further deployment configurations.

## Configuration
**SOME OF THESE PROPERTIES ARE SENSITIVE. DO NOT PUBLISH THEM TO VERSION CONTROL**

You may configure these values in `ode-mongo-logger/src/main/resources/application.properties` or by editing them in the `sample.env` file at the project root.

**IMPORTANT** When using the env file method, you must You must rename or duplicate the `sample.env` file to `.env`. If using the application.properties method, you must pass in the name of the environment to use with the `--spring.profiles.active` parameter.

| Environment Variable | Variable name(s) in `sample.env` | Property name in `application.properties` | Description                               | Example Value                                                  |
| -------------------- | ------------------------------ | ----------------------------------------- | ----------------------------------------- | -------------------------------------------------------------- |
| MONGOLOGGER_DEPOSIT_TOPIC | TIM_TOPIC, BSM_TOPIC, DA_TOPIC | mongologger.depositTopic                  | The Kafka topic to listen for messages on | topic.OdeTimJson                                            |
| MONGOLOGGER_DEPOSIT_GROUP | TIM_GROUP_MONGO, BSM_GROUP_MONGO, DA_GROUP_MONGO | mongologger.depositGroup                  | The Kafka consumer group to use           | logger_group_tim_dev_local                                            |
| MONGOLOGGER_HOSTNAME | MONGO_HOSTNAME | mongologger.hostname                       | The hostname of the machine that the mongo logger is running on         | localhost                                                    |
| MONGOLOGGER_MONGO_HOST | MONGO_HOST | mongologger.mongoHost                      | The hostname of the MongoDB server         | localhost                                                    |
| MONGOLOGGER_MONGO_DATABASE | MONGO_DATABASE | mongologger.mongoDatabase                       | The name of the MongoDB database to write to | wyo_cv                                                      |
| MONGOLOGGER_MONGO_AUTH_DATABASE | MONGO_AUTH_DATABASE | mongologger.mongoAuthDatabase             | The name of the MongoDB authentication database | admin                                                      |
| MONGOLOGGER_MONGO_USERNAME | MONGO_USERNAME | mongologger.mongoUsername                 | The username to use to connect to MongoDB  | admin                                                       |
| MONGOLOGGER_MONGO_PASSWORD | MONGO_PASSWORD | mongologger.mongoPassword                 | The password to use to connect to MongoDB  | password                                                    |
| MONGOLOGGER_ALERT_ADDRESSES | MONGO_ALERT_ADDRESSES | mongologger.alertAddresses                | The email addresses to send alerts to      | test@gmail.com                                               |
| MONGOLOGGER_FROM_EMAIL | MONGO_FROM_EMAIL | mongologger.fromEmail                      | The email address to send alerts from      | test@gmail.com                                               |
| MONGOLOGGER_ENVIRONMENT_NAME | ENVIRONMENT_NAME | mongologger.environmentName              | The name of the environment the logger is running in | dev                                                      |
| MONGOLOGGER_MAIL_HOST | MAIL_HOST | mongologger.mailHost                      | The hostname of the SMTP server to use for sending alerts | smtp.gmail.com                                            |
| MONGOLOGGER_MAIL_PORT | MAIL_PORT | mongologger.mailPort                      | The port of the SMTP server to use for sending alerts | 587                                                        |

## Testing
### Unit Tests
1. Reopen project in provided dev container
1. Run the following command to execute unit tests:
    ```bash
    mvn clean test -p cv-data-service-library -p ode-mongo-logger
    ```

This will build the library that the module depends on and run the unit tests for the module (as well as the library).

## Usage
This module requires a Kafka broker and MongoDB server to be running. The module listens for messages on a specified Kafka topic and writes them to a MongoDB database.

Once configured and running, no further action is required.