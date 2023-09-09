package com.app.rediscache.Validator;

import com.app.rediscache.repository.PlanPkgInfoDao;
import com.auth0.jwk.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.*;
import org.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TokenValidatorImpl implements TokenValidator {
    @Autowired
    PlanPkgInfoDao planPkgInfoDao;

    @Override
    public boolean validate(@RequestHeader HttpHeaders headers) throws IOException, ParseException, JwkException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        String token = headers.getFirst("Authorization");
        if (token == null||token.isBlank()||token.isEmpty())
            return false;

        if (!token.contains("Bearer"))
            return false;

        token = token.substring(7);
        DecodedJWT jwt = JWT.decode(token);

        if(!validatePayload(jwt))
            return false;

        if (!validateSignature(jwt))
            return false;

        return true;

    }

    @Override
    public boolean validatePayload(DecodedJWT jwt) throws ParseException {
        Map payloadInfo = jwt.getClaims();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        Date currentDate = formatter.parse(formatter.format(calendar.getTime()));
        Date expDate = jwt.getExpiresAt();
        if(expDate.before(currentDate))
            return false;
        if(!jwt.getIssuer().equals("https://accounts.google.com"))
            return false;
        String email = payloadInfo.get("email").toString();
        email = email.substring(1,email.length()-1);
        String givenName = payloadInfo.get("given_name").toString();
        givenName = givenName.substring(1,givenName.length()-1);
        String familyName = payloadInfo.get("family_name").toString();
        familyName = familyName.substring(1,familyName.length()-1);
        String name = givenName+"_"+familyName;
        if (!planPkgInfoDao.isValidRequester(email,name))
            return false;

        return true;
    }

    @Override
    public boolean validateSignature(DecodedJWT jwt) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, IOException {
        JSONObject jwkJson = getJWKFromGoogle(jwt.getKeyId());
        PublicKey publicKey = convertJWKToPublicKey(jwkJson);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        String message = jwt.getHeader()+"."+jwt.getPayload();
        signature.update(message.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = Base64.getUrlDecoder().decode(jwt.getSignature());
        return signature.verify(signatureBytes);
    }

    @Override
    public JSONObject readJsonFromUrl(String url) throws IOException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            String jsonText = sb.toString();
            return new JSONObject(jsonText);
        }
    }

    @Override
    public JSONObject getJWKFromGoogle(String JWTKeyId) throws IOException {

        JSONObject jwksJson = readJsonFromUrl("https://www.googleapis.com/oauth2/v3/certs");
        JSONArray jwksJsonArray = jwksJson.getJSONArray("keys");
        JSONObject jwkJson = null;
        for(int i =0;i<jwksJsonArray.length();i++)
        {
            JSONObject temp = jwksJsonArray.getJSONObject(i);
            String test = temp.getString("kid");
            if (jwksJsonArray.getJSONObject(i).get("kid").equals(JWTKeyId)){
                jwkJson = jwksJsonArray.getJSONObject(i);
                break;
            }
        }
        return jwkJson;
    }

    @Override
    public PublicKey convertJWKToPublicKey(JSONObject jwkJson) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(jwkJson.getString("kty"));
        BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(jwkJson.getString("n")));
        BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(jwkJson.getString("e")));
        return keyFactory.generatePublic(new RSAPublicKeySpec(modulus, exponent));
    }
}
