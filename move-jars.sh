#!/bin/bash
# This script moves all .jar files in the target directory of each subdirectory to the subdirectory itself
# This is necessary because the Dockerfiles expect the .jar files to be in the same directory as the Dockerfile

# for each directory, move (directory)/target/*.jar to (directory)/
for d in */; do
  # not all directories have a target directory, so we need to check for its existence
    if [ -d "$d/target" ]; then
        mv "$d/target"/*.jar "$d"
    fi
done