# WyoCV Applications

## Table of Contents

- [About](#about)
- [Getting Started](#getting-started)
- [Installing](#installing)
- [Testing](#testing)
- [Deployment](#deployment)

## About

The WyoCV Applications are a suite of tools for interacting with the Wyoming DOT ODE, with an emphasis on Traveler Information Messages (TIMs). The tool suite include modules for both sides of interaction, from the ode-wrapper used to simplify interactions with pushing TIMs, to the ode-data-logger used to subscribe to ODE Kafka topics and deposit data into an Oracle database. Each module within the project contains its own README file to help understand specific functionality.
> __Note:__ Most of the modules are hosted here, however the SMDM applicaton is hosted in BitBucket as it is owned by WYDOT and USDOT. This application is imported and ran by our docker-compose.yml file, but is not maintained here.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See [Deployment](#deployment) for notes on how to deploy the project on a live system.

### Prerequisites

- Git (https://git-scm.com/downloads)
- Docker Desktop (https://docs.docker.com/install/)
- VS Code (https://code.visualstudio.com/)
- Remote - Containers (VS Code Extension: [see here](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers))

### Installing

1. Clone the repo

   ```
   git clone https://trihydro@dev.azure.com/trihydro/CV/_git/WyoCV
   ```

2. Open the `wyocv` workspace in VS Code

    ```
    code wyocv.code-workspace
    ```

3. Open the project in a development container
    - Click <kbd>F1</kbd> then run the `Remote-Containers: Open Workspace in Container...` command
    ![command](/images/open-in-remote-container.png)
    - Alternatively, click the Status Bar in the lower-left corner of the window to access this command more quickly.
    ![quick actions](/images/remote-dev-status-bar.png)

    > __Note:__ the first time you do this, it will take a few minutes to build the container. Subsequent connections will be much faster.

4. Once you've connected to the development container, you should be able to build the project by running the following command:
    ```
    mvn clean install
    ```
5. To debug the project, select and run the relevant profile from the _VS Code Debug_ window (see [launch.json](./.vscode/launch.json)).

> __Note:__ when developing inside a docker container, the workspace files are mounted from the local file system. So any changes you make in the container will persist to your computer. If you close your connection to the container, you can still open the workspace locally and commit your changes as necessary.

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