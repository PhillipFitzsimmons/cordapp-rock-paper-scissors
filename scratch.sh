i=1
while ! nc -z localhost 4000; do
    i=`expr $i + 1`
    echo waiting on port 4000 `expr $i \\* 10`
    sleep 2
done
echo port 3000 is open