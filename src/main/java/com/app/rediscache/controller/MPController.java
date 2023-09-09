package com.app.rediscache.controller;
import com.app.rediscache.Validator.JsonValidator;
import com.app.rediscache.Validator.JsonValidatorImpl;
import com.app.rediscache.Validator.TokenValidator;
import com.app.rediscache.service.ElasticService;
import com.app.rediscache.service.KafkaService;
import com.app.rediscache.service.RedisService;
import com.auth0.jwk.JwkException;
import org.everit.json.schema.ValidationException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;




import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class MPController {


    @Autowired
    private JsonValidator jsonValidator;
    @Autowired
    private RedisService redisService;
    @Autowired
    private TokenValidator tokenValidator;
    @Autowired
    private KafkaService kafkaService;
    @Autowired
    private ElasticService elasticService;

    boolean tokenIsValid;

    @PostMapping("/plan")
    public ResponseEntity<String> savePlan(@RequestBody String planJsonContent, @RequestHeader HttpHeaders headers) throws NoSuchAlgorithmException, IOException {
        tokenIsValid =false;
        JSONObject jsonContent;
        jsonContent = new JSONObject(planJsonContent);

        JSONObject errorBodyOutput1 = new JSONObject();
        errorBodyOutput1.put("Message", "Invalid token");
        errorBodyOutput1.put("planId",jsonContent.get("objectId"));
        JSONObject errorBodyOutput2 = new JSONObject();
        errorBodyOutput2.put("Message", "Invalid Json format");
        errorBodyOutput2.put("planId",jsonContent.get("objectId"));



        try {
            tokenIsValid = tokenValidator.validate(headers);
        } catch (ParseException | JwkException |InvalidKeySpecException | SignatureException |InvalidKeyException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBodyOutput1.toString());
        }

        if (!tokenIsValid)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBodyOutput1.toString());

        try {
            jsonValidator.validate(jsonContent);
        } catch (ValidationException | IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBodyOutput2.toString());
        }

        String key = jsonContent.get("objectType").toString() + "_" + jsonContent.get("objectId").toString();
        if(redisService.keyExist(key))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JSONObject().put("Message", "Plan already exist").toString());

        Map elasticIndices = new LinkedHashMap();
        String eTag=redisService.storeObjectToRedisAndMQ(jsonContent,key,elasticIndices);
        JSONObject elasticIndicesJson = new JSONObject(elasticIndices);
        kafkaService.publish(elasticIndicesJson.toString(),"index");
        //elasticService.postDocument(elasticIndices);
        //kafkaService.publish(jsonContent.toString(),"index");
        return ResponseEntity.ok().eTag(eTag).body(" {\"message\": \"Created data with key: " + jsonContent.get("objectId") + "\" }");
    }

    @GetMapping(path = "/{type}/{objectId}", produces = "application/json")
    public ResponseEntity<Object> getPlan(@RequestHeader HttpHeaders headers, @PathVariable String objectId,@PathVariable String type) throws JSONException, Exception {

        tokenIsValid =false;
        JSONObject errorBodyOutput1 = new JSONObject();
        errorBodyOutput1.put("Message", "Invalid token");
        errorBodyOutput1.put(type,objectId);
        try {
            tokenIsValid = tokenValidator.validate(headers);
        } catch (ParseException | JwkException |InvalidKeySpecException | SignatureException |InvalidKeyException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBodyOutput1.toString());
        }

        if (!tokenIsValid)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBodyOutput1.toString());


        String key = type + "_" + objectId;
        boolean isConditionRead = false;
        if (!type.equals("plan"))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JSONObject().put("no such Object", type).toString());
        }

        if(headers.containsKey("If-None-Match") && headers.getFirst("If-None-Match")!=null) {
            String actualEtag = redisService.getEtag(key);
            String eTag = headers.getFirst("If-None-Match");
            isConditionRead = true;
            assert eTag != null;
            if (eTag.equals(actualEtag)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
        }

        if (!redisService.keyExist(key)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JSONObject().put("Message", "ObjectId does not exist").toString());
        }

        if(isConditionRead)
        {
            String actualEtag = redisService.getEtag(key);
            return ResponseEntity.ok().eTag(actualEtag).body(redisService.getContent(key).toString());
        }

        return ResponseEntity.ok().body(redisService.getContent(key).toString());
    }
    @PatchMapping(path = "/{objectType}/{objectId}", produces = "application/json")
    public ResponseEntity<Object> patchPlan(@RequestHeader HttpHeaders headers,  @RequestBody String planJsonContent, @PathVariable String objectType,@PathVariable String objectId) throws IOException, NoSuchAlgorithmException {

        tokenIsValid =false;
        JSONObject errorBodyOutput1 = new JSONObject();
        errorBodyOutput1.put("Message", "Invalid token");
        errorBodyOutput1.put(objectType,objectId);

        try {
            tokenIsValid = tokenValidator.validate(headers);
        } catch (ParseException | JwkException |InvalidKeySpecException | SignatureException |InvalidKeyException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBodyOutput1.toString());
        }

        if (!tokenIsValid)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBodyOutput1.toString());


        String key = objectType + "_" + objectId;
        JSONObject jsonContent = new JSONObject(planJsonContent);

        if (!redisService.keyExist(key)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JSONObject().put("Message", "ObjectId does not exist").toString());
        }

        if(headers.containsKey("If-None-Match") && headers.getFirst("If-None-Match")!=null) {
            String actualEtag = redisService.getEtag(key);
            String eTag = headers.getFirst("If-None-Match");
            assert eTag != null;
            if (!eTag.equals(actualEtag)) {
                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
            }
        }


        Map elasticIndices = new LinkedHashMap();
        StringBuilder indicesToDelete = new StringBuilder();
        String eTag=redisService.patchContent(jsonContent,key,elasticIndices,indicesToDelete);
        JSONObject elasticIndicesJson = new JSONObject(elasticIndices);
        kafkaService.publish(elasticIndicesJson.toString(),"index");
        if(!indicesToDelete.toString().isBlank()){
            kafkaService.publish(indicesToDelete.toString(),"delete");
        }
        return ResponseEntity.ok().eTag(eTag).body(" {\"message\": \"patched data with key: " + objectId + "\" }");
    }
    @DeleteMapping(path = "/{type}/{objectId}", produces = "application/json")
    public ResponseEntity<Object> deletePlan(@RequestHeader HttpHeaders headers, @PathVariable String objectId, @PathVariable String type){

        JSONObject errorBodyOutput1 = new JSONObject();
        errorBodyOutput1.put("Message", "Invalid token");
        errorBodyOutput1.put(type,objectId);
        boolean tokenIsValid=false;
        try {
            tokenIsValid = tokenValidator.validate(headers);
        } catch (ParseException | JwkException | InvalidKeySpecException | SignatureException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBodyOutput1.toString());
        }
        if (!tokenIsValid)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBodyOutput1.toString());

        if (!redisService.keyExist("plan"+ "_" + objectId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JSONObject().put("Message", "ObjectId does not exist").toString());
        }

        String indicesToDelete = redisService.deleteContent("plan" + "_" + objectId);
        kafkaService.publish(indicesToDelete,"delete");

        return ResponseEntity
                .noContent()
                .build();


    }

}

