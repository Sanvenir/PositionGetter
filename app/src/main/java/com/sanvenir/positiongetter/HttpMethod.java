package com.sanvenir.positiongetter;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sanvenir on 1/4/2018.
 */

public class HttpMethod {

    protected static CookieManager cookieManager = new CookieManager();
    static
    {
        CookieHandler.setDefault(cookieManager);
    }

    public static boolean getPos = false;
    public static String setGetPos(Boolean getPos) throws IOException {
        HttpMethod.getPos = getPos;
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("method", "setgetpos");
        paramMap.put("getpos", getPos.toString());
        return postMethod(paramMap);
    }

    public static String bindParams(Map<String, String> paramMap){
        StringBuilder builder = new StringBuilder();
        builder.append("?");
        for(String key : paramMap.keySet()) {
            builder.append(key);
            builder.append("=");
            builder.append(paramMap.get(key));
            builder.append("&");
        }
        return builder.toString();
    }
    public static String postMethod(Map<String, String> paramMap) throws IOException {
        String baseURL = "http://104.238.181.152:8080/posget/info";
        URL url = new URL(baseURL + bindParams(paramMap));
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);

        int responseCode = conn.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );
        String inputLine;
        StringBuilder response = new StringBuilder();
        while((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        conn.disconnect();
        Log.d("Post Method " + bindParams(paramMap), response.toString());
        return response.toString();
    }
}
