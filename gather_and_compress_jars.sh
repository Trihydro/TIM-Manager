# create directory
echo "Creating directory"
mkdir jars

# move jars
echo "Moving JARs"
mv ./*/target/*.jar ./jars

# compress jars
echo "Compressing JARs"
tar -czvf wyocv-jars-4-1-2024t10.30.tar.gz ./jars

# done
echo "Done"
