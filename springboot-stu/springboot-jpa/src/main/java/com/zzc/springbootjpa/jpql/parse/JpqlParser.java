package com.zzc.springbootjpa.jpql.parse;

import com.zzc.springbootjpa.jpql.utils.TemplateHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * @author zzc
 * @since 2020-09-03
 *
 */
@Slf4j
@Component
public class JpqlParser {
    @Autowired
    private JpqlScanner jpqlScanner;
    @Autowired
    private VelocityEngine engine;
    private SpelExpressionParser expressionParser=new SpelExpressionParser();

    public ParsedJpql parse(String id, Map<String, Object> parameter, Pageable pageable) {
        Jpql orginalJpql = jpqlScanner.load(id);
        VelocityContext velocityContext = new VelocityContext();
        TemplateHelper templateHelper=new TemplateHelper();
        if (pageable != null){
            velocityContext.put("pageable", pageable);
        }

        for (Map.Entry<String, Object> entry : parameter.entrySet()) {
            velocityContext.put(entry.getKey(), entry.getValue());
        }
        velocityContext.put("refs",jpqlScanner.getCachedJpql());
        velocityContext.put("helper",templateHelper);
        StringWriter stringWriter = new StringWriter();
        engine.evaluate(velocityContext, stringWriter, "jpql", orginalJpql.getJpql());

        ParsedJpql jpql = new ParsedJpql();
        BeanUtils.copyProperties(orginalJpql, jpql);
        jpql.setParsed(stringWriter.getBuffer().toString());
        Pattern pattern = Pattern.compile(":(" + "[\\p{Lu}\\P{InBASIC_LATIN}\\p{Alnum}._%\\[\\]$]+)", CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(jpql.getParsed());
        int i = 0;
        String parsedJpql=jpql.getParsed();
        HashMap<String, Object> map = new HashMap();
        List<String> params=new ArrayList();
        while (matcher.find()) {
            String name = matcher.group(0);
            //修复字符串常量里面的冒号解析问题，去除引号里冒号被解析的问题
            int start=matcher.start(0);
            boolean inStr=false;
            for(int j=0;j<start;j++){
                char c=parsedJpql.charAt(j);
                if(c=='\''){
                    inStr=!inStr;
                }
            }
            if(!params.contains(name)&&!inStr){
                params.add(name);
            }
        }
        Map<String,String> paramMapping=new HashMap<>();
        for(String param:params){
            String test=StringUtils.strip(param,":");
            Object o = getParameter(parameter,StringUtils.strip(test,"%"));
            String key = "argument" + (i++);
            StringBuilder value=new StringBuilder();
            prepareForLike(map, test, o, key, value);
            //use replace all to replace the string
            paramMapping.put(param,key);
//            jpql.setParsed(StringUtils.replaceAll(jpql.getParsed(), toReplaceString(param),":".concat(key).concat(" ")));
        }
        jpql.setParsed(replaceParameter(jpql.getParsed(),paramMapping,pattern));

        jpql.setParameterMap(map);

        log.info("Parsed jpql: {}",jpql.getParsed());
        log.info("Jpql parameters: {}",jpql.getParameterMap());
        return jpql;
    }

    private String replaceParameter(String parsed,Map<String,String> mapping,Pattern pattern) {

        Matcher matcher=pattern.matcher(parsed);
        StringBuilder stringBuilder=new StringBuilder();
        int lastEnd=0;
        while (matcher.find()) {
            String name = matcher.group(0);
            if(lastEnd>=0){
                stringBuilder.append(parsed, lastEnd, matcher.start(0));
            }
            if(mapping.get(name)!=null){
                stringBuilder.append(":".concat(mapping.get(name)));
            }else {
                stringBuilder.append(name);
            }
            lastEnd = matcher.end(0);
        }
        if(lastEnd>0){
            stringBuilder.append(parsed.substring(lastEnd));
        }
        return stringBuilder.length()>0?stringBuilder.toString():parsed;

    }


    private void prepareForLike(HashMap<String, Object> map, String test, Object o, String key, StringBuilder value) {
        boolean start=test.startsWith("%");
        boolean end=test.endsWith("%");
        if(start){
            value.append("%");
        }
        if(o!=null){
            value.append(o.toString());
        }
        if (end){
            value.append("%");
        }
        map.put(key,start||end?value.toString():o);
    }

    /**
     * 通过Spring EL 解析表达式
     * @param parameter
     * @param name
     * @return
     */
    private Object getParameter(Map<String, Object> parameter, String name) {
        EvaluationContext context = new StandardEvaluationContext();
        for (String key:parameter.keySet()){
            context.setVariable(key,parameter.get(key));
        }
        Expression expression=expressionParser.parseExpression("#"+name);
        return expression.getValue(context);
    }

    public JpqlScanner getJpqlScanner() {
        return jpqlScanner;
    }

}
