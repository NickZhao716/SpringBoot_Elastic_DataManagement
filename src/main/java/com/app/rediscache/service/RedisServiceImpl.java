package com.app.rediscache.service;

import com.app.rediscache.JsonGraph.*;
import com.app.rediscache.repository.PlanPkgInfoDao;
import jakarta.xml.bind.DatatypeConverter;
import org.apache.logging.log4j.util.StringBuilders;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.rmi.MarshalledObject;
import java.security.MessageDigest;

import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

@Service
public class RedisServiceImpl implements RedisService{
    @Autowired
    PlanPkgInfoDao planPkgInfoDao;

    SecureRandom random = SecureRandom.getInstanceStrong();
    byte[] message = new byte[32];

    public RedisServiceImpl() throws NoSuchAlgorithmException {
    }


    @Override
    public boolean keyExist(String key) {

        return planPkgInfoDao.keyExisted(key);
    }

    @Override
    public String storeObjectToRedisAndMQ(JSONObject jsonContent, String key,Map elasticIndices) throws NoSuchAlgorithmException {
        Graph jsonGraph = new Graph();
        mangeJsonContent(jsonContent,jsonGraph,new NormalEdge("plan",""),elasticIndices);

        storeJsonGraphToRedis(key,jsonGraph);
        String eTag;
        random.nextBytes(message);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        eTag = DatatypeConverter.printHexBinary(digest.digest(message));
        planPkgInfoDao.saveEtag(eTag,key);
        return eTag;
    }

    @Override
    public void storeJsonGraphToRedis(String key,Graph jsonGraph) {
        planPkgInfoDao.saveJsonGraph(key,jsonGraph);
    }


    @Override
    public void mangeJsonContent(JSONObject jsonContent, Graph jsonGraph,NormalEdge fanInEdge,Map elasticIndices) {
        Map jsonContentMap = jsonContent.toMap();
        Map<String,Object> simplePropertySet = new HashMap<>();
        Map<String,Boolean> objectPropertySet = new HashMap<>();
        String node_name = jsonContentMap.get("objectType")+"_"+jsonContentMap.get("objectId");
        Node node = new Node(node_name);
        for(Object key:jsonContentMap.keySet())
        {
            Object mapContent = jsonContentMap.get(key);
            if(mapContent instanceof String || mapContent instanceof Integer)
            {
                simplePropertySet.put((String) key,mapContent);
            }
            else
            {
                objectPropertySet.put((String) key,false);
            }
        }

        planPkgInfoDao.saveObjectInfo(node_name,simplePropertySet);
        jsonGraph.addNode(node_name,node);

        if(fanInEdge!=null)
            node.addFanInEdge(fanInEdge.getName(),fanInEdge);


        for(String key:objectPropertySet.keySet())
        {
            Object subObject = jsonContent.get(key);
            if(subObject instanceof JSONObject){
                JSONObject subJsonObject = (JSONObject) subObject;
                String node_name_next = subJsonObject.get("objectType")+ "_" + subJsonObject.get("objectId");
                mangeJsonContent(subJsonObject,jsonGraph,new NormalEdge(key,node_name),elasticIndices);
                Edge edge = new NormalEdge(key,node_name_next);
                node.addFanOutEdge(key,edge);
                jsonGraph.addFanOutEdge(key,edge);
            }
            else
            {
                JSONArray subJsonObjectArray = (JSONArray) subObject;
                ArrayList<String> node_name_next_list = new ArrayList<>();
                for(Object object:subJsonObjectArray)
                {
                    JSONObject subJsonObject = (JSONObject) object;
                    String node_name_next = subJsonObject.get("objectType")+ "_" + subJsonObject.get("objectId");
                    node_name_next_list.add(node_name_next);
                    mangeJsonContent(subJsonObject,jsonGraph,new NormalEdge(key,node_name),elasticIndices);
                }
                Edge edge = new ForkEdge(key,node_name_next_list);
                node.addFanOutEdge(key,edge);
                jsonGraph.addFanOutEdge(key,edge);
            }
        }

        Map joinInfo = new HashMap();
        joinInfo.put("name",fanInEdge.getName());
        String[] parentInfo =  fanInEdge.getTargetNode().split("_");
        if(!fanInEdge.getTargetNode().isEmpty())
        {
            joinInfo.put("parent",parentInfo[parentInfo.length-1]);
            simplePropertySet.put("plan_join",joinInfo);
        }
        else {
            simplePropertySet.put("plan_join",fanInEdge.getName());
        }
        elasticIndices.put(jsonContentMap.get("objectId"),simplePropertySet);
    }



    @Override
    public ArrayList<Map> resetTopologicalOrder(ArrayList<Map> reversed_topologicalOrder) {
        int left = 0;
        int right= reversed_topologicalOrder.size()-1;
        while(left<right)
        {
            Map temp = reversed_topologicalOrder.get(left);
            reversed_topologicalOrder.set(left,reversed_topologicalOrder.get(right));
            reversed_topologicalOrder.set(right,temp);
            left++;
            right--;
        }
        return reversed_topologicalOrder;
    }


