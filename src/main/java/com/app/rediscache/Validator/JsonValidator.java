package com.app.rediscache.Validator;

import org.json.JSONObject;

import java.io.IOException;

public interface JsonValidator {
    public static String PlANINFO_JSONSCHEMA = "/JsonSchema.json";
    public void validate(JSONObject object) throws IOException;
}
