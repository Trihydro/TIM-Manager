# Nginx Proxy
This directory contains the resources needed to run the Nginx proxy server.

## Diagram
The following diagram shows the context of the Nginx proxy server in the CVManager application.

![Nginx Proxy Diagram](./diagram/cvmanager-proxy-setup.drawio.png)

## Resources
The following resources are available in this directory:
- `Dockerfile`: The Dockerfile used to build the Nginx proxy server image.
- `gen-dhparam.sh`: A script to generate the Diffie-Hellman parameters for the Nginx proxy server.
- `nginx-ssl.conf`: The Nginx configuration file for the SSL settings.