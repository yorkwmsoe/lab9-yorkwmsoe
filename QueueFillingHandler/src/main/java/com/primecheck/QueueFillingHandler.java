package com.primecheck;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.jr.ob.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueFillingHandler {

    private final Logger logger = LoggerFactory.getLogger(QueueFillingHandler.class);
    private final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
    private final String queueUrl = sqs.getQueueUrl(Constants.QUEUE_NAME).getQueueUrl();

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        logger.info("Received a request");
        try {
            PrimeCandidate primeCandidate = JSON.std.beanFrom(PrimeCandidate.class, input.getBody());
            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(input.getBody());
            sqs.sendMessage(sendMessageRequest);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody("Received Prime Candidate: " + primeCandidate.toString());
        } catch (Exception e) {
            logger.error("Error while processing the request", e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody("Error processing the request");
        }
    }
}
