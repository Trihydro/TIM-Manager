# Milepost Neo4j Database

---

<p align="center"> Dockerized Neo4j database used to efficiently query paths across Wyoming highways
    <br> 
</p>

## 📝 Table of Contents

- [About](#about)
- [Getting Started](#getting_started)
- [Deployment](#deployment)
- [Usage](#usage)
- [Built Using](#built_using)
- [TODO](#TODO)

## 🧐 About <a name = "about"></a>

This is a simple stand-up of a dockerized Neo4j database used to query paths instead of the milepost_vw. It includes several scripts to run to get a version up and running with the Milepost layer.

## 🏁 Getting Started <a name = "getting_started"></a>

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See [deployment](#deployment) for notes on how to deploy the project on a live system.

### Prerequisites

The only real prerequisite here is to have Docker up and running. The scripts take care of the rest.

### Installing

A docker-compose file has been provided here for simplicity to enable running this container individually. To run the container simply execute the following command:

```
docker-compose up --build -d
```

This will install Neo4j in a docker container without authentication and expose the following ports for access:

```
6474: http
6473: https
6687: bolt
```

To view the database in browser navigate to http://localhost:6474/browser.

## 🎈 Usage <a name="usage"></a>

The initial check-in includes the base set of data to avoid having to run the scripts. If you need to update the data in the Neo4j database the following steps may be taken.

1. Export milepost_vw_new data as .csv from the database (include headers)
   ```
   SELECT
       *
   FROM
       milepost_vw_new
   ORDER BY
       common_name,
       milepost
   ```
2. Copy the new export.csv file to the specified [import folder](./neo-data/import)
3. Run [import_commands.cmd](./import_commands.cmd) to import new data. Note that you will need to remove any existing data you are replacing through the browser UI

## 🚀 Deployment <a name = "deployment"></a>

Deployment should be very similar to running this locally since everything will be handled in docker. This application can be added to the main docker-compose file in the TIMM applications folder for simplicity. If a different location is desired to store data, specify those locations in the docker-compose.yml file and copy the following folders to the appropriate locations:

- [conf](./neo-data/conf) - holds custom configuration
- [data/databases](./neo-data/data/databases) - houses the data
- [import](./neo-data/import) - used to import any data
- [plugins](./neo-data/plugins) - holds the APOC and Graph Algorithm jar files used to access data

## ⛏️ Built Using <a name = "built_using"></a>

- [Neo4j](https://neo4j.com/) - Database

## TODO <a name="TODO"></a>

The [generate_relationships.sh](./generate_relationships.sh) and [generate_named_relationships.cql](generate_named_relationships.cql) are to be used in conjunction to generate a more efficient and communicative set of import statements to be ran rather than the existing [import.cql](./neo-data/import/import.cql). The existing import scripts run for a substantial time (~12+ hours on dev machine) and do not communicate progress very well.
