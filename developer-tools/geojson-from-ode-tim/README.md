## About

The geojson-from-ode tool is a simple way to generate a geojson object given the ODE submitted TIM.

## Prerequisites
To generate the geojson, you will need to have the following:
- `sampledata.json` updated with the submitted TIM you want to view.
    Note that this file can be grabbed directly from the logs. The checked-in `sample.json` comes from the TIM refresh application.

## Installation
You can either install the necessary tools directly on your system or use a development container.

### Option 1: Direct Installation

Ensure you have Python 3 (latest version) and pip3 installed. You can install them as follows:

#### On Ubuntu/Debian:
```sh
sudo apt update
sudo apt install python3 python3-pip
```

#### On macOS:
```sh
brew install python
```

#### On Windows:
Download and install Python from the [official website](https://www.python.org/). Ensure you check the option to add Python to your PATH during installation.

### Option 2: Using the Dev Container

If you prefer to use the development container, follow these steps:

1. Ensure you have Docker installed on your machine. You can download it from the [official website](https://www.docker.com/).
2. Open your project in Visual Studio Code with the Remote - Containers extension.
3. Reopen the project in the provided dev container by clicking on the blue button in the bottom left corner of the window and selecting "Reopen in Container"

## Running

This script runs with Python and can be executed as follows:
```sh
pip3 install -r requirements.txt
python3 ./main.py
```