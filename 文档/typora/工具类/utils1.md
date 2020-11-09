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

