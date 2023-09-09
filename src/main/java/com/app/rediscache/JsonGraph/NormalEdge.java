package com.app.rediscache.JsonGraph;


import java.io.Serializable;

public class NormalEdge extends Edge implements EdgeFunction, Serializable {
   String targetNode;
   public NormalEdge(String name, String targetNode){
      this.name = name;
      this.targetNode = targetNode;
   }

   @Override
   public String getTargetNode() {
      return targetNode;
   }
   public void setTargetNode(String newName){
      targetNode = newName;
   }
}