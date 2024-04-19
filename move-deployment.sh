# This script assumes that you have a directory called 'deployment'
# at the same level as this script, containing the WyoCV .jar's you'd
# like to deploy. This also assumes that the directory name of each "submodule"
# can be extracted from the name of the .jar to be deployed.
# For example, cv-data-tasks-0.0.2-SNAPSHOT.jar would be deployed to cv-data-tasks/
#
# This script will:
# 1. Delete the previous backup (e.g. cv-data-tasks/cv-data-tasks-0.0.2-SNAPSHOT.jar.bak)
# 2. Backup the existing .jar by appending .bak to the filename
# 3. Move the corresponding .jar from ./deployment to the correct folder 

directory=""
base=/home/wyocvadmin/wyocv
for path in ./deployment/*; do 
    file=$(basename $path)
    # Extract the directory name from the .jar (e.g. cv-data-tasks-0.0.2-SNAPSHOT.jar -> cv-data-tasks)
    directory=$(echo $file | grep -oP '[a-zA-z-]*(?=-\d)') # directory=$(echo $file | grep -o '[a-zA-z-]*')
    # directory=${directory%?}
    rm "$base/$directory/$file.bak"
    mv $base/$directory/$file "$base/$directory/$file.bak"
	mv $path $base/$directory/
    echo "-------- Done moving $file --------"
done