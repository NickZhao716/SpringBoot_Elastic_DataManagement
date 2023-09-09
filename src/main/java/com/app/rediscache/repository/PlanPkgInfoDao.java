package com.app.rediscache.repository;

import com.app.rediscache.JsonGraph.Graph;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface PlanPkgInfoDao {
     static final String KEY = "plan";
     static final String ETAG_SET = "ETAG_HASH_SET";
     static final String VALID_REQUESTER_SET = "VALID_REQUESTER_SET";


    boolean keyExisted(String key);
    String returnEtag(String key);
    boolean saveEtag(String eTag,String key);
    boolean deleteEtag(String key);
    boolean eTagExisted(String eTag);
    boolean isValidRequester(String email,String userName);
    void deleteContent(String key);
    void printHashInfo();
    void saveObjectInfo(String key, Map simplePropertyInfo);
    void updateObjectInfo(String key,Map objectInfo);
    void renameObjectKey(String oldKey,String newKey);
    Map<String,String> getData(String key);
    void saveJsonGraph(String key,Graph jsonGraph);
    Graph getJsonGraph(String key);

    Map<String,String> getObjectsType(String key);
    void saveIndexInfo(String key,Object value);
}