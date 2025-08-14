import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.junit.Test;


import java.io.File;


public class test2{
    @Value("${spring.cos.secretId}")
    private String secretId;

    @Value("${cos.secretKey}")
    private String secretKey;

    @Value("${cos.region}")
    private String region;

    @Value("${cos.bucketName}")
    private String bucketName;

    @Test
    public void test() {
        // 初始化COS客户端
        System.out.println(secretId);
        System.out.println(secretKey);
        System.out.println(region);
        System.out.println(bucketName);

        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        COSClient cosClient = new COSClient(cred, clientConfig);
        // 上传文件
        String key = "exampleobject"; // 文件在COS中的唯一标识
        String filePath = "path/to/local/file"; // 本地文件路径
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, new File(filePath));
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        System.out.println("文件上传成功, ETag:" + putObjectResult.getETag());
        // 关闭COS客户端
        cosClient.shutdown();
    }
}