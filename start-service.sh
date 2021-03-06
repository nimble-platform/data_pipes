#!/bin/bash

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


#java -jar ${JAR_PATH} delete --tables --topics ; exit 1

gnome-terminal --geometry 240x16+50+${startWindow} -x bash -c "docker run -it -p 1000:8080  \
-e ENVIRONMENT=${ENVIRONMENT} \
-e POSTGRES_USERNAME=${POSTGRES_USERNAME} \
-e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
-e POSTGRES_URL=${POSTGRES_URL} \
-e MESSAGE_HUB_CREDENTIALS='' \
evgeniyh/data-channels | tee streams.log"

export SERVICE_PATH='localhost:1000'

echo "Waiting for the service to be up"
while [ `curl -s -o /dev/null -w "%{http_code}" ${SERVICE_PATH}/health-check` -ne 200 ] ; do
    echo -n "."
    sleep 1
done
echo

SOURCE="SOURCE_test_user%40test.com"
TARGET="TARGET_test_user%40test.com"
FILTER="%7B%22machineId%22%3A%22machine_id_1%22%7D"

CREATE_URL_QUERY_PARAMS="${SERVICE_PATH}/start-new?source=${SOURCE}&target=${TARGET}&filter=${FILTER}"
echo "Sending create new channel with query params on ${CREATE_URL_QUERY_PARAMS}"
CREATE_RESPONSE=`curl -s -X POST ${CREATE_URL_QUERY_PARAMS}`
echo ${CREATE_RESPONSE}

CHANNEL_ID=`echo ${CREATE_RESPONSE} | jq .channelId`

#SOURCE="SOURCE_test_user@test.com"
#TARGET="TARGET_test_user@test.com"
#FILTER="{\"machineId\":\"machine_id_1\"}"
#
#CREATE_URL_JSON_BODY="${SERVICE_PATH}/start-new"
#echo "Sending create new channel with json body on ${CREATE_URL_JSON_BODY}"
#CREATE_RESPONSE=`curl -H "Content-Type: application/json" -d "{\"source\":\"${SOURCE}\", \"target\":\"${TARGET}\", \"filter\":${FILTER}}" -s -X POST ${CREATE_URL_JSON_BODY}`
#CHANNEL_ID=`echo ${CREATE_RESPONSE} | jq .channelId`

#CHANNEL_ID="9c053bee-d6a4-439e-86a2-0339ea4612ae"

echo "The created channel id is ${CHANNEL_ID}"

startNewTerminalWithProcess "Starting consumer for the channel output topic" "consumer --channelId ${CHANNEL_ID} | tee consumer.log"
startNewTerminalWithProcess "Starting producer for the streams input topic"  "producer --channelId ${CHANNEL_ID} | tee producer.log"

function printTargetsChannelsAndMessages() {
    CHANNELS_RESULT=`curl -s ${SERVICE_PATH}/${1}/channels`
    echo "The channels for ${1} are:"
    echo ${CHANNELS_RESULT} | jq

    MESSAGES_RESULT=`curl -s ${SERVICE_PATH}/${2}/messages`
    echo "The messages for ${2} are:"
    echo ${MESSAGES_RESULT} | jq
}

echo "Printing the initial state for target=${TARGET} and channel=${CHANNEL_ID}"
printTargetsChannelsAndMessages ${TARGET} ${CHANNEL_ID}

echo "Sleeping 15 sec"
sleep 15

echo "Printing state for target=${TARGET} and channel=${CHANNEL_ID}"
printTargetsChannelsAndMessages ${TARGET} ${CHANNEL_ID}

DELETE_RESULT=`curl -s -X DELETE ${SERVICE_PATH}/${CHANNEL_ID}`
echo "Delete result is ${DELETE_RESULT}"

echo "Printing state after DELETE for target=${TARGET} and channel=${CHANNEL_ID}"
printTargetsChannelsAndMessages ${TARGET} ${CHANNEL_ID}

docker stop $(docker ps -aq) > /dev/null
