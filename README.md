# TIM Manager
![data-flow-diagram](/images/diagrams/data-flow-diagram.png)

The TIM Manager (TIMM) is a suite of tools for interacting with the Operational Data Environment (ODE), with an emphasis on Traveler Information Messages (TIMs). The tool suite include modules for both sides of interaction, from the ode-wrapper used to simplify interactions with pushing TIMs, to the ode-data-logger used to subscribe to ODE Kafka topics and deposit data into a database. Each module within the project contains its own README file to help understand specific functionality.

## Table of Contents
- [Useful Links](#useful-links)
- [Installation](#installation)
- [Deployment](#deployment)
- [Configuration](#configuration)
- [Testing](#testing)
- [Usage](#usage)

## Useful Links
### Core Library
- [CV Data Service Library README](./cv-data-service-library/README.md)

### Modules
- [CV Data Controller](./cv-data-controller/README.md)
- [CV Data Tasks](./cv-data-tasks/README.md)
- [Logger Kafka Consumer](./logger-kafka-consumer/README.md)
- [ODE Data Logger](./ode-data-logger/README.md)
- [ODE Mongo Logger](./ode-mongo-logger/README.md)
- [ODE Wrapper](./ode-wrapper/README.md)
- [RSU Data Controller](./rsu-data-controller/README.md)
- [TIM Refresh](./tim-refresh/README.md)

### Other
- [Milepost Graph DB](./milepost-graph-db/README.md)
- [Local Deployment Resources](./local-deployment/README.md)
- [Monitoring Resources](./monitoring/README.md)
- [CV Manager Resources](./cv-manager/README.md)

## Installation
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See [Deployment](#deployment) for notes on how to deploy the project on a live system.

### Prerequisites
- Git (https://git-scm.com/downloads)
- Docker Desktop (https://docs.docker.com/install/)
- VS Code (https://code.visualstudio.com/)
- Remote - Containers (VS Code Extension: [see here](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers))

### Setup
1. Clone the repository to your local machine
   ```
   git clone https://github.com/Trihydro/TIM-Manager.git
   ```

2. Open the `timm` workspace in VS Code
    ```
    code timm.code-workspace
    ```

3. Open the project in a development container
    - Click <kbd>F1</kbd> then run the `Remote-Containers: Open Workspace in Container...` command
    ![command](/images/open-in-remote-container.png)
    - Alternatively, click the Status Bar in the lower-left corner of the window to access this command more quickly.
    ![quick actions](/images/remote-dev-status-bar.png)

    > __Note:__ the first time you do this, it will take a few minutes to build the container. Subsequent connections will be much faster.

4. Generate a GitHub Access Token
    - Create a new [Github Classic Token](https://github.com/settings/tokens) with the read:packages scope
    - Copy the resulting token

5. Create Maven settings.xml
    - The [usdot jpo-ode packages](https://github.com/orgs/usdot-jpo-ode/packages?repo_name=jpo-ode) required by the Tim Manager require a settings.xml file used by Maven
    - There is an [example settings file](example-settings.xml) provided for you
    - Navigate to the .m2 directory and create the settings.xml file by running the following:
        ```
        cd ~/.m2
        touch settings.xml
        ```
    - Copy the contents of example-settings.xml to the settings.xml file, replacing username with your github username and password with your generated access token
    - This can be done with any installed text editor such as [Nano](https://www.nano-editor.org/docs.php) or [Vim](https://www.vim.org/docs.php)
        ```
        nano settings.xml
        ```
    - Navigate back to the project working directory

6. Once you've connected to the development container, you should be able to build the project by running the following command:
    ```
    mvn clean install
    ```
7. To debug the project, select and run the relevant profile from the _VS Code Debug_ window (see [launch.json](./.vscode/launch.json)).

> __Note:__ when developing inside a docker container, the workspace files are mounted from the local file system. So any changes you make in the container will persist to your computer. If you close your connection to the container, you can still open the workspace locally and commit your changes as necessary.

## Deployment
This application is deployed using Docker, with the docker compose tool. The associated [docker-compose.yml](./docker-compose.yml) file is used to spin up containers for each of the modules.

To deploy the suite, first build all modules using 
```
mvn clean install
```
This will create the `target` folder under each module. From here, create a new folder structure to deploy using the `docker-compose.yml`, `.env`, and respective `.jar` file and `Dockerfile`. A basic example using the TIMM applications as seen here follows:

```
.
├── cv-data-controller
│   ├── cv-data-controller-2.0.0.jar
│   ├── Dockerfile
├── cv-data-tasks
│   ├── cv-data-tasks-2.0.0.jar
│   ├── Dockerfile
├── docker-compose.yml
├── ode-data-logger
│   ├── Dockerfile
│   ├── ode-data-logger-2.0.0.jar
├── ode-mongo-logger
│   ├── Dockerfile
│   ├── ode-mongo-logger-2.0.0.jar
├── ode-wrapper
│   ├── Dockerfile
│   ├── ode-wrapper-2.0.0.jar
├── ode-wrapper-docs
│   └── swagger-ui-master
│       ├── Dockerfile
│       ├── (swagger folder structure)
└── tim-refresh
    ├── Dockerfile   
    ├── tim-refresh-2.0.0.jar

```

To run the suite, copy the [clean-build-and-deploy.sh](./docker-scripts/clean-build-and-deploy.sh) script to the deployment root and execute the script. Alternatively, run the following commands in the same directory as the `docker-compose.yml`:
```
docker compose stop
docker compose rm -f -v
docker compose up --build -d
docker compose ps
```

## Configuration
Each module has its own configuration, but the suite as a whole can be configured using a copy of the `sample.env` file. This file should be renamed to `.env` and placed in the same directory as the `docker-compose.yml` file.

## Testing
### Unit Testing
To run the unit tests, follow these steps:
1. Reopen the project in the provided dev container by clicking on the blue button in the bottom left corner of the window and selecting "Reopen in Container"
1. Open a terminal in the dev container
1. Run the following command to execute the tests:
    ```
    mvn clean test
    ```

### Integration Testing
To test the integration of the modules, see the [local deployment resources](./local-deployment/README.md) for instructions on how to deploy the suite locally. This will allow for testing the interaction between some or all of the modules.

## Usage 
Each module may be developed and ran individually. Instructions for each module can be found in their respective README files. This top-level view is primarily to allow for ease of running the entire suite through docker containers. This can be done by running `docker compose up --build -d` in the same directory as the `docker-compose.yml` file.

Once running, the ODE Wrapper module serves as the primary entry point for interacting with the suite. By default, this module runs on port 7777.