    @Override
    public String getEtag(String key) {
        return planPkgInfoDao.returnEtag(key);
    }

    @Override
    public JSONObject getContent(String key) {
        Graph JsonGraph = planPkgInfoDao.getJsonGraph(key);
        JsonGraph.setPlanPkgInfoDao(planPkgInfoDao);
        return JsonGraph.convertToJson(key);
    }

    @Override
    public String patchContent(JSONObject jsonContent,String key,Map patchInfoSet,StringBuilder indicesToDelete) throws NoSuchAlgorithmException {

        Graph JsonGraph = planPkgInfoDao.getJsonGraph(key);
        managePatchJsonContent(jsonContent,JsonGraph,new NormalEdge("plan",""),patchInfoSet,indicesToDelete);
        planPkgInfoDao.saveJsonGraph(key,JsonGraph);
        String eTag;
        random.nextBytes(message);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        eTag = DatatypeConverter.printHexBinary(digest.digest(message));
        planPkgInfoDao.saveEtag(eTag,key);
        return eTag;
    }

    @Override
    public void managePatchJsonContent(JSONObject jsonContent,Graph jsonGraph,NormalEdge fanInEdge,Map elasticIndices,StringBuilder indicesToDelete) {
        Map jsonContentMap = jsonContent.toMap();
        Map<String,Object> simplePropertySet = new HashMap<>();
        Map<String,Boolean> objectPropertySet = new HashMap<>();
        String node_name = jsonContentMap.get("objectType")+"_"+jsonContentMap.get("objectId");
        for(Object key:jsonContentMap.keySet())
        {
            Object mapContent = jsonContentMap.get(key);
            if(mapContent instanceof String || mapContent instanceof Integer)
            {
                simplePropertySet.put((String) key,mapContent);
            }
            else
            {
                objectPropertySet.put((String) key,false);
            }
        }

        for(String key:objectPropertySet.keySet())
        {
            Object subObject = jsonContent.get(key);
            if(subObject instanceof JSONObject){
                JSONObject subJsonObject = (JSONObject) subObject;
                managePatchJsonContent(subJsonObject,jsonGraph,new NormalEdge(key,node_name),elasticIndices,indicesToDelete);
            }
            else
            {
                JSONArray subJsonObjectArray = (JSONArray) subObject;
                ForkEdge edge = (ForkEdge)jsonGraph.getFanOutEdge(key);
                ArrayList<String> node_name_list = edge.getTargetNode();
                ArrayList<String> node_name_next_list = new ArrayList<>();
                for(Object object:subJsonObjectArray)
                {
                    JSONObject subJsonObject = (JSONObject) object;
                    String node_name_next = subJsonObject.get("objectType")+ "_" + subJsonObject.get("objectId");
                    node_name_next_list.add(node_name_next);
                    mangeJsonContent(subJsonObject,jsonGraph,new NormalEdge(key,node_name),elasticIndices);
                }

                for(String node:node_name_next_list){
                    if(!node_name_list.contains(node))
                        node_name_list.add(node);
                }
                //node_name_list.addAll(node_name_next_list);

                jsonGraph.addTargetNodeToForkEdge(edge.getName(),node_name_list,node_name);
            }
        }

        Map joinInfo = new HashMap();
        Map<String,Object> elasticIndexSet = new HashMap<>(simplePropertySet);
        String[] parentInfo = fanInEdge.getTargetNode().split("_");
        joinInfo.put("name",fanInEdge.getName());
        if(!fanInEdge.getTargetNode().isEmpty())
        {
            joinInfo.put("parent",parentInfo[parentInfo.length-1]);
            elasticIndexSet.put("plan_join",joinInfo);
            elasticIndices.put(jsonContentMap.get("objectId"),elasticIndexSet);
        }



        if(!fanInEdge.getTargetNode().isEmpty())
        {
            String originNodeName = ((NormalEdge)jsonGraph.getFanOutEdge(fanInEdge.getName())).getTargetNode();
            Node originNode = jsonGraph.getNode(originNodeName);
            planPkgInfoDao.updateObjectInfo(originNodeName,simplePropertySet);
            if(!originNode.getName().equals(node_name)){
                jsonGraph.renameNode(originNodeName,node_name,fanInEdge.getTargetNode(),fanInEdge.getName());
                String[] indexInfo = originNodeName.split("_");
                indicesToDelete.append(indexInfo[indexInfo.length-1]).append("$");
            }
            planPkgInfoDao.renameObjectKey(originNodeName,node_name);
        }
    }


    @Override
    public String deleteContent(String key) {
        StringBuilder str = new StringBuilder();
        planPkgInfoDao.deleteEtag(key);
        Graph jsonGraph = planPkgInfoDao.getJsonGraph(key);

        for(String nodeName : jsonGraph.nodes.keySet())
        {
            planPkgInfoDao.deleteContent(nodeName);
            String[] nodeInfo = nodeName.split("_");
            str.append(nodeInfo[nodeInfo.length - 1]).append("$");
        }
        planPkgInfoDao.deleteContent(key+"_"+"GraphNodes");
        planPkgInfoDao.deleteContent(key+"_"+"GraphEdges");
        return str.toString();
    }
}
