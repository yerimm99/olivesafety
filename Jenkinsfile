pipeline {
    agent any

    environment {
        // GitLab 저장소에서 소스 가져오기 위한 자격 증명
        GITLAB_CREDENTIALS_ID = 'gitlab'

        // AWS ECR 레지스트리 관련 변수
        AWS_REGION = 'ap-northeast-2'
        ECR_REPOSITORY_NAME = 'olivesafety'
        ECR_REPOSITORY_URI = '975050178695.dkr.ecr.ap-northeast-2.amazonaws.com'
        ECR_LOGIN_COMMAND = "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPOSITORY_URI}"
        IMAGE_TAG = "${GIT_COMMIT}"
    }

    stages {
        stage('Checkout') {
            steps {
                // GitLab 저장소에서 소스 가져오기
                checkout([$class: 'GitSCM',
                    userRemoteConfigs: [[url: 'https://gitlab.joon-test.shop/olivesafety/project.git', credentialsId: "${GITLAB_CREDENTIALS_ID}"]],
                    branches: [[name: '*/main']]
                ])
            }
        }


    stage('Gradle Jar Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean bootJar'
            }
            post {
                    failure {
                      echo 'Gradle jar build failure!'
                    }
                    success {
                      echo 'Gradle jar build success!'
                    }
            }

    }


        stage('Build Docker Image') {
            steps {
                script {
                    // ECR 레지스트리에 로그인
                    sh "${ECR_LOGIN_COMMAND}"

                    // Docker 이미지 빌드
                    sh "docker build -t ${ECR_REPOSITORY_NAME}:${IMAGE_TAG} ."
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    // Docker 이미지 푸시
                    sh "docker tag ${ECR_REPOSITORY_NAME}:${IMAGE_TAG} ${ECR_REPOSITORY_URI}/${ECR_REPOSITORY_NAME}:${IMAGE_TAG}"
                    sh "docker push ${ECR_REPOSITORY_URI}/${ECR_REPOSITORY_NAME}:${IMAGE_TAG}"
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline succeeded!'
        }

        failure {
            echo 'Pipeline failed!'
        }
    }
}
