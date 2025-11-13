pipeline {
    agent {label "java_agent"}

    environment{
        ECR_URI=credentials('ECR_URI')
        AWS_CREDS = credentials('aws-creds')
        AWS_DEFAULT_REGION='ap-south-1'
        SERVER_IP=credentials('SERVER_IP')

    }

    stages {

        stage('AWS Login') {
            
            steps {
            
                sh '''
                aws configure set aws_access_key_id  $AWS_ACCESS_KEY_ID
                aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY
                aws configure set region $AWS_DEFAULT_REGION
                aws sts get-caller-identity

                '''
            
        }
        }


        stage('Build') {
            steps {
                sh '''
                export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
                export PATH=$JAVA_HOME/bin:$PATH
                mvn clean install -DskipTests
                '''
            }

        }

        stage('Sonarqube static code analysis') {
            steps {
                withSonarQubeEnv('Sonar') { 
               sh '''
                mvn sonar:sonar \
                    -Dsonar.projectKey=inventory_factory_management \
                    -Dsonar.host.url=$SONAR_HOST_URL \
                    -Dsonar.login=$SONAR_AUTH_TOKEN
            '''
            }}

        }

        stage('Build docker image and push to ECR') {
            steps {
                sh """
                    aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin ${ECR_URI}
                    sudo docker build -t iosbackend:${env.BUILD_NUMBER} .
                    sudo docker tag iosbackend:${env.BUILD_NUMBER} ${ECR_URI}/iosbackend:${env.BUILD_NUMBER}
                    sudo docker push ${ECR_URI}/iosbackend:${env.BUILD_NUMBER}

                """
            }

        }

        
        stage('Docker container run') {
    steps {
        withCredentials([string(credentialsId: 'ios_creds', variable: 'IOS_CREDS')]) {
            sh '''#!/bin/bash
            export $(echo "$IOS_CREDS" | xargs)

            sudo ssh -o StrictHostKeyChecking=no -i /home/ubuntu/new-key ubuntu@${SERVER_IP}  \"
                aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin ${ECR_URI}
                sudo docker pull ${ECR_URI}/iosbackend:${BUILD_NUMBER}
                sudo docker stop javacont || true
                sudo docker rm javacont || true
                sudo docker run -d --name javacont -p 8080:8080 \
                    -e MAIL_HOST=$MAIL_HOST \
                    -e MAIL_PORT=$MAIL_PORT \
                    -e MAIL_USERNAME=$MAIL_USERNAME \
                    -e MAIL_PASS=$MAIL_PASS \
                    -e SPRING_DATASOURCE_URL=$SPRING_DATASOURCE_URL \
                    -e SPRING_DATASOURCE_USERNAME=$SPRING_DATASOURCE_USERNAME \
                    -e SPRING_DATASOURCE_PASSWORD=$SPRING_DATASOURCE_PASSWORD \
                    -e JWT_SECRET=$JWT_SECRET \
                    -e CLOUDINARY_CLOUD_NAME=$CLOUDINARY_CLOUD_NAME \
                    -e CLOUDINARY_API_KEY=$CLOUDINARY_API_KEY \
                    -e CLOUDINARY_SECRET_KEY=$CLOUDINARY_SECRET_KEY \
                    ${ECR_URI}/iosbackend:${BUILD_NUMBER}
            \"
            '''
        }
    }
}


        

    }

    

}