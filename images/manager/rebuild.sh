basename=$(basename `pwd`)

sudo docker build --network=host -t $basename .
