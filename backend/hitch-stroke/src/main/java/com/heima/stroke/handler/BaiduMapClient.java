package com.heima.stroke.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.heima.commons.domin.bo.RoutePlanResultBO;
import com.heima.commons.domin.bo.TextValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class BaiduMapClient {
    @Value("${baidu.map.api}")
    private String api;
    @Value("${baidu.map.ak}")
    private String ak;

    private final static Logger logger = LoggerFactory.getLogger(BaiduMapClient.class);

    //TODO:任务3.2-调百度路径计算两点间的距离，和预估抵达时长
    public RoutePlanResultBO pathPlanning(String origins, String destinations){

        BaiduMapClient snCal = new BaiduMapClient();
        Map params = new LinkedHashMap<String, String>();
        params.put("origin", origins);
        params.put("destination", destinations);
        params.put("ak", ak);


        String JsonStr = null;

        try {
            JsonStr = snCal.requestGetAK(api, params);
        }catch (Exception e){
            e.printStackTrace();
        }
        JSONObject jsonObject = JSON.parseObject(JsonStr);

        int status = jsonObject.getIntValue("status");
        if(status != 0) return null;
        JSONObject jsonObject1 = jsonObject.getJSONArray("result").getJSONObject(0);
        String distanceText = jsonObject1.getJSONObject("distance").getString("text");
        Integer distanceValue = jsonObject1.getJSONObject("distance").getInteger("value");
        String durationText = jsonObject1.getJSONObject("duration").getString("text");
        Integer durationValue = jsonObject1.getJSONObject("duration").getInteger("value");
        logger.info("获取行程数据");
        logger.info("距离 =" + distanceText);
        logger.info("时间 =" + durationText);

        RoutePlanResultBO routePlanResultBO = new RoutePlanResultBO();

        //设置距离长度
        TextValue textValue = new TextValue();
        textValue.setText(distanceText);
        textValue.setValue(distanceValue);
        routePlanResultBO.setDistance(textValue);

        //设置时间长短
        TextValue textValue1 = new TextValue();
        textValue1.setText(durationText);
        textValue1.setValue(durationValue);
        routePlanResultBO.setDuration(textValue1);

        return routePlanResultBO;
    }

    /**
     * 默认ak
     * 选择了ak，使用IP白名单校验：
     * 根据您选择的AK已为您生成调用代码
     * 检测到您当前的ak设置了IP白名单校验
     * 您的IP白名单中的IP非公网IP，请设置为公网IP，否则将请求失败
     * 请在IP地址为xxxxxxx的计算发起请求，否则将请求失败
     *
     * @return
     */
    public String requestGetAK(String strUrl, Map<String, String> param) throws Exception {
        if (strUrl == null || strUrl.length() <= 0 || param == null || param.size() <= 0) {
            return null;
        }

        StringBuffer queryString = new StringBuffer();
        queryString.append(strUrl);
        for (Map.Entry<?, ?> pair : param.entrySet()) {
            queryString.append(pair.getKey() + "=");
            //    第一种方式使用的 jdk 自带的转码方式  第二种方式使用的 spring 的转码方法 两种均可
            //    queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8").replace("+", "%20") + "&");
            queryString.append(UriUtils.encode((String) pair.getValue(), "UTF-8") + "&");
        }

        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1);
        }

        URL url = new URL(queryString.toString());
        System.out.println(queryString.toString());
        URLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.connect();

        InputStreamReader isr = new InputStreamReader(httpConnection.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        isr.close();
        System.out.println("AK: " + buffer.toString());
        //截取字符串
        return buffer.toString();

    }
}
