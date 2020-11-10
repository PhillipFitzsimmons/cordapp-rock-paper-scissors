echo "starting client"

ALL_CONFIGS=$(find build/nodes -name "*_node.conf")
echo $ALL_CONFIGS
SERVERPORT=8081
for filename in $ALL_CONFIGS; do
    LEGALNAME=$(grep 'LegalName=' $filename | sed 's/\(myLegalName=\)\(.\)/\2/')
    PORT=$(grep 'address=' $filename | sed 's/\(.*localhost\)\(.*\)/\2/' | grep -o '[0-9]\+')
    PASSWORD=$(grep 'password=' $filename | sed 's/\(.*=\)\(.*\)/\2/')
    USER=$(grep 'user=' $filename | sed 's/\(.*=\)\(.*\)/\2/')
    SERVERPORT=`expr $SERVERPORT + 1`
    
    #echo serveport $SERVERPORT $LEGALNAME
    if [ -z "$PASSWORD" ]
    then
        echo "Notary - not launching client" $LEGALNAME
    else
        echo launching server on port $SERVERPORT rpc $PORT user $USER password $PASSWORD $LEGALNAME
        ./docker-starter-java.sh --server.port $SERVERPORT --config.rpc.host localhost --config.rpc.port $PORT --config.rpc.username $USER --config.rpc.password $PASSWORD &
        sleep 30
    fi
done
