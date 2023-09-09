package com.app.rediscache.Validator;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;

@Service
public class JsonValidatorImpl implements JsonValidator {
    @Override
    public void validate(JSONObject object) throws IOException {
        try(InputStream inputStream = getClass().getResourceAsStream(PlANINFO_JSONSCHEMA)){
            assert inputStream != null;
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            Schema schema = SchemaLoader.load(rawSchema);
            schema.validate(object);
        }
    }
}