package com.ecode.modelevalplat.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.InputStream;

@Configuration
public class CosConfig {
    @Value("${spring.cos.secret-id}")
    private String secretId;

    @Value("${spring.cos.secret-key}")
    private String secretKey;

    @Value("${spring.cos.region}")
    private String region;

    @Value("${spring.cos.bucket-name}")
    private String bucketName;

    @Bean
    public COSClient cosClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        return new COSClient(cred, clientConfig);
    }

    @Bean
    public PutObjectRequestBuilder putObjectRequestBuilder() {
        return new PutObjectRequestBuilder(bucketName);
    }

    // 构建器模式封装 PutObjectRequest 创建
    public static class PutObjectRequestBuilder {
        private final String bucketName;

        public PutObjectRequestBuilder(String bucketName) {
            this.bucketName = bucketName;
        }

        public PutObjectRequest build(String key, InputStream inputStream, ObjectMetadata metadata) {
            return new PutObjectRequest(bucketName, key, inputStream, metadata);
        }

        public PutObjectRequest build(String key, File file) {
            return new PutObjectRequest(bucketName, key, file);
        }
    }
}