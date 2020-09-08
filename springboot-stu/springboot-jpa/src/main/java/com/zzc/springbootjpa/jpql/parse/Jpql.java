package com.zzc.springbootjpa.jpql.parse;

/**
 * @author zzc
 * @since 2020-09-03
 */
public class Jpql {
    private String id;
    private String jpql;
    private String module;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    private String resultClass;
    private boolean _native;

    public void setId(String id) {
        this.id = id;
    }

    public void setJpql(String jpql) {
        this.jpql = jpql;
    }

    public void setResultClass(String resultClass) {
        this.resultClass = resultClass;
    }

    public void setNative(boolean _native) {
        this._native = _native;
    }

    public String getId() {
        return id;
    }

    public String getJpql() {
        return jpql;
    }

    public String getResultClass() {
        return resultClass;
    }

    public boolean isNative() {
        return _native;
    }

}
