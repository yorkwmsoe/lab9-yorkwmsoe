# spring-2024-lab9-aws-lambda 
# Lab9

`mvn clean package` will create 4 .jar files, 3 of which are uploaded to AWS as lambdas.
The Common module contains all the common dependencies to keep the other module pom files smaller.
The use of modules may be overkill for this project, but I wanted to see the difference in startup time between having the leaner code vs the full application in each lambda.
It did seem to help with the startup time, but it's hard to say for sure because I had switched to this method and still needed to do some code updates afterward.
Regardless, the only major difference with using modules will be looking in 3 different target/ folders. 

Included in lab9-yorkmsoe/src/test/res is the postman collection I created for testing this project.
I could not find a way to export a flow, but essentially it's start->test1->delay(17500ms)->test2->...->test8->delay(30000ms)->getPrimes.
I would drop the table and re-create it between tests to clear it out. 
I have dropped it and recreated it without hitting the endpoint for grading purposes. 
The delays exist because on occasion not every candidate number would be ready by the time the getPrimes request was called.
All 8 would eventually show up, but for the purposes of automated testing, that was not desirable. 