package com.app.rediscache.repository;

import com.app.rediscache.JsonGraph.Edge;
import com.app.rediscache.JsonGraph.Graph;
import com.app.rediscache.JsonGraph.Node;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.SerializationUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.RedisOutputStream;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.*;

@Repository
public class PlanPkgInfoDaoImpl implements PlanPkgInfoDao {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean keyExisted(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public String returnEtag(String key) {
        Map eTagSet;
        try {
            eTagSet=redisTemplate.opsForHash().entries(ETAG_SET);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return (String) eTagSet.get(key);
    }

    @Override
    public boolean saveEtag(String eTag, String key) {
        if(!keyExisted(ETAG_SET))
        {
            Map eTagSet = new HashMap<>();
            eTagSet.put(key,eTag);
            try {
                redisTemplate.opsForHash().putAll(ETAG_SET,eTagSet);
            }catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }
        else
        {
            try {
                redisTemplate.opsForHash().put(ETAG_SET,key,eTag);
                printHashInfo();
            }catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean deleteEtag(String key) {
        try {
            redisTemplate.opsForHash().delete(ETAG_SET,key);
            printHashInfo();
            return true;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eTagExisted(String eTag) {
        Map eTagSet = null;
        try {
            eTagSet=redisTemplate.opsForHash().entries(ETAG_SET);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert eTagSet != null;
        return eTagSet.containsKey(eTag);
    }

    @Override
    public boolean isValidRequester(String email, String userName) {

        redisTemplate.opsForHash().put(VALID_REQUESTER_SET,"nickzhaohk@gmail.com","Zhao_Nick");
        if(!redisTemplate.opsForHash().entries(VALID_REQUESTER_SET).containsKey(email))
            return false;
        return redisTemplate.opsForHash().entries(VALID_REQUESTER_SET).get(email).toString().equals(userName);
    }


    @Override
    public void deleteContent(String key) {
        try {
            redisTemplate.delete(key);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void printHashInfo() {
        Map map = redisTemplate.opsForHash().entries(ETAG_SET);
        for(Object key:map.keySet())
        {
            System.out.println("Key : "+key+" Value : "+map.get(key));
        }
        System.out.println("end----------------------------------------------");
    }

    @Override
    public void saveObjectInfo(String key, Map simplePropertyInfo) {
        try{
            redisTemplate.opsForHash().putAll(key,simplePropertyInfo);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void updateObjectInfo(String key, Map objectInfo) {
        redisTemplate.opsForHash().putAll(key,objectInfo);
    }

    @Override
    public void renameObjectKey(String oldKey, String newKey) {
        redisTemplate.rename(oldKey,newKey);
    }



    @Override
    public Map<String, String> getData(String key) {
        return  redisTemplate.opsForHash().entries(key);
    }

    @Override
    public void saveJsonGraph(String key,Graph jsonGraph) {
        if(redisTemplate.hasKey(key+"_"+"GraphNodes"))
        {
            redisTemplate.delete(key+"_"+"GraphNodes");
        }
        //redisTemplate.opsForHash().putAll(key,jsonGraph.getNodes());
        redisTemplate.opsForHash().putAll(key+"_"+"GraphNodes",jsonGraph.getNodes());
        redisTemplate.opsForHash().putAll(key+"_"+"GraphEdges",jsonGraph.getFanOutEdges());
        Graph test = getJsonGraph(key);
        //redisTemplate.opsForValue().set(key,jsonGraph);
    }

    @Override
    public Graph getJsonGraph(String key) {
        HashMap<String, Node> graphNodes = (HashMap<String, Node>) redisTemplate.opsForHash().entries(key+"_"+"GraphNodes");
        HashMap<String, Edge> graphFanOutEdges = (HashMap<String, Edge>) redisTemplate.opsForHash().entries(key+"_"+"GraphEdges");
        return new Graph(graphNodes,graphFanOutEdges);
    }


    @Override
    public Map<String, String> getObjectsType(String key) {
        return  redisTemplate.opsForHash().entries(key);
    }

    @Override
    public void saveIndexInfo(String key, Object value) {
        if(value instanceof String)
        {
            String valueToAdd = value.toString();
            redisTemplate.opsForValue().append(key,valueToAdd);
        }
        else
        {
            ArrayList<String> valueToAdd = (ArrayList<String>) value;
            for(String s:valueToAdd)
            {
                redisTemplate.opsForList().leftPush(key,s);
            }
        }
    }


}
