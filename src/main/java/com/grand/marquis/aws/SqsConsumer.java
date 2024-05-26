package com.grand.marquis.aws;


import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class SqsConsumer {

    private static final Logger logger = Logger.getLogger(SqsConsumer.class.getName());
    private final SqsClient client;
    private final String queueUrl;

    private SqsConsumer(){this(null, null);}

    public SqsConsumer(SqsClient c, String queueUrl) {
        Objects.requireNonNull(c);
        Objects.requireNonNull(queueUrl);
        this.client = c;
        this.queueUrl = queueUrl;
    }

    public List<Message> getMessages(ReceiveMessageRequest req) {
        return this.client.receiveMessage(req).messages();
    }

    public List<Message> getMessages(int messageReadLimit) {
        logger.info(String.format("Executing getMessages retrieving [%d] messages per invocation", messageReadLimit));
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(this.queueUrl)
                .maxNumberOfMessages(messageReadLimit)
                .build();

        return this.getMessages(receiveMessageRequest);
    }

    public Uni<List<Message>> getMessagesUni(int messageReadLimit) {
        return Uni.createFrom().item(() -> this.getMessages(messageReadLimit));
    }

    public Multi<List<Message>> getSqsStream(int messageReadLimit, Duration pollInterval) {
        return this.getMessagesUni(messageReadLimit).repeat().withDelay(pollInterval).indefinitely();
    }

    public static SqsClient createLocalAWSClient() {
            logger.info("Creating AWS client for localstack integration.");
            return SqsClient
                    .builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(() -> new AwsCredentials() {
                        @Override
                        public String accessKeyId() {
                            return "test";
                        }

                        @Override
                        public String secretAccessKey() {
                            return "test";
                        }
                    })
                    .endpointOverride(URI.create("https://localhost.localstack.cloud:4566"))
                    .build();
    }

    public static String getAwsSQSQueueUrl(SqsClient client, String queueName) {
        ListQueuesRequest listQueuesRequest = ListQueuesRequest.builder().queueNamePrefix(queueName.split("-")[0]).build();
        ListQueuesResponse listQueuesResponse = client.listQueues(listQueuesRequest);
        return listQueuesResponse.queueUrls().get(0);
    }

}
