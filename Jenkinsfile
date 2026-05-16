// ================================================================
//  AI Chatbot Testing Framework — Jenkinsfile
//  Declarative Pipeline
// ================================================================
//
//  Stages:
//    1. Checkout
//    2. Build & Compile
//    3. Smoke Tests
//    4. Functional & API Tests (parallel)
//    5. Negative Tests
//    6. Security Tests
//    7. Performance Tests
//    8. Allure Report
//    9. Notify
//
//  Requirements:
//    - Jenkins with Maven, JDK 11, Chrome, Allure plugins
//    - Credentials: CHATBOT_API_TOKEN, CHATBOT_APP_URL, CHATBOT_API_URL
// ================================================================

pipeline {

    agent any

    tools {
        maven 'Maven-3.9'
        jdk   'JDK-11'
    }

    parameters {
        choice(name: 'BROWSER',   choices: ['chrome', 'firefox', 'edge'],
               description: 'Browser for UI tests')
        booleanParam(name: 'HEADLESS', defaultValue: true,
               description: 'Run browser in headless mode?')
        choice(name: 'TEST_SCOPE',
               choices: ['full', 'smoke', 'functional', 'security', 'performance'],
               description: 'Which test groups to run')
        booleanParam(name: 'RUN_PERFORMANCE', defaultValue: false,
               description: 'Include performance tests (heavy load)?')
    }

    environment {
        APP_BASE_URL    = credentials('CHATBOT_APP_URL')
        API_BASE_URL    = credentials('CHATBOT_API_URL')
        API_VALID_TOKEN = credentials('CHATBOT_API_TOKEN')
        ALLURE_RESULTS  = 'target/allure-results'
        REPORT_DIR      = 'reports'
        SCREENSHOT_DIR  = 'screenshots'
    }

    options {
        timeout(time: 90, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '10'))
        disableConcurrentBuilds()
        timestamps()
        ansiColor('xterm')
    }

    stages {

        // ── Stage 1: Checkout ────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_MSG = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
                    env.GIT_AUTHOR    = sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim()
                }
                echo "Branch: ${env.BRANCH_NAME} | Author: ${env.GIT_AUTHOR}"
                echo "Commit: ${env.GIT_COMMIT_MSG}"
            }
        }

        // ── Stage 2: Build & Compile ─────────────────────────────────────────
        stage('Build & Compile') {
            steps {
                sh 'mvn clean compile test-compile -q --no-transfer-progress'
                echo '✅ Compilation successful'
            }
        }

        // ── Stage 3: Smoke Tests ──────────────────────────────────────────────
        stage('Smoke Tests') {
            steps {
                sh """
                    mvn test \
                      -Dbrowser=${params.BROWSER} \
                      -Dbrowser.headless=${params.HEADLESS} \
                      -Dapp.base.url=${APP_BASE_URL} \
                      -Dapi.base.url=${API_BASE_URL} \
                      -Dapi.valid.token="${API_VALID_TOKEN}" \
                      -Dgroups=smoke \
                      -Dsurefire.suiteXmlFiles=testng.xml \
                      --no-transfer-progress
                """
            }
            post {
                always {
                    allure includeProperties: false,
                           jdk: '',
                           results: [[path: "${ALLURE_RESULTS}"]]
                }
                failure {
                    echo '❌ Smoke tests failed — pipeline halted.'
                    error 'Smoke tests must pass before continuing.'
                }
            }
        }

        // ── Stage 4: Functional & API (Parallel) ─────────────────────────────
        stage('Functional & API Tests') {
            when { expression { params.TEST_SCOPE in ['full', 'functional'] } }
            parallel {

                stage('Functional Tests') {
                    steps {
                        sh """
                            mvn test \
                              -Dbrowser=${params.BROWSER} \
                              -Dbrowser.headless=${params.HEADLESS} \
                              -Dapp.base.url=${APP_BASE_URL} \
                              -Dapi.base.url=${API_BASE_URL} \
                              -Dapi.valid.token="${API_VALID_TOKEN}" \
                              -Dgroups=functional \
                              -Dsurefire.suiteXmlFiles=testng.xml \
                              --no-transfer-progress
                        """
                    }
                }

                stage('API Tests') {
                    steps {
                        sh """
                            mvn test \
                              -Dapi.base.url=${API_BASE_URL} \
                              -Dapi.valid.token="${API_VALID_TOKEN}" \
                              -Dgroups=api \
                              -Dsurefire.suiteXmlFiles=testng.xml \
                              --no-transfer-progress
                        """
                    }
                }
            }
        }

        // ── Stage 5: Negative Tests ───────────────────────────────────────────
        stage('Negative Tests') {
            when { expression { params.TEST_SCOPE in ['full', 'functional'] } }
            steps {
                sh """
                    mvn test \
                      -Dbrowser=${params.BROWSER} \
                      -Dbrowser.headless=${params.HEADLESS} \
                      -Dapp.base.url=${APP_BASE_URL} \
                      -Dapi.base.url=${API_BASE_URL} \
                      -Dapi.valid.token="${API_VALID_TOKEN}" \
                      -Dgroups=negative \
                      -Dsurefire.suiteXmlFiles=testng.xml \
                      --no-transfer-progress
                """
            }
        }

        // ── Stage 6: Security Tests ───────────────────────────────────────────
        stage('Security Tests') {
            when { expression { params.TEST_SCOPE in ['full', 'security'] } }
            steps {
                sh """
                    mvn test \
                      -Dbrowser=${params.BROWSER} \
                      -Dbrowser.headless=${params.HEADLESS} \
                      -Dapp.base.url=${APP_BASE_URL} \
                      -Dapi.base.url=${API_BASE_URL} \
                      -Dapi.valid.token="${API_VALID_TOKEN}" \
                      -Dapi.invalid.token="Bearer invalid_token" \
                      -Dapi.expired.token="Bearer expired_token" \
                      -Dgroups=security \
                      -Dsurefire.suiteXmlFiles=testng.xml \
                      --no-transfer-progress
                """
            }
        }

        // ── Stage 7: Performance Tests ────────────────────────────────────────
        stage('Performance Tests') {
            when {
                anyOf {
                    expression { params.RUN_PERFORMANCE == true }
                    expression { params.TEST_SCOPE == 'performance' }
                    triggeredBy 'TimerTrigger'
                }
            }
            steps {
                sh """
                    mvn test \
                      -Dapi.base.url=${API_BASE_URL} \
                      -Dapi.valid.token="${API_VALID_TOKEN}" \
                      -Dgroups=performance \
                      -Dsurefire.suiteXmlFiles=testng.xml \
                      --no-transfer-progress
                """
            }
        }

        // ── Stage 8: Allure Report ────────────────────────────────────────────
        stage('Generate Allure Report') {
            steps {
                allure includeProperties: true,
                       jdk: '',
                       results: [[path: "${ALLURE_RESULTS}"]]
                echo '📊 Allure report generated.'
            }
        }

    } // end stages

    post {

        always {
            // Archive screenshots and logs
            archiveArtifacts artifacts: 'screenshots/**/*.png, logs/**/*.log, reports/**/*',
                             allowEmptyArchive: true

            // Publish TestNG results
            publishHTML(target: [
                allowMissing:          true,
                alwaysLinkToLastBuild: true,
                keepAll:               true,
                reportDir:             'reports',
                reportFiles:           'ExtentReport.html',
                reportName:            'Extent Test Report'
            ])
        }

        success {
            echo "✅ Pipeline PASSED — Branch: ${env.BRANCH_NAME}"
        }

        failure {
            echo "❌ Pipeline FAILED — Branch: ${env.BRANCH_NAME}"
            emailext(
                subject: "❌ Chatbot Tests FAILED — ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    <h2>Build Failed</h2>
                    <p><b>Job:</b> ${env.JOB_NAME}</p>
                    <p><b>Build:</b> #${env.BUILD_NUMBER}</p>
                    <p><b>Branch:</b> ${env.BRANCH_NAME}</p>
                    <p><b>Author:</b> ${env.GIT_AUTHOR}</p>
                    <p><b>Commit:</b> ${env.GIT_COMMIT_MSG}</p>
                    <p><a href="${env.BUILD_URL}allure">View Allure Report</a></p>
                    <p><a href="${env.BUILD_URL}console">View Console Log</a></p>
                """,
                to: '${DEFAULT_RECIPIENTS}',
                mimeType: 'text/html'
            )
        }

        unstable {
            echo "⚠️ Pipeline UNSTABLE — some tests failed."
        }

        cleanup {
            cleanWs()
        }
    }
}
