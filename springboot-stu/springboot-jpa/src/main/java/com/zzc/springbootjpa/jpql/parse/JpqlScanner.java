package com.zzc.springbootjpa.jpql.parse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zzc
 * @since 2020-09-03
 * 扫描jpql/ 文件下的xml中的sql，并缓存到cache中
 */
@Slf4j
@Component
public class JpqlScanner {
    private Map<String, Jpql> cache = new HashMap<>();
    @PostConstruct
    public void init() throws IOException {
        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:jpql/**/*.xml");

        if (resources != null) {
            for (Resource resource : resources) {
                SAXReader saxReader = new SAXReader();

                try {
                    Document document = saxReader.read(resource.getInputStream());
                    String module = document.getRootElement().attributeValue("module");
//                    List<Node> notes = document.selectNodes("/jpa/jpql");
                    List<Element> notes = document.getRootElement().elements("jpql");
                    for (Element node : notes) {
                        String id = node.attribute("id").getStringValue();
                        Jpql jpql = new Jpql();
                        jpql.setId(id);
                        jpql.setJpql(node.getTextTrim());
                        jpql.setNative("true".equals(node.attributeValue("native")));
                        jpql.setResultClass(node.attributeValue("resultClass"));
                        log.debug("{}: native {}, resultClass {}", id, jpql.isNative(), jpql.getResultClass());
                        StringBuilder uid=new StringBuilder(StringUtils.isNotEmpty(module)?module.concat("."):"").append(id);
                        if (!cache.containsKey(uid.toString())) {
                            cache.put(uid.toString(),jpql);
                        } else {
                            log.error("duplicate id {} found in {}", id, resource.getURI());
                        }
                    }
                    log.info("Jpql file loaded: {}", resource.getURI());
                } catch (Exception e) {
                    log.error("error to load Jpql file {}", resource.getURI());
                }

            }
        }
    }

    public Jpql load(String id) {
        return cache.get(id);
    }

    public Map getCachedJpql(){
        Map<String,String> map=new HashMap<>();
        for(String key:cache.keySet()){
            map.put(key,cache.get(key).getJpql());
        }
        return Collections.unmodifiableMap(map);
    }
}
