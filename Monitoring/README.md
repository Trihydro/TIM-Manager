<p align="center">
  <a href="" rel="noopener">
 <img width=200px height=200px src="https://i.imgur.com/6wj0hh6.jpg" alt="Project logo"></a>
</p>

<h3 align="center">WyoCV Monitoring</h3>

<div align="center">

[![Status](https://img.shields.io/badge/status-active-success.svg)]()
[![GitHub Issues](https://img.shields.io/github/issues/kylelobo/The-Documentation-Compendium.svg)](https://github.com/kylelobo/The-Documentation-Compendium/issues)
[![GitHub Pull Requests](https://img.shields.io/github/issues-pr/kylelobo/The-Documentation-Compendium.svg)](https://github.com/kylelobo/The-Documentation-Compendium/pulls)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](/LICENSE)

</div>

---

<p align="center"> Few lines describing your project.
    <br> 
</p>

## 📝 Table of Contents

- [About](#about)
- [Getting Started](#getting_started)
- [Deployment](#deployment)
- [Usage](#usage)
- [Built Using](#built_using)
- [TODO](../TODO.md)
- [Contributing](../CONTRIBUTING.md)
- [Authors](#authors)
- [Acknowledgments](#acknowledgement)

## 🧐 About <a name = "about"></a>

A simple monitoring solution for the WyoCV tool suite. Watches for server irregularities such as high CPU/memory usage as well as storage. Also monitors all WyoCV and ODE containers and alerts if anything goes down.

## 🏁 Getting Started <a name = "getting_started"></a>

The `docker-compose.yml` file holds the information needed to get up and running with this monitoring solution. Configurations for individual components can be found in their respective folders. 
### Alert Manager ###
The `alertmanager` folder contains alert configurations in the `config.yml` file. This is used to manage email recipients and SMTP settings. 
### Prometheus ###
The `prometheus` folder holds the Prometheus config as well as alert rules to use for sending alerts. Prometheus runs on port 9091 ([DEV](http://10.145.9.205:9091/), [PROD](http://10.145.9.204:9091/)) where alerts can be viewed and queries tested.
### Grafana ###
Grafana runs on port 9095 ([DEV](http://10.145.9.205:9095/), [PROD](http://10.145.9.204:9095/)). Note the `Docker and system monitoring` dashboard for useful information.
### Data Scraping ###
The solution uses Prometheus to scrape data from a couple of different sources, namely `node_exporter` and `docker_exporter`. To see available fields these can be viewed [DEV](http://10.145.9.205:9100/metrics) and [DEV](http://10.145.9.205:9417/metrics) respectively. 

## 🚀 Deployment <a name = "deployment"></a>

The system is deployed using docker/docker compose and the configuraton files found here. Note that the alertmanager files may need to change depending on who will need notified.

## ⛏️ Built Using <a name = "built_using"></a>

- [Prometheus](https://prometheus.io/)
- [Node Exporter](https://github.com/prometheus/node_exporter)
- [docker_exporter](https://github.com/prometheus-net/docker_exporter)
- [Grafana](https://grafana.com/)
- [Alert Manager](https://github.com/prometheus/alertmanager)

## Additional Configurations
### CentOS
When setting up the new CentOS machine, we ran into irregularities compared with the previous setups. Mainly this deals with the node exporter module. In order for this module to work properly on the CentOS machine, the following configuration changes were neccessary:
 - docker-compose file, addition of `path.rootfs` variable
   ```
   node-exporter:
    image: prom/node-exporter
    restart: always
    ports:
      - 9100:9100
    command:
      - '--path.procfs=/host/proc'
      - '--path.sysfs=/host/sys'
      - '--path.rootfs=/rootfs'
    volumes:
      - /proc:/host/proc:ro
      - /sys:/host/sys:ro
      - /:/rootfs:ro

   ```
 - `*.rules` file mountpoint adjustment to reflect docker-compose change:
    ```
     - alert: high_storage_load
    expr: 100 - ((node_filesystem_avail_bytes{mountpoint="/var/lib/docker"}/node_filesystem_size_bytes{mountpoint="/var/lib/docker"}) * 100) > 75
    for: 30s
    labels:
      severity: critical
    annotations:
      summary: "Server storage is almost full on machine 10.145.9.101"
      description: "Docker host storage usage is {{ humanize $value}}%. Reported by instance {{ $labels.instance }} of job {{ $labels.job }}."
    ```