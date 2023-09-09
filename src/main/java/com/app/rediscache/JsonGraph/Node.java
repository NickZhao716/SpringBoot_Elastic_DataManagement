package com.app.rediscache.JsonGraph;

import java.io.Serializable;
import java.util.HashMap;

public class Node implements Serializable {
    String name;
    HashMap<String,Edge> fanOut;
    HashMap<String,Edge> fanIn;
    public Node(String name){
        this.name =name;
        fanOut = new HashMap<>();
        fanIn = new HashMap<>();
    }
    public Node(String name,HashMap<String,Edge> fanOut,HashMap<String,Edge> fanIn){
        this.name =name;
        this.fanOut = fanOut;
        this.fanIn = fanIn;
    }
    public String getName(){
        return name;
    }
    public Edge getFanOutEdge(String key){
        return fanOut.get(key);
    }
    public HashMap<String,Edge> getFanOut(){return fanOut;}
    public HashMap<String,Edge> getFanIn(){return fanIn;}
    public Edge getFanInEdge(String key){
        return fanIn.get(key);
    }
    public void addFanOutEdge(String name,Edge edge){
        fanOut.put(name,edge);
    }
    public void addFanInEdge(String name,Edge edge){
        fanIn.put(name,edge);
    }
    public void renameNode(String newName){
        name = newName;
    }
}
