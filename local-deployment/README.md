# Local Deployment
This document describes how to deploy the application suite locally for development purposes.

## Preparation
### Postgres Database Setup
The postgres database service is defined in the `docker-compose.yml` file, but after creation it must be set up and populated manually. The following steps describe how to do this.
1. Navigate to the `local-deployment` directory.
1. Copy the `sample.env` file to `.env` and set DOCKER_HOST_IP to the IP address of the host machine.
1. Spin up the database with the following command:

    ```bash
    docker compose up -d postgres
    ```
1. Open WSL and navigate to the root of the repository.
1. Make sure that psql is installed on your machine. If it is not, install it using the following command:

    ```bash
    sudo apt-get install postgresql-client
    ```
1. Navigate to the `db-scripts/pgsql/setup` directory
1. Copy the `sample.env` file to `.env` and edit the file to specify the database connection information.
1. Run the following command to set up the database:

    ```bash
    ./setup.sh
    ```
1. Navigate to the `db-scripts/pgsql/static-data` directory
1. Copy the `sample.env` file to `.env` and edit the file to specify the database connection information.
1. Run the following command to populate the database with static data:

    ```bash
    ./insert-static-data.sh
    ```

1. Set wal_level to 'logical' by executing the following query:
    
        ```sql
        ALTER SYSTEM SET wal_level = 'logical';
        ```
1. Restart the database service:
    
        ```bash
        docker compose restart postgres
        ```

### Preparing JAR Files
The dockerfiles for the TIMM services expect the JAR files to be in the same directory as the Dockerfile. After compilation, copy the JAR files to the appropriate directories.

### Certificates
The certificate file that is being used must be present in the same directory as the Dockerfile for the `cv-data-tasks` service.

### Mocking
Several database objects are mocked and can be created using the scripts in `db-scripts\pgsql\mocking\sql`. Depending on what is being tested, these may or may not be necessary.

## Running the Suite
After the database is set up and the JAR files are in place, run the following command in the `local-deployment` directory:

```bash
docker compose up --build -d
```

This will build and run the suite in detached mode. To view logs, use the following command:

```bash
docker compose logs -f
```

## Stopping the Suite
To stop the suite, run the following command in the `local-deployment` directory:

```bash
docker compose down
```

To reset the database, run the following command in the `local-deployment` directory:

```bash
docker compose down -v
```

This will remove the database volume, effectively resetting the database.