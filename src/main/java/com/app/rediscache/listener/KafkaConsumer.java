package com.app.rediscache.listener;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.app.rediscache.repository.ElasticDao;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ElasticDao elasticDao;

    @KafkaListener(topics = "bigdataindexing", groupId = "group_id")
    public void consume(ConsumerRecord<String, String> record) throws ExecutionException, InterruptedException, IOException {
        logger.info("Consumed Message - {} ", record);
        if (record.key().toString().equals("index")){
            JSONObject recordJson = new JSONObject(record.value());
            elasticDao.postDocument(recordJson.toMap());
        }
        else if (record.key().toString().equals("delete")){
            elasticDao.deleteDocument(record.value().split("\\$"));
        }


        //elasticDao.postDocument(recordJson);
    }

}