# This script assumes that you have a directory called 'deployment'
# at the same level as this script, containing the WyoCV .jar's you'd
# like to deploy. This also assumes that the directory name of each "submodule"
# can be extracted from the name of the .jar to be deployed.
# For example, cv-data-tasks-X.X.X-SNAPSHOT.jar would be deployed to cv-data-tasks/
#
# This script will:
# 1. Delete the previous backup (e.g. cv-data-tasks/cv-data-tasks-X.X.X-SNAPSHOT.jar.bak)
# 2. Backup the existing .jar & Dockerfile by appending .bak to the filenames
# 3. Move the corresponding .jar from ./deployment to the correct folder
# 4. Update the Dockerfile with the new version

directory=""
base=/home/wyocvadmin/wyocv
for path in ./deployment/*; do 
    file=$(basename $path)
    # Extract the directory name from the .jar (e.g. cv-data-tasks-X.X.X-SNAPSHOT.jar -> cv-data-tasks)
    directory=$(echo $file | grep -oP '[a-zA-z-]*(?=-\d)') # directory=$(echo $file | grep -o '[a-zA-z-]*')

    # 1. Delete previous backups
    rm $base/$directory/*.bak

    # Get current JAR
    current_jar=$(ls $base/$directory/$directory-*.jar)

    # Extract current & new versions from JARs (e.g. cv-data-tasks-X.X.X-SNAPSHOT.jar -> X.X.X-SNAPSHOT)
    current_version=$(echo $current_jar | grep -oP '(?<=-)\d.*(?=.jar)')
    new_version=$(echo $file | grep -oP '(?<=-)\d.*(?=.jar)')

    # 2. Create backups of the current JAR and Dockerfile
    mv $current_jar $current_jar.bak
    cp $base/$directory/Dockerfile $base/$directory/Dockerfile.bak

    # 3. Move the new JAR to the correct directory
	mv $path $base/$directory/

    # 4. Update Dockerfile with new version
    sed -i "s/$current_version/$new_version/g" $base/$directory/Dockerfile

    # inform the user of the changes
    echo "-------- $directory: $current_version -> $new_version --------"
done