package com.svc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import com.config.AwsS3Config;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AwsS3Service {

	@Autowired
	AmazonS3 s3Client;

	@Autowired
	AwsS3Config config;

	public List<String> getAllFiles() {
		return s3Client.listObjectsV2(config.getBucketName()).getObjectSummaries().stream().map(S3ObjectSummary::getKey)
				.collect(Collectors.toList());
	}

	public String storeFileInBucket(MultipartFile multiPartFile) {
		createBucket();
		File file = convertMultiPartFileToFile(multiPartFile);
		if (file == null) {
			return "";
		}
		String fileName = getRandomFileName(file.getName());
		String key = getKey(fileName);
		PutObjectRequest request = new PutObjectRequest(config.getBucketName(), key, file);
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(getContentTypeString(multiPartFile.getOriginalFilename()));
		request.setMetadata(metadata);
		s3Client.putObject(request);
		return fileName;
	}

	public byte[] downloadFileFromBucket(String fileName) {
		String key = getKey(fileName);
		S3Object s3Object = s3Client.getObject(new GetObjectRequest(config.getBucketName(), key));
		S3ObjectInputStream inputStream = s3Object.getObjectContent();
		try {
			return IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
			log.error("Exception while downloading file", e.getMessage());
			e.printStackTrace();
		}
		return new byte[0];
	}

	public boolean deleteFile(String fileName) {
		String key = getKey(fileName);
		s3Client.deleteObject(new DeleteObjectRequest(config.getBucketName(), key));
		return true;
	}

	public int deleteAllFiles() {
		List<String> allFiles = getAllFiles();
		List<KeyVersion> keys = allFiles.stream().map(KeyVersion::new).collect(Collectors.toList());
		if (keys.isEmpty()) {
			return 0;
		}
		DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(config.getBucketName()).withKeys(keys)
				.withQuiet(false);
		DeleteObjectsResult delObjRes = s3Client.deleteObjects(multiObjectDeleteRequest);
		return delObjRes.getDeletedObjects().size();
	}

	public String copyFile(String fileName) {
		String key = getKey(fileName);
		String copyFileName = "copy_" + getRandomFileName(fileName);
		CopyObjectRequest copyObjRequest = new CopyObjectRequest(config.getBucketName(), key, config.getBucketName(),
				getKey(copyFileName));
		s3Client.copyObject(copyObjRequest);
		return copyFileName;
	}

	public String createBucket() {
		if (!s3Client.doesBucketExistV2(config.getBucketName())) {
			s3Client.createBucket(config.getBucketName());
			return "New bucket created with bucket name:- " + config.getBucketName();
		} else {
			return config.getBucketName() + " bucket already exists";
		}
	}

	public List<String> getAllBuckets() {
		List<Bucket> buckets = s3Client.listBuckets();
		return buckets.stream().map(Bucket::getName).collect(Collectors.toList());
	}

	// Bucket should be emptied before it can be deleted
	public String deleteBucket() {
		if (s3Client.doesBucketExistV2(config.getBucketName())) {
			deleteAllFiles();
			s3Client.deleteBucket(config.getBucketName());
			return config.getBucketName() + " bucket deleted successfully";
		}
		return config.getBucketName() + " bucket does not exsit";
	}

	private File convertMultiPartFileToFile(MultipartFile multiPartFile) {
		File file = new File(multiPartFile.getOriginalFilename());
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(multiPartFile.getBytes());
		} catch (IOException e) {
			log.error("Exception while converting multipartFile to file", e.getMessage());
			return null;
		}
		return file;
	}

	public String getContentTypeString(String filename) {
		String[] fileArrSplit = filename.split("\\.");
		String fileExtension = fileArrSplit[fileArrSplit.length - 1];
		switch (fileExtension) {
		case "txt":
			return MediaType.TEXT_PLAIN_VALUE;
		case "jpg":
			return MediaType.IMAGE_JPEG_VALUE;
		case "pdf":
			return MediaType.APPLICATION_PDF_VALUE;
		case "html":
			return MediaType.TEXT_HTML_VALUE;
		default:
			return MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}
	}

	public MediaType getContentType(String filename) {
		String[] fileArrSplit = filename.split("\\.");
		String fileExtension = fileArrSplit[fileArrSplit.length - 1];
		switch (fileExtension) {
		case "txt":
			return MediaType.TEXT_PLAIN;
		case "jpg":
			return MediaType.IMAGE_JPEG;
		case "pdf":
			return MediaType.APPLICATION_PDF;
		case "html":
			return MediaType.TEXT_HTML;
		default:
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

	/**
	 * If folder name is not specified, store files directly inside the bucket. If
	 * folder name is specified, then create a folder inside the bucket and store
	 * the files in that folder
	 * 
	 * @param fileName
	 * @return
	 */
	private String getKey(String fileName) {
		if (config.getFolderName() != null && !"".equals(config.getFolderName())) {
			return config.getFolderName() + File.separator + fileName;
		}
		return fileName;
	}

	/**
	 * File name is appended with random UUID to avoid overwriting of files with
	 * same file name
	 * 
	 * @param fileName
	 * @return
	 */
	private String getRandomFileName(String fileName) {
		return UUID.randomUUID().toString() + "_" + fileName;
	}

}
