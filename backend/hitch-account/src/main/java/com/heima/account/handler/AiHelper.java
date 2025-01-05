package com.heima.account.handler;

import com.baidu.aip.util.Base64Util;
import com.heima.commons.ai.BaiduAIHelper;
import com.heima.modules.po.VehiclePO;
import okhttp3.*;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import static java.nio.file.Files.createTempDirectory;


@Component
public class AiHelper {
    @Value("${baidu.apikey}")
    private static String API_KEY = "D9rk4PLEVcpWml4ms987al60";
    @Value("${baidu.secretkey}")
    private static String SECRET_KEY = "0KEH1nI8Z2U0rFI2TGRCxR0FU81QQ18D";

    private final static Logger logger = LoggerFactory.getLogger(AiHelper.class);

    final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();

    @Resource
    BaiduAIHelper baiduAIHelper = new BaiduAIHelper(API_KEY, SECRET_KEY, redisTemplate);

    public static void main(String []args) throws IOException {
        String code = new AiHelper().getLicense(null);
        System.out.println(code);
    }

    /**
    图像识别，获取车牌信息
    文档（行驶证识别）：https://cloud.baidu.com/doc/OCR/s/yk3h7y3ks
    文档（车牌识别）：https://cloud.baidu.com/doc/OCR/s/ck3h7y191
     将url下载到某个临时文件夹
     调百度AI接口，返回对应信息
     对比：行驶证车牌 和 车辆车牌是否一致
     如果一致，设置车牌信息，认证通过，身份变更为车主
    简化版业务流程（至少完成）：识别车辆车牌号即可

    */
    public String getLicense(VehiclePO vehiclePO) throws IOException {
        //TODO:任务2.1-车辆信息验证代码编写-2day
        // 获取车牌号照片
        String url = vehiclePO.getCarFrontPhoto();
//        创建临时文件夹
        byte[] imgData = fetchImageFromServer(url);
        String imgStr = Base64Util.encode(imgData);
        String imgParam = URLEncoder.encode(imgStr, "UTF-8");

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "image=" + imgParam +"&multi_detect=false&multi_scale=false");
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/ocr/v1/license_plate?access_token=" + baiduAIHelper.getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();

        Response response = HTTP_CLIENT.newCall(request).execute();
        String responseBody = response.body().string();
        System.out.println(responseBody);

        JSONObject jsonObject = new JSONObject(responseBody);
        JSONObject wordsObject = jsonObject.getJSONObject("words_result");
        String number = wordsObject.getString("number");
        return number;
    }

    private byte[] fetchImageFromServer(String url) throws IOException {
        URL imageUrl = new URL(url);
        try (InputStream in = imageUrl.openStream();
             BufferedInputStream bis = new BufferedInputStream(in)) {
            // 用于存储读取到的字节数据
            byte[] buffer = new byte[1024];
            int bytesRead;
            // 用于拼接完整的字节数组数据
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            while ((bytesRead = bis.read(buffer))!= -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        }
    }
}
