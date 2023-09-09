package com.app.rediscache.service;


import com.app.rediscache.repository.ElasticDao;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class ElasticServiceImpl implements ElasticService{
    @Autowired
    ElasticDao elasticDao;

    public void postIndexToElastic() throws IOException {
        elasticDao.createElasticIndex();
    }




    public void postDocument(Map<String, Object> MapOfDocuments) throws IOException {
        elasticDao.postDocument(MapOfDocuments);
    }




    /*
    public void postDocument(JSONObject jsonObject) throws IOException {
        elasticDao.postDocument(jsonObject);
    }


     */






}


