pipeline {
    agent any

    environment {
        // GitLab 저장소에서 소스 가져오기 위한 자격 증명
        GITLAB_CREDENTIALS_ID = 'gitlab'

        // Harbor 레지스트리 관련 변수
        HARBOR_URL = 'harbor.joon-test.shop'
        HARBOR_REPOSITORY_NAME = 'olivesafety'
        HARBOR_REPOSITORY_URI = "${HARBOR_URL}/${HARBOR_REPOSITORY_NAME}/olivesafety"
        IMAGE_TAG = 'latest'
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

        stage('SonarQube Analysis') {
            withSonarQubeEnv() {
              sh "./gradlew sonar"
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

        stage('Build and Push Docker Image') {
            steps {
                script {
                    // Docker 이미지 빌드
                    sh "docker build -t ${HARBOR_REPOSITORY_NAME}:${IMAGE_TAG} ."

                    // Docker 이미지 태그 추가
                    sh "docker tag ${HARBOR_REPOSITORY_NAME}:${IMAGE_TAG} ${HARBOR_REPOSITORY_URI}:${IMAGE_TAG}"

                    // Harbor 레지스트리에 로그인 및 Docker 이미지 푸시
                    withCredentials([usernamePassword(credentialsId: 'harbor', usernameVariable: 'HARBOR_USERNAME', passwordVariable: 'HARBOR_PASSWORD')]) {
                        sh "echo ${HARBOR_PASSWORD} | docker login ${HARBOR_URL} --username ${HARBOR_USERNAME} --password-stdin"
                        sh "docker push ${HARBOR_REPOSITORY_URI}:${IMAGE_TAG}"
                    }
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
