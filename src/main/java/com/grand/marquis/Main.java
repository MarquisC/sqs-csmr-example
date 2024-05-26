package com.grand.marquis;

import com.grand.marquis.aws.SqsConsumer;
import com.grand.marquis.configuration.Config;
import io.smallrye.mutiny.Context;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {

        String envAwsEnvironment = Config.get(Config.AWS_ENVIRONMENT);
        logger.info(String.format("AWS Environment is [%s].", envAwsEnvironment));
        String envAwsRegion = Config.get(Config.AWS_REGION);
        logger.info(String.format("AWS Region is [%s].", envAwsRegion));
        Region awsRegion = Region.of(envAwsRegion);
        String envQueueName = Config.get(Config.AWS_SQS_QUEUE_NAME);
        logger.info(String.format("AWS SQS Queue Name is [%s].", envQueueName));
        SqsClient client = SqsConsumer.createLocalAWSClient();
        String queueUrl = SqsConsumer.getAwsSQSQueueUrl(client, envQueueName);
        SqsConsumer consumer = new SqsConsumer(client, queueUrl);

        Context context = Context.empty();
        Multi<List<Message>> stream = consumer.getSqsStream(10,
                Duration.parse(Config.get(Config.POLL_INTERVAL))).
                runSubscriptionOn(Infrastructure.getDefaultWorkerPool());

        stream.subscribe().with(context, item -> item.forEach( element -> {
            logger.info(String.format("Element's contents are: [%s], [%s] - with attributes [%s] ",
                    element.body(),
                    element.receiptHandle().substring(0, 5),
                    element.attributesAsStrings()
                    .keySet().stream().map(key -> key + "=" + element.attributesAsStrings().get(key))
                    .collect(Collectors.joining(", ", "{", "}"))));


            boolean shouldDelete = ThreadLocalRandom.current().nextInt(0, 100) % 2 == 0;
            if(shouldDelete) {
                DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest
                        .builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(element.receiptHandle())
                        .build();

                DeleteMessageResponse deleteMessageResponse = client.deleteMessage(deleteMessageRequest);
                logger.info(String.format("Message with receipt [%s] was attempted to" +
                        " be deleted with http status code [%d]", element.receiptHandle().substring(0, 5), deleteMessageResponse.sdkHttpResponse().statusCode()));
            }
        }));
    }
}