#!/usr/bin/env bash

#!/usr/bin/env bash

function compile() {
    if [ "$1" == "only-build" ] ; then
        echo "Only building the project"
        mvn clean compile assembly:single | grep "SUCCESS"

        echo "Building docker image"
        docker build . -t data-pipes
        exit 0
    fi
    if [ "$1" != "build" ] ; then
        echo "Running without build"
        return;
    fi

    echo "Compiling the jar"
    mvn clean compile assembly:single | grep "SUCCESS"

    if [ $? -ne 0 ] ; then
        echo "Failed to build the jar"
        exit 1
    fi
}

function startNewTerminalWithProcess() {
    echo ${1}
    startWindow=$(( 20 + count * 340))
    let count+=1
    gnome-terminal --geometry 240x16+50+${startWindow} -x bash -c "echo ${1} ; java -jar ${JAR_PATH} ${2}"
}

compile $1

export count=0
export POSTGRES_USERNAME=''
export POSTGRES_PASSWORD=''
export POSTGRES_URL=''

export JAR_PATH="target/data_pipes.jar"

export MESSAGE_HUB_CREDENTIALS=''

echo "Starting new data pipe demo"
echo "Creating new DB record for the data filtering"

FILTER_ID=`java -jar ${JAR_PATH} --create-data-pipe-db`
#FILTER_ID='30ff97d9-f040-4e76-8267-c9434e7bf2c1'

echo "The created filter UID is $FILTER_ID"

startNewTerminalWithProcess "Starting kafka streams" "--start-streams"

startNewTerminalWithProcess "Starting consumer - for filtered" "--start-consumer filtered"
startNewTerminalWithProcess "Starting consumer - for non-filtered" "--start-consumer non-filtered"

startNewTerminalWithProcess "Starting producer for sending data" "--start-producer --uuid=$FILTER_ID"