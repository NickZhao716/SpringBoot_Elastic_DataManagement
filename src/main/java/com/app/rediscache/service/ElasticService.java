package com.app.rediscache.service;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

public interface ElasticService {
    void postIndexToElastic() throws IOException;
    public void postDocument(Map<String, Object> MapOfDocuments) throws IOException;
    //public void postDocument(JSONObject jsonObject) throws IOException;
}
