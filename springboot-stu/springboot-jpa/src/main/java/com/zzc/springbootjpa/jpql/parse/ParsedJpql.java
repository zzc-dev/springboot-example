package com.zzc.springbootjpa.jpql.parse;

import java.util.Map;

/**
 * @author zzc
 * @since 2020-09-03
 */
public class ParsedJpql extends Jpql {
    private String parsed;
    private Map<String, Object> parameterMap;

    public Map<String, Object> getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(Map<String, Object> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public String getParsed() {
        return parsed;
    }

    public void setParsed(String parsed) {
        this.parsed = parsed;
    }
}
