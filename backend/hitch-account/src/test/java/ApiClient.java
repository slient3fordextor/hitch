
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import springfox.documentation.service.ApiKey;

import java.io.*;

class ApiClient {

    @Value("${baidu.apikey}")
    private static String API_KEY = "D9rk4PLEVcpWml4ms987al60";
    @Value("${baidu.secretkey}")
    private static String SECRET_KEY = "0KEH1nI8Z2U0rFI2TGRCxR0FU81QQ18D";

    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    public static void main(String []args) throws IOException{
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token?client_id="+ API_KEY +"&client_secret=" + SECRET_KEY +"&grant_type=client_credentials")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        System.out.println(response.body().string());

    }
}
