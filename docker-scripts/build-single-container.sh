docker-compose stop $1
docker-compose rm -f -v $1
docker-compose up --build -d $1
docker-compose ps
