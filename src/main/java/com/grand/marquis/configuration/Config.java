package com.grand.marquis.configuration;

import software.amazon.awssdk.regions.Region;

import java.util.Objects;

public enum Config {

    AWS_ENVIRONMENT("AWS_ENVIRONMENT", "localstack"),
    AWS_REGION("QUEUE_REGION", Region.US_EAST_1.toString()),
    AWS_SQS_QUEUE_NAME("QUEUE_NAME", "default-queue"),
    POLL_INTERVAL("POLL_INTERVAL", "PT5S");
    // Reference -> https://en.wikipedia.org/wiki/ISO_8601#Durations (default 5 seconds)

    private final String key;
    private final String value;
    Config(String k, String v) {
        Objects.requireNonNull(k);
        Objects.requireNonNull(v);
        this.key = k;
        this.value = v;
    }

    private String getKey() {
        return key;
    }

    private String getValue() {
        return value;
    }

    public static String get(Config k) {
        return System.getenv().getOrDefault(k.getKey(), k.getValue());
    }
}
