cat common_names.txt | 
while read line; do 
sed "s/COMMON_NAME/$line/g" generate_named_relationships.cql >> neo-data/import/tmp.cql
done