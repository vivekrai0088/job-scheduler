# job-scheduler
Job Scheduler

Steps to follow to run in local:
1. git clone https://github.com/vivekrai0088/job-scheduler.git
2. Go to job-scheduler directory
3. ./gradlew build
4. java -jar ./server/build/libs/server-1.0-SNAPSHOT.jar

Steps to run in docker container :
1. docker build -t job-scheduler .
2. docker run -p 8080:8080 job-scheduler

Swagger Link : http://localhost:8080/swagger-ui.html#/