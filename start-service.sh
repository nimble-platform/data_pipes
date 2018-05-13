#!/bin/bash
docker stop $(docker ps -aq) > /dev/null


function compile() {
    if [ "$1" == "build" ] ; then
        echo "Building the project"
        mvn clean install war:war compile assembly:single | grep "SUCCESS"

        if [ $? -ne 0 ] ; then
            echo "Failed to build the jar"
            exit 1
        fi
        echo "Building docker image"
        docker build . -t evgeniyh/data-channels | grep "Successfully"
        if [ $? -ne 0 ] ; then
            echo "Failed to build the image"
            exit 1
        fi
    fi
}

function startNewTerminalWithProcess() {
    echo ${1}
    sleep 1
    startWindow=$(( 20 + count * 360))
    gnome-terminal --geometry 240x16+50+${startWindow} -x bash -c "echo ${1} ; java -jar ${JAR_PATH} ${2}"
    let count+=1
}

compile $1

export startWindow=$(( 20 + 0 * 360))
export count=1


export POSTGRES_USERNAME=""
export POSTGRES_PASSWORD=""
export POSTGRES_URL=""

export JAR_PATH="target/data_pipes.jar"
export ENVIRONMENT="dev"
export MESSAGE_HUB_CREDENTIALS=''


#java -jar ${JAR_PATH} delete --tables ; exit 1

gnome-terminal --geometry 240x16+50+${startWindow} -x bash -c "docker run -it -p 1000:8080  \
-e ENVIRONMENT=${ENVIRONMENT} \
-e POSTGRES_USERNAME=${POSTGRES_USERNAME} \
-e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
-e POSTGRES_URL=${POSTGRES_URL} \
-e MESSAGE_HUB_CREDENTIALS='' \
evgeniyh/data-channels | tee streams.log"

SERVICE_PATH='localhost:1000'

echo "Waiting for the service to be up"
while [ `curl -s -o /dev/null -w "%{http_code}" ${SERVICE_PATH}/health-check` -ne 200 ] ; do
    echo -n "."
    sleep 1
done
echo

SOURCE="SOURCE_test_user%40test.com"
TARGET="TARGET_test_user%40test.com"

CREATE_URL="${SERVICE_PATH}/start-new?source=${SOURCE}&target=${TARGET}&filter=%7B%22machineId%22%3A%22machine_id_1%22%7D"
echo "Sending create new channel id on ${CREATE_URL}"

CHANNEL_ID=`curl -s -X POST ${CREATE_URL}`
#CHANNEL_ID="2656e3bc-ab89-4518-8d25-8cbe279ba78d"

echo "The created channel id is ${CHANNEL_ID}"
#curl -s -X POST ${SERVICE_PATH}/start-new/existing?c_id=${CHANNEL_ID}

startNewTerminalWithProcess "Starting consumer for the channel output topic" "consumer --channelId ${CHANNEL_ID} | tee consumer.log"
startNewTerminalWithProcess "Starting producer for the streams input topic"  "producer --channelId ${CHANNEL_ID} | tee producer.log"

#java -jar ${JAR_PATH} delete --tables
