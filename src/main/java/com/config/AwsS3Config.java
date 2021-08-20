package com.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
@Setter
public class AwsS3Config {

	@Value("${amazon.s3.bucket-name}")
	private String bucketName;

	@Value("${amazon.s3.access-key}")
	private String accessKey;

	@Value("${amazon.s3.secret-key}")
	private String secretKey;

	@Value("${amazon.s3.region}")
	private String region;
	
	@Value("${amazon.s3.folder-name}")
	private String folderName;
	

	@Bean
	public AmazonS3 client() {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		return AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(region).build();
	}
}
