package com.zzc.temp.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * @author zzc
 * @since 2020-09-25
 */
@RestController
public class CjgkController {

    @PostMapping("/GatewayMsg/endpoint/invoke")
    public JSONObject cjgk(@RequestBody JSONObject jsonObject, HttpServletRequest request, HttpServletResponse response){

        response.setContentType("text/html;charset=utf-8");

        String[] headers = new String[] {
                "jdcwkkd","jdccsysdm","jdczbzl","jdczt","jdcjsshpzkrs",
                "bxzzrq","jdcllpzbh","ywppmc","jdcnszmdm","clsbdh",
                "ccrq","jdchxnbgd","jdcwkcd","jdcnszm","jdchhpfgmbh",
                "zzgdq","jdcllpz","jdcjkpz","jdcnszmbh","zzc",
                "jdczxxsdm","jdcfdjddjgl","fprq","jdchpzldm","jdcdjzsbh",
                "gxsj","clxh","jdchxnbkd","jdcdyzybjdm","jyyxqz",
                "djzsffrq","jdcfdjh","jdchphm","ccdjrq","jdczzl",
                "hbdbqk","rlzl","jdczjdjrq","jdcwkgd","jdchxnbcd",
                "jdczqyzzl","jdcfdjpl","fzjg","zwppmc","jdcqzbfqz",
                "glbm","jdcjssqpzkrs","jdchdzzl","jdcjkpzhm","jdccllxdm"
        };

        String[] values = new String[]{
               "100", "1", "1000", "正常", "4",
                "202001011010","110","brandname","110","110",
                "202001011010","100","10","纳税证明","110",
                "中国","凭证名称","110","110","比亚迪制造厂",
                "1","1000W","202001011010","01","110",
                "202001011010","车辆型号","10","1","202001011010",
                "202001011010","110","苏XXX","202001011010","1000kg",
                "环保达标情况","1","202001011010","10","10",
                "500","100","车管所","比亚迪","202001011010",
                "管理部门","1","100","110","01"
        };


        Enumeration<String> headerNames = request.getHeaderNames();
        System.out.println(jsonObject.toJSONString());
        JSONObject result = new JSONObject();
        result.put("app_header", JSON.parseObject("{\"status\": \"000\",\"statusMsg\": \"服务返回码说明\"}"));

        JSONObject appBody = new JSONObject();
        appBody.put("totalCount", 1);
        appBody.put("count", 1);

        JSONArray appBodyResult = new JSONArray();
        appBodyResult.add(headers);
        appBodyResult.add(values);

        appBody.put("result",appBodyResult);
        result.put("app_body", appBody);
        System.out.println(result.toJSONString());



        return result;
    }
}
