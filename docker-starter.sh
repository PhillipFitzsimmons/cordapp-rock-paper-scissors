echo "starting client"

#sh ./nodes/runnodes

echo "beginning sleep"
#sleep 90
echo "done sleeping"

ALL_CONFIGS=$(find build/nodes -name "*_node.conf")
#echo $ALL_CONFIGS
for filename in $ALL_CONFIGS; do
#cat $filename
    PORT=$(grep 'address=' $filename | sed 's/\(.*localhost\)\(.*\)/\2/' | grep -o '[0-9]\+')
    PASSWORD=$(grep 'password=' $filename | sed 's/\(.*=\)\(.*\)/\2/')
    USER=$(grep 'user=' $filename | sed 's/\(.*=\)\(.*\)/\2/')
    SERVERPORT="8${PORT:1:1}${PORT:3:4}  $PORT"
    
    if [ -z "$PASSWORD" ]
    then
        echo "Notary - not launching client"
    else
        echo serveport $SERVERPORT
    #    java -jar clients.jar --server.port=$SERVERPORT --config.rpc.host=localhost --config.rpc.port=$PORT --config.rpc.username=$USER --config.rpc.password=$PASSWORD
    fi
done



#host.docker.internal for testing locally when nodes are running on local host, outside of docker image
#java -jar clients.jar --server.port=10050 --config.rpc.host=host.docker.internal --config.rpc.port=10006 --config.rpc.username=user1 --config.rpc.password=test
#"production" - localhost because everything's running on the same container
#java -jar clients.jar --server.port=10050 --config.rpc.host=localhost --config.rpc.port=10006 --config.rpc.username=user1 --config.rpc.password=test

#java -jar clients/build/libs/clients-0.1.jar --server.port=10050 --config.rpc.host=localhost --config.rpc.port=10006 --config.rpc.username=user1 --config.rpc.password=test
#docker build -t phillipfitzsimmons/corda-rockpaperscissors:1.0 .
#docker run -p 10050:10050 -p 10006:10006 phillipfitzsimmons/corda-rockpaperscissors:1.0
#docker run --network host phillipfitzsimmons/corda-rockpaperscissors:1.0
