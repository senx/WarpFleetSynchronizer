#!/usr/bin/env groovy
import hudson.model.*

pipeline {
    agent any
    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '3'))
    }
    environment {
        version = "${getVersion()}"
    }
    stages {
        stage("Checkout") {
            steps {
                this.notifyBuild('STARTED', version)
                git poll: false, url: 'git@gitlab.com:senx/WarpFleetSynchronizer.git'
                sh 'git fetch --tags'
                echo "Building ${version}"
            }
        }

        stage("Package") {
            steps {
                sh "./gradlew clean"
                sh "./gradlew shadowJar"
                archiveArtifacts "build/libs/WarpFleetSynchronizer-all.jar"
            }
        }

        stage("Deploy to prod 7") {
            steps {
                sh "scp build/libs/WarpFleetSynchronizer-all.jar root@172.16.0.7:/opt/www/WarpFleetSynchronizer/."
                sh "ssh root@172.16.0.7 'service warpfleetsync stop'"
                sh "ssh root@172.16.0.7 'service warpfleetsync start'"
            }
        }

        stage("Docker") {
            when {
                expression { return isItATagCommit() }
            }
            options {
                timeout(time: 30, unit: 'DAYS')
            }
            input {
                message 'Build docker?'
            }
            steps {
                sh 'DOCKER_BUILD_KIT=1 DOCKER_CLI_EXPERIMENTAL=enabled docker buildx use multiarch'
                sh "docker system prune --force --all --volumes --filter 'label=maintainer=contact@senx.io'"
                sh "DOCKER_BUILD_KIT=1 DOCKER_CLI_EXPERIMENTAL=enabled docker buildx build --platform linux/amd64,linux/arm64,linux/arm/v7 --push -t registry.gitlab.com/senx/warpfleetsynchronizer:latest -t registry.gitlab.com/senx/warpfleetsynchronizer:$version ."
            }
        }

        stage("Deploy to prod 242") {
            when {
                expression { return isItATagCommit() }
            }
            steps {
                sh "scp build/libs/WarpFleetSynchronizer-all.jar root@172.16.0.242:/opt/www/WarpFleetSynchronizer/."
                sh "ssh root@172.16.0.242 'service warpfleetsync stop'"
                sh "ssh root@172.16.0.242 'service warpfleetsync start'"
                this.notifyBuild('PUBLISHED', version)
            }
        }
        stage('Deploy to DockerHub') {
            when {
                expression { return isItATagCommit() }
            }
            options {
                timeout(time: 2, unit: 'HOURS')
            }
            input {
                message 'Should we deploy to DockerHub?'
            }
            steps {
                sh 'DOCKER_BUILD_KIT=1 DOCKER_CLI_EXPERIMENTAL=enabled docker buildx use multiarch'
                sh "DOCKER_BUILD_KIT=1 DOCKER_CLI_EXPERIMENTAL=enabled docker buildx build --push --platform linux/amd64,linux/arm64,linux/arm/v7 -t warp10io/warpfleetsynchronizer:latest -t warp10io/warpfleetsynchronizer:${version} ."
                sh "docker system prune --force --all --volumes --filter 'label=maintainer=contact@senx.io'"
                this.notifyBuild('PUBLISHED', version)
            }
        }
    }
    post {
        success {
            this.notifyBuild('SUCCESSFUL', version)
        }
        failure {
            this.notifyBuild('FAILURE', version)
        }
        aborted {
            this.notifyBuild('ABORTED', version)
        }
        unstable {
            this.notifyBuild('UNSTABLE', version)
        }
    }
}

void notifyBuild(String buildStatus, String version) {
    // build status of null means successful
    buildStatus = buildStatus ?: 'SUCCESSFUL'
    String subject = "${buildStatus}: Job ${env.JOB_NAME} [${env.BUILD_DISPLAY_NAME}] | ${version}" as String
    String summary = "${subject} (${env.BUILD_URL})" as String
    // Override default values based on build status
    if (buildStatus == 'STARTED') {
        color = 'YELLOW'
        colorCode = '#FFFF00'
    } else if (buildStatus == 'SUCCESSFUL') {
        color = 'GREEN'
        colorCode = '#00FF00'
    } else if (buildStatus == 'PUBLISHED') {
        color = 'BLUE'
        colorCode = '#0000FF'
    } else {
        color = 'RED'
        colorCode = '#FF0000'
    }

    // Send notifications
    this.notifySlack(colorCode, summary, buildStatus)
}

String getParam(String key) {
    return params.get(key)
}

void notifySlack(String color, String message, String buildStatus) {
    String slackURL = getParam('slackUrl')
    String payload = "{\"username\": \"${env.JOB_NAME}\",\"attachments\":[{\"title\": \"${env.JOB_NAME} ${buildStatus}\",\"color\": \"${color}\",\"text\": \"${message}\"}]}" as String
    sh "curl -X POST -H 'Content-type: application/json' --data '${payload}' ${slackURL}" as String
}


String getVersion() {
    return sh(returnStdout: true, script: 'git describe --abbrev=0 --tags').trim()
}


boolean isItATagCommit() {
    String lastCommit = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
    String tag = sh(returnStdout: true, script: "git show-ref --tags -d | grep ^${lastCommit} | sed -e 's,.* refs/tags/,,' -e 's/\\^{}//'").trim()
    return tag != ''
}
