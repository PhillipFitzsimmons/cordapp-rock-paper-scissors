setting=''
for i; do
   if [[ "$setting" = "--server.port" ]]
   then
     serverPort=$i
   fi
   if [[ "$setting" = "--config.rpc.host" ]]
   then
     host=$i
   fi
   if [[ "$setting" = "--config.rpc.port" ]]
   then
     rpcport=$i
   fi
   if [[ "$setting" = "--config.rpc.username" ]]
   then
     username=$i
   fi
   if [[ "$setting" = "--config.rpc.password" ]]
   then
     password=$i
   fi
   setting=$i
done

#sleep 30
echo starting on port $serverPort with rpc host $host on port $rpcport with username $username and password $password
java -jar clients.jar --server.port=$serverPort --config.rpc.host=$host --config.rpc.port=$rpcport --config.rpc.username=$username --config.rpc.password=$password
        