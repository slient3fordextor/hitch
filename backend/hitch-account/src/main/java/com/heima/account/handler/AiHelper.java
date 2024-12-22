package com.heima.account.handler;

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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;



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
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "tempDownloads");
        Files.createDirectories(tempDir);
        //获取车牌号照片
        try {
            URL drivingCarUrl = new URL(url);
//        写入文件夹
            InputStream inputStream = drivingCarUrl.openStream();
            Path tempFile = tempDir.resolve(drivingCarUrl.getPath().substring(drivingCarUrl.getPath().lastIndexOf('/') + 1));
            Files.copy(inputStream, tempFile);
            inputStream.close();
            //进行base64编码
            byte[] fileContent = Files.readAllBytes(tempFile);
            String base64Encoded = Base64.getEncoder().encodeToString(fileContent);
            String encoded = URLEncoder.encode(base64Encoded, "UTF-8");


            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "image=" + encoded +"&multi_detect=false&multi_scale=false");
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String getLicensePlateByDrivingLicense(VehiclePO vehiclePO) {
        try {
            String filePath = vehiclePO.getCarBackPhoto();
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "tempDownloads");
            //获取驾驶证照片
            URL drivingLicneseUrl = new URL(filePath);
//            存放文件到临时文件夹
            InputStream inputStream = drivingLicneseUrl.openStream();
            Path tempFile = tempDir.resolve(drivingLicneseUrl.getPath().substring(drivingLicneseUrl.getPath().lastIndexOf('/') + 1));
            Files.copy(inputStream, tempFile);
            inputStream.close();
//        将文件编码为base64
            byte[] fileContent = Files.readAllBytes(tempFile);
            String base64Encoded = Base64.getEncoder().encodeToString(fileContent);
            System.out.println("Base64编码结果: " + base64Encoded);
            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = baiduAIHelper.getAccessToken();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getLicensePlateByCar(VehiclePO vehiclePO) {
        return "00000";
    }
}
