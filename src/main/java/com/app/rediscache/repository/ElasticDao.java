package com.app.rediscache.repository;


import org.elasticsearch.common.xcontent.XContentBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public interface ElasticDao {
     static final String INDEX_NAME = "plan";
    public void createElasticIndex() throws IOException;
    XContentBuilder getMapping() throws IOException;

   // public void postDocument(JSONObject plan) throws IOException;
    public void postDocument(Map<String, Object> MapOfDocuments) throws IOException;
    public void deleteDocument(String[] list) throws IOException;

}
