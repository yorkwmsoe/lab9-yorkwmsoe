package com.primecheck;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.io.BigIntegerParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;

public class DatabaseDumpingHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Logger logger = LoggerFactory.getLogger(DatabaseDumpingHandler.class);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context contest) {
        logger.info("Received a request");
        try {
            Map<BigInteger, Boolean> primes = getAllRecords(logger);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();
            ArrayNode primesList = rootNode.putArray("primes");
            for (BigInteger i : primes.keySet()) {
                ObjectNode primeNode = primesList.addObject();
                primeNode.put("candidate", i.toString());
                primeNode.put("isPrime", primes.get(i));
                primesList.set(primesList.size() - 1, primeNode);
            }
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(rootNode.toPrettyString());
        } catch (AmazonServiceException e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody("Error while processing request: " + e.getErrorMessage());
        }
    }

    public Map<BigInteger, Boolean> getAllRecords(Logger logger) {
        Map<BigInteger, Boolean> primes = new HashMap<>();
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(Constants.TABLE_NAME);

        ScanResult result= client.scan(scanRequest);
        for (Map<String, AttributeValue> returned_item : result.getItems()) {
            try {
                if(returned_item != null) {
                    Set<String> keys = returned_item.keySet();
                    for(String key: keys) {
                        logger.info(String.format("%s: %s\n",
                                key, returned_item.get(key).toString()));
                    }
                    primes.put(BigIntegerParser.parseWithFastParser(returned_item.get("Integer").getS()), Boolean.parseBoolean(returned_item.get("IsPrime").getS()));
                } else {
                    logger.error("No items found in the table " + Constants.TABLE_NAME+"\n");
                }
            } catch (AmazonServiceException e) {
                logger.error(e.getErrorMessage());
                throw e;
            }
        }
        return primes;
    }
}
