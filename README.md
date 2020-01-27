# WyoCV Applications

## Table of Contents

- [About](#about)
- [Getting Started](#getting-started)
- [Installing](#installing)
- [Testing](#testing)
- [Deployment](#deployment)

## About

The WyoCV Applications are a suite of tools for interacting with the Wyoming DOT ODE, with an emphasis on Traveler Information Messages (TIMs). The tool suite include modules for both sides of interaction, from the ode-wrapper used to simplify interactions with pushing TIMs, to the ode-data-logger used to subscribe to ODE Kafka topics and deposit data into an Oracle database. Each module within the project contains its own README file to help understand specific functionality.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See [deployment](#deployment) for notes on how to deploy the project on a live system.

### Prerequisites

The WyoCV suite is reliant on very few prerequisites. Any module-specific prerequisite can be found in associated README files. In general, the suite is built using Java and thus a version of JDK 7 or higher is required (JDK found [here](https://www.oracle.com/technetwork/java/javase/downloads/index.html)). 

Additionally, all tools are compiled using [Maven](https://maven.apache.org/). 

At the time of writing this README, the latest working versions are Maven version 3.6.3, Java Version 8 Update 241 (build 1.8.0_241-b07).

### Installing

1. Clone the repo

```
git clone https://trihydro@dev.azure.com/trihydro/CV/_git/WyoCV
```

2. Open the cloned folder in VS Code

```
code WyoCV
```

3. Build the project

```
mvn clean install
```

4. Use the debugger to run each individual module (as seen in the [launch.json](./.vscode/launch.json)).

## Usage 

Each module may be developed and ran individually. Instructions for each module can be found in their respective README files. This top-level view is primarily to allow for ease of running the entire suite through docker containers. 

## Testing

Tests are written using various Java testing libraries, but all may be executed in order with

```
mvn test
```

## Deployment

This application is deployed using Docker, with the docker-compose tool. The associated [docker-compose.yml](./docker-compose.yml) file is used to spin up containers for each of the modules. The file is set up for the development ODE environment and includes multiple instances of several of the Kafka consumers to allow for efficient consumption.

To deploy the suite, first build all modules using 
```
mvn clean install
```
This will create the `target` folder under each module. From here, create a new folder structure to deploy using the `docker-compose.yml`, `.env`, and respective `.jar` file and `Dockerfile`. A basic example using the WyoCV applications as seen here follows (note the Docker configuration can be more complex to include additional modules such as the SMDM and TimCreator):

```
.
├── cv-data-controller
│   ├── cv-data-controller-0.0.1-SNAPSHOT.jar
│   ├── Dockerfile
├── cv-data-tasks
│   ├── cv-data-tasks-0.0.1-SNAPSHOT.jar
│   ├── Dockerfile
├── docker-compose.yml
├── ode-data-logger
│   ├── Dockerfile
│   ├── ode-data-logger-0.0.1-SNAPSHOT.jar
├── ode-mongo-logger
│   ├── Dockerfile
│   ├── ode-mongo-logger-0.0.1-SNAPSHOT.jar
├── ode-wrapper
│   ├── Dockerfile
│   ├── ode-wrapper-0.0.1-SNAPSHOT.jar
├── ode-wrapper-docs
│   └── swagger-ui-master
│       ├── Dockerfile
│       ├── (swagger folder structure)
└── tim-refresh
    ├── Dockerfile   
    ├── tim-refresh-0.0.1-SNAPSHOT.jar

```

To run the suite, copy the [clean-build-and-deploy.sh](./docker-scripts/clean-build-and-deploy.sh) script to the deployment root and execute the script. Alternatively, run the following commands in the same directory as the `docker-compose.yml`:
```
docker-compose stop
docker-compose rm -f -v
docker-compose up --build -d
docker-compose ps
```