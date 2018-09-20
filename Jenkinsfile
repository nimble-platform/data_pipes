node ('nimble-jenkins-slave') {
    stage('Download Latest') {
        git(url: 'https://github.com/nimble-platform/data_pipes.git', branch: env.BRANCH_NAME)
    }

    stage('Build Java') {
        sh 'mvn clean install -DskipTests'
    }

    if (env.BRANCH_NAME == 'staging') {
        stage('Build Docker') {
            sh 'docker build -t nimbleplatform/data-pipes:staging .'
        }

        stage('Push Docker') {
            sh 'docker push nimbleplatform/data-pipes:staging'
        }

//        stage('Deploy') {
//            sh 'ssh staging "cd /srv/nimble-staging/ && ./run-staging.sh restart-single identity-service"' // ToDo: implement
//        }
    }

    if (env.BRANCH_NAME == 'master') {
        stage('Build Docker') {
            sh 'docker build -t nimbleplatform/data-pipes:latest .'
        }

        stage('Push Docker') {
            sh 'docker push nimbleplatform/data-pipes:latest'
        }

//        stage('Deploy') {
//            sh 'ssh nimble "cd /data/deployment_setup/prod/ && sudo ./run-prod.sh restart-single identity-service"' // ToDo: implement
//        }
    }

    // Kubernetes deployment is disabled for the time being
//    stage ('Deploy') {
//        sh ''' sed -i 's/IMAGE_TAG/'"$BUILD_NUMBER"'/g' kubernetes/deploy.yaml '''
//        sh 'kubectl apply -f kubernetes/deploy.yaml -n prod --validate=false'
//        sh 'kubectl apply -f kubernetes/svc.yaml -n prod --validate=false'
//    }
//
//    stage ('Print-deploy logs') {
//        sh 'sleep 60'
//        sh 'kubectl -n prod logs deploy/data-channels -c data-channels'
//    }
}