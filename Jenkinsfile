pipeline {
    agent any

    environment {
        ECR_REGISTRY = "143555787778.dkr.ecr.ap-northeast-2.amazonaws.com"
        IMAGE_NAME = "my-project-spring"
        AWS_REGION = "ap-northeast-2"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'development', credentialsId: 'github-token',
                    url: 'https://github.com/novolunteer/final_spring.git'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean build -x test'
            }
        }

        stage('Docker Build & Push') {
            steps {
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    sh '''
                        aws ecr get-login-password --region $AWS_REGION | \
                        docker login --username AWS --password-stdin $ECR_REGISTRY

                        docker build -t $ECR_REGISTRY/$IMAGE_NAME:$IMAGE_TAG .
                        docker tag $ECR_REGISTRY/$IMAGE_NAME:$IMAGE_TAG $ECR_REGISTRY/$IMAGE_NAME:latest
                        docker push $ECR_REGISTRY/$IMAGE_NAME:$IMAGE_TAG
                        docker push $ECR_REGISTRY/$IMAGE_NAME:latest
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sshagent(['ec2-ssh-key']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ubuntu@10.0.3.46 "
                            cd ~ &&
                            aws ecr get-login-password --region ap-northeast-2 | \
                            docker login --username AWS --password-stdin 143555787778.dkr.ecr.ap-northeast-2.amazonaws.com &&
                            docker-compose pull spring &&
                            docker-compose up -d --no-deps spring
                        "
                        ssh -o StrictHostKeyChecking=no ubuntu@10.0.4.39 "
                            cd ~ &&
                            aws ecr get-login-password --region ap-northeast-2 | \
                            docker login --username AWS --password-stdin 143555787778.dkr.ecr.ap-northeast-2.amazonaws.com &&
                            docker-compose pull spring &&
                            docker-compose up -d --no-deps spring
                        "
                    '''
                }
            }
        }
    }

    post {
        success {
            echo '✅ Spring 배포 성공!'
        }
        failure {
            echo '❌ Spring 배포 실패!'
        }
        always {
            sh 'docker image prune -f'
        }
    }
}