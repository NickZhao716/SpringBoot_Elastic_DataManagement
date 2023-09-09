package com.app.rediscache.JsonGraph;

import java.io.Serializable;
import java.util.ArrayList;

public class ForkEdge extends Edge implements EdgeFunction, Serializable {
    ArrayList<String> targetNode;
    public ForkEdge(String name, ArrayList<String> targetNode)
    {
        this.name = name;
        this.targetNode = targetNode;
    }

    @Override
    public ArrayList<String> getTargetNode() {
        return targetNode;
    }

    public void resetTargetNode(ArrayList<String> newList){
        targetNode = newList;
    }
}
