package com.app.rediscache.service;

import java.util.Map;

public interface KafkaService {
    public void publish(String message, String operation);
}
