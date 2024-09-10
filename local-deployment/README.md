# Local Deployment
This document describes how to deploy the application suite locally for development purposes.

## Preparation
### Postgres Database Setup
The postgres database service is defined in the `docker-compose.yml` file, but after creation it must be set up and populated manually. The following steps describe how to do this.

1. Run setup scripts in `db-scripts\pgsql\setup\sql`
2. Run static data scripts in `db-scripts\pgsql\migration\sql\static`

### Preparing JAR Files
The dockerfiles for the wyocv services expect the JAR files to be in the same directory as the Dockerfile. After compilation, copy the JAR files to the appropriate directories.

### Certificates
The `resdf.wyoroad.info.cer` file, found in resources, must be present in the same directory as the Dockerfile for the `cv-data-tasks` service.

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