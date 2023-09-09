package com.app.rediscache.service;

import com.app.rediscache.JsonGraph.Edge;
import com.app.rediscache.JsonGraph.Graph;
import com.app.rediscache.JsonGraph.NormalEdge;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;

public interface RedisService {



    public boolean keyExist(String key);
    public String storeObjectToRedisAndMQ(JSONObject jsonContent, String key,Map elasticIndices) throws NoSuchAlgorithmException;
    public void storeJsonGraphToRedis(String key,Graph jsonGraph);
    void mangeJsonContent(JSONObject jsonContent, Graph jsonGraph, NormalEdge fanInEdge,Map elasticIndices);
    void managePatchJsonContent(JSONObject jsonContent,Graph jsonGraph,NormalEdge fanInEdge,Map elasticIndices,StringBuilder indicesToDelete);
    ArrayList<Map> resetTopologicalOrder(ArrayList<Map> reversed_topologicalOrder);

    String getEtag(String key);
    JSONObject getContent(String key);

    String patchContent(JSONObject jsonContent,String key,Map elasticIndices,StringBuilder indicesToDelete) throws NoSuchAlgorithmException;

    String deleteContent(String key);
}
