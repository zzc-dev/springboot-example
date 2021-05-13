# 获取ip

```
public static String getRemoteIp(HttpServletRequest request) {
        String unKnown = "unknown";
        int ipLength = 15;
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null) {
            log.info("getIpAddress(HttpServletRequest) - X-Forwarded-For - String ip=" + ip);
        }

        if (ip != null && ip.length() != 0 && !unKnown.equalsIgnoreCase(ip)) {
            if (ip != null && ip.length() > ipLength) {
                String[] ips = ip.split(",");
                String[] var5 = ips;
                int var6 = ips.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    String strIp = var5[var7];
                    if (!unKnown.equalsIgnoreCase(strIp)) {
                        ip = strIp;
                        break;
                    }
                }
            }
        } else {
            if (ip == null || ip.length() == 0 || unKnown.equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
                if (ip != null) {
                    log.info("getIpAddress(HttpServletRequest) - Proxy-Client-IP - String ip=" + ip);
                }
            }

            if (ip == null || ip.length() == 0 || unKnown.equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
                if (ip != null) {
                    log.info("getIpAddress(HttpServletRequest) - WL-Proxy-Client-IP - String ip=" + ip);
                }
            }

            if (ip == null || ip.length() == 0 || unKnown.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
                if (ip != null) {
                    log.info("getIpAddress(HttpServletRequest) - HTTP_CLIENT_IP - String ip=" + ip);
                }
            }

            if (ip == null || ip.length() == 0 || unKnown.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
                if (ip != null) {
                    log.info("getIpAddress(HttpServletRequest) - HTTP_X_FORWARDED_FOR - String ip=" + ip);
                }
            }

            if (ip == null || ip.length() == 0 || unKnown.equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
                if (ip != null) {
                    log.info("getIpAddress(HttpServletRequest) - getRemoteAddr - String ip=" + ip);
                }
            }
        }

        return ip;
    }
```

# 导入图片到excel

# httpclient

https://www.cnblogs.com/supiaopiao/p/12513148.html

## 1.commons-httpclient

```xml
<dependency>
    <groupId>commons-httpclient</groupId>
    <artifactId>commons-httpclient</artifactId>
    <version>3.1</version>
</dependency>
```

post application/x-www-form-urlencoded

```java
String postUrl = shantouProperties.getAppToken().getUrl();
PostMethod postMethod = new PostMethod(postUrl);
postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8") ;
NameValuePair[] params = {
  new NameValuePair("client_id", shantouProperties.getAppToken().getClientId()),
  new NameValuePair("client_secret", shantouProperties.getAppToken().getClientSecret()),
  new NameValuePair("grant_type", shantouProperties.getAppToken().getGrantType()),
};
postMethod.setRequestBody(params);

HttpClient httpClient = new HttpClient();

try {
    int status = httpClient.executeMethod(postMethod);
    log.info("status={}",status);
    JSONObject reponse = JSONObject.parseObject(postMethod.getResponseBodyAsString());
    return reponse.getString("access_token");
} catch (Exception e) {
    log.error("获取appToken失败！", e);
}
return null;
```

## 2.org.apache.httpcomponents

```xml
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.4.1</version>
</dependency>
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpcore</artifactId>
    <version>4.4.1</version>
</dependency>
```

post application/x-www-form-urlencoded

```
private String requestApptoken(){
    String postUrl = shantouProperties.getAppToken().getUrl();

    HttpPost httpPost = new HttpPost(postUrl);
    BasicNameValuePair[] params = {
      new BasicNameValuePair("client_id", shantouProperties.getAppToken().getClientId()),
      new BasicNameValuePair("client_secret", shantouProperties.getAppToken().getClientSecret()),
      new BasicNameValuePair("grant_type", shantouProperties.getAppToken().getGrantType()),
    };
    try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
        httpPost.setEntity(new UrlEncodedFormEntity(Arrays.asList(params), "UTF-8"));
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        JSONObject jsonObject = JSONObject.parseObject(EntityUtils.toString(entity, "UTF-8"));
        log.info("获取appToken={}",jsonObject.toJSONString());
        return jsonObject.getString("access_token");
    } catch (Exception e) {
        log.error("获取appToken失败！", e);
    }
    return null;
}
```

post form-data