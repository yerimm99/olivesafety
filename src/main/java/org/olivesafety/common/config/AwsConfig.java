package org.olivesafety.common.config;

import com.amazonaws.auth.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AwsConfig {

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;


    private AWSCredentialsProvider awsCredentialsProvider() {
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
    }
    @Bean
    public AmazonSQS amazonSQS() {


        return AmazonSQSClientBuilder.standard()
                .withRegion(region)
                .withCredentials(awsCredentialsProvider())
                .build();
    }

    @Bean
    public AmazonSNS amazonSNS() {

        return AmazonSNSClientBuilder.standard()
                .withRegion(region)
                .withCredentials(awsCredentialsProvider())
                .build();
    }

}
