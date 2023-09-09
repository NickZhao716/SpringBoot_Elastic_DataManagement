package com.app.rediscache.Validator;

import com.auth0.jwk.JwkException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;


public interface TokenValidator {

    boolean validate(@RequestHeader HttpHeaders HttpHeaders) throws IOException, ParseException, JwkException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException;
    boolean validatePayload(DecodedJWT jwt) throws ParseException;
    boolean validateSignature(DecodedJWT jwt) throws JwkException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, IOException;
    JSONObject readJsonFromUrl(String url) throws IOException;
    JSONObject getJWKFromGoogle(String JWTKeyId) throws IOException;
    PublicKey convertJWKToPublicKey(JSONObject jwkJson) throws NoSuchAlgorithmException, InvalidKeySpecException;
}
