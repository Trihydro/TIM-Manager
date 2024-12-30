# create directory
echo "Creating directory"
mkdir jars

# move jars
echo "Moving JARs"
mv ./*/target/*.jar ./jars

# compress jars
echo "Compressing JARs"
date=$(date '+%Y%m%dT%H.%M')
tar -czvf timm-jars-$date.tar.gz ./jars

# done
echo "Done"
