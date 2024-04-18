package com.primecheck;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.fasterxml.jackson.jr.ob.JSON;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrimeTestingHandler implements RequestHandler<SQSEvent, Void> {

    private final Logger logger = LoggerFactory.getLogger(PrimeTestingHandler.class);

    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        try{
            final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
            final String queueUrl = sqs.getQueueUrl(Constants.QUEUE_NAME).getQueueUrl();
            List<SQSEvent.SQSMessage> messages = sqsEvent.getRecords();
            if(messages.isEmpty()) {
                logger.info("No messages to receive!");
            }
            for (SQSEvent.SQSMessage m : messages) {
                logger.info("Message: " + m.getBody());
                PrimeCandidate primeCandidate = JSON.std.beanFrom(PrimeCandidate.class, m.getBody());
                // process the primeCandidate here
                createPrimeRecord(primeCandidate, isPrime(primeCandidate.getInteger()));
                sqs.deleteMessage(queueUrl, m.getReceiptHandle());
            }
            logger.info("Complete. Processed all the message.");
        } catch (Exception e) {
            logger.error("Error while processing the request", e);
        }
        return null;
    }

    // Algorithm adapted from Wikipedia's Example Code For Primality Testing
    public boolean isPrime(BigInteger integer) {
        if(integer.compareTo(BigInteger.ONE) <= 0) {
            return false;
        }
        if(integer.compareTo(BigInteger.TWO) == 0 || integer.compareTo(BigInteger.valueOf(3L)) == 0) {
            return true;
        }
        if((integer.mod(BigInteger.TWO).equals(BigInteger.ZERO)) || integer.mod(BigInteger.valueOf(3L)).equals(BigInteger.ZERO)) {
            return false;
        }
        for(BigInteger i = BigInteger.valueOf(5L); i.compareTo(integer.sqrt()) <= 0; i = i.add(BigInteger.valueOf(6L))) {
            if(integer.mod(i).equals(BigInteger.ZERO) || integer.mod(i.add(BigInteger.TWO)).equals(BigInteger.ZERO)) {
                logger.info("Not a true prime, divisible by: {}", i.toString());
                return false;
            }
        }
        return true;
    }

    private void createPrimeRecord(PrimeCandidate primeCandidate, boolean isPrime) {
        Map<String, AttributeValue> item_values = new HashMap<>();

        item_values.put("Integer", new AttributeValue(""+primeCandidate.getInteger()));
        item_values.put("IsPrime", new AttributeValue(""+isPrime));

        final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();

        try {
            ddb.putItem(Constants.TABLE_NAME, item_values);
        } catch (ResourceNotFoundException e) {
            logger.error(String.format("Error: the table \"%s\" can't be found.\n", Constants.TABLE_NAME));
            logger.error("Be sure that it exists and that you've typed its name correctly!");
        } catch (AmazonServiceException e) {
            logger.error(e.getMessage());
        }
    }
}
