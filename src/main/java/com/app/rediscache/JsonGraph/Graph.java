package com.app.rediscache.JsonGraph;

import com.app.rediscache.repository.PlanPkgInfoDao;
import lombok.Data;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class Graph implements Serializable {
    PlanPkgInfoDao planPkgInfoDao;

    public HashMap<String,Node> nodes;
    public HashMap<String,Edge> fanOutEdges;

    public Graph(){
        nodes = new HashMap<>();
        fanOutEdges = new HashMap<>();
    }
    public Graph(HashMap<String,Node> nodes,HashMap<String,Edge> fanOutEdges){
        this.nodes = nodes;
        this.fanOutEdges = fanOutEdges;
    }

    public void setPlanPkgInfoDao(PlanPkgInfoDao planPkgInfoDao) {
        this.planPkgInfoDao = planPkgInfoDao;
    }

    public Node getNode(String key){
        return nodes.get(key);
    }
    public void addNode(String name,Node node){
        nodes.put(name,node);
    }
    public Edge getFanOutEdge(String key){return fanOutEdges.get(key);}
    public void renameNode(String oldName,String newName,String parentNodeName,String edgeName){
        Node oldNode = getNode(oldName);
        addNode(newName,new Node(newName,oldNode.getFanOut(),oldNode.getFanIn()));
        nodes.remove(oldName);
        ((NormalEdge)nodes.get(parentNodeName).getFanOutEdge(edgeName)).setTargetNode(newName);
        ((NormalEdge)fanOutEdges.get(edgeName)).setTargetNode(newName);
    }
    public void addTargetNodeToForkEdge(String edgeName,ArrayList<String> newList,String nodeName){
        ForkEdge edge = (ForkEdge) fanOutEdges.get(edgeName);
        edge.resetTargetNode(newList);
        edge = (ForkEdge) nodes.get(nodeName).getFanOutEdge(edgeName);
        edge.resetTargetNode(newList);

    }

    public void addFanOutEdge(String key,Edge edge) {
        fanOutEdges.put(key,edge);
    }

    public HashMap<String,Node> getNodes(){
        return nodes;
    }
    public HashMap<String,Edge> getFanOutEdges(){return fanOutEdges;}
    public JSONObject convertToJson(String key){
        Map JsonContentMap = new HashMap();
        dfs(JsonContentMap,key);
        return new JSONObject(JsonContentMap);
    }

    public void dfs(Map jsonMap, String key){
        Node node = getNode(key);
        HashMap<String,Edge> fanOut = node.getFanOut();
        for(String edgeKey:fanOut.keySet()){
            Object edge = fanOut.get(edgeKey);
            String tag = ((Edge) edge).getName();
            if(edge instanceof NormalEdge){
                Map subJsonMap = new HashMap();
                String targetNode = ((NormalEdge) edge).getTargetNode();
                dfs(subJsonMap,targetNode);
                jsonMap.put(tag,subJsonMap);
            }
            else if(edge instanceof ForkEdge){
                ArrayList<String> targetNodeList = ((ForkEdge) edge).getTargetNode();
                ArrayList<Map> subJsonMapList = new ArrayList<>();
                for(String targetNode: targetNodeList)
                {
                    Map subJsonMap = new HashMap();
                    dfs(subJsonMap,targetNode);
                    subJsonMapList.add(subJsonMap);
                }
                jsonMap.put(tag,subJsonMapList);
            }
        }
        Map simpleProperty = planPkgInfoDao.getData(key);
        jsonMap.putAll(simpleProperty);
    }
}
