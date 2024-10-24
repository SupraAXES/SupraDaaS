#/bin/bash -e

current_dir=$(pwd)

image_list="admin portal manager"

for image in $image_list
do
    cd "$current_dir/images/$image"
    docker build -t "supradaas-$image" .
done 

docker pull postgres
docker pull supraaxes/supra-guacd
