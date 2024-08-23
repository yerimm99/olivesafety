pipeline {
    agent {
        label 'gradle'  // Docker 에이전트
        }

    environment {
        // GitLab 저장소에서 소스 가져오기 위한 자격 증명
        GITLAB_CREDENTIALS_ID = 'gitlab'

        // Harbor 레지스트리 관련 변수
        HARBOR_URL = 'harbor.joon-test.shop'
        HARBOR_REPOSITORY_NAME = 'olivesafety'
        HARBOR_REPOSITORY_URI = "${HARBOR_URL}/${HARBOR_REPOSITORY_NAME}/olivesafety"
        IMAGE_TAG = 'latest'

        // Argo CD 관련 변수
        ARGOCD_SERVER = 'argocd.joon-test.shop'  // 실제 Argo CD 서버 주소로 교체
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
        }

        stage('SonarQube analysis') {
                    steps {
                        // mysonar = jenkins - System - SonarQube servers 이름
                        withSonarQubeEnv('mysonar') {
                            sh './gradlew sonar'
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

        stage('Login to Argo CD') {
            steps {
                script {
                    // Argo CD에 로그인하고 sync
                    withCredentials([usernamePassword(credentialsId: 'argocd', usernameVariable: 'ARGOCD_USERNAME', passwordVariable: 'ARGOCD_PASSWORD')]) {
                        sh "argocd login ${ARGOCD_SERVER} --username ${ARGOCD_USERNAME} --password ${ARGOCD_PASSWORD} --insecure"
                        sh "argocd app sync app"
                    }
                }
            }
        }
    }

    post {
            success {
                slackSend (color: '#00FF00', message: "Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' succeeded.")
                echo 'Pipeline succeeded!'
            }
            failure {
                slackSend (color: '#FF0000', message: "Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' failed.")
                echo 'Pipeline failed!'
            }
        }
}
