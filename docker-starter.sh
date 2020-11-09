echo "starting client"

sh ./nodes/runnodes

echo "beginning sleep 0..."
sleep 300
echo "done sleeping ...300"

ALL_CONFIGS=$(find nodes -name "*_node.conf")
echo $ALL_CONFIGS
SERVERPORT=8081
for filename in $ALL_CONFIGS; do
    PORT=$(grep 'address=' $filename | sed 's/\(.*localhost\)\(.*\)/\2/' | grep -o '[0-9]\+')
    PASSWORD=$(grep 'password=' $filename | sed 's/\(.*=\)\(.*\)/\2/')
    USER=$(grep 'user=' $filename | sed 's/\(.*=\)\(.*\)/\2/')
    SERVERPORT=`expr $SERVERPORT + 1`
    
    echo serveport $SERVERPORT
    if [ -z "$PASSWORD" ]
    then
        echo "Notary - not launching client"
    else
        echo launching server on port $SERVERPORT rpc $PORT user $USER password $PASSWORD
        #i=1
        #while ! nc -z localhost $PORT; do
        #    i=`expr $i + 1`
        #    echo waiting on port $PORT `expr $i \\* 10`
        #    sleep 10
        #done
        sleep 30
        echo port $PORT is open. Launching client on port $SERVERPORT
        #java -jar clients.jar --server.port=$SERVERPORT --config.rpc.host=localhost --config.rpc.port=$PORT --config.rpc.username=$USER --config.rpc.password=$PASSWORD
        #sleep 30
        ./docker-starter-java.sh --server.port $SERVERPORT --config.rpc.host localhost --config.rpc.port $PORT --config.rpc.username $USER --config.rpc.password $PASSWORD &
        sleep 5
    fi
done

tail -f /dev/null

#host.docker.internal for testing locally when nodes are running on local host, outside of docker image
#java -jar clients.jar --server.port=10050 --config.rpc.host=host.docker.internal --config.rpc.port=10006 --config.rpc.username=user1 --config.rpc.password=test
#"production" - localhost because everything's running on the same container
#java -jar clients.jar --server.port=10050 --config.rpc.host=localhost --config.rpc.port=10006 --config.rpc.username=user1 --config.rpc.password=test

#java -jar clients/build/libs/clients-0.1.jar --server.port=10050 --config.rpc.host=localhost --config.rpc.port=10006 --config.rpc.username=user1 --config.rpc.password=test
#docker build -t phillipfitzsimmons/corda-rockpaperscissors:1.0 .
#docker run -p 10050:10050 -p 10006:10006 phillipfitzsimmons/corda-rockpaperscissors:1.0
#docker run --network host phillipfitzsimmons/corda-rockpaperscissors:1.0

#./docker-starter-java.sh --server.port 8081 --config.rpc.host localhost --config.rpc.port 10006 --config.rpc.username user1 --config.rpc.password test &
