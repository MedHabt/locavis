pipeline{
    agent any
    stages{

        stage("test"){
            steps{
                echo 'testing the application ...'
            }

        stage("build"){
            steps{
                echo 'building the application ...'
                sh 'npm install'
                sh 'npm build'
            }

        stage("deploy"){
            steps{
                echo 'deploying the application ...'
            }

        }
    }
}
