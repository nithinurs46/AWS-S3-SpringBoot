package com.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.svc.AwsS3Service;

@RestController
@RequestMapping("/s3")
public class AwsS3Controller {

	@Autowired
	AwsS3Service s3Svc;

	@GetMapping(value = "/showFiles")
	public ResponseEntity<List<String>> listAllFiles() {
		return ResponseEntity.ok().body(s3Svc.getAllFiles());
	}

	@PostMapping(value = "/upload")
	public ResponseEntity<String> uploadFile(@RequestPart(value = "file") MultipartFile file) {
		String newFileName = s3Svc.storeFileInBucket(file);
		String message = file.getOriginalFilename() + " uploaded with new file name as :- " + newFileName;
		if ("".equals(newFileName)) {
			message = "Upload operation failed for the file:- " + file.getOriginalFilename();
		}
		return new ResponseEntity<>(message, HttpStatus.OK);
	}

	@GetMapping(value = "/download")
	public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam String fileName) {
		byte[] file = s3Svc.downloadFileFromBucket(fileName);
		return ResponseEntity.ok().contentLength(file.length).contentType(s3Svc.getContentType(fileName))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.body(new ByteArrayResource(file));
	}

	@DeleteMapping(value = "/delete")
	public ResponseEntity<String> deleteFile(@RequestParam String fileName) {
		boolean status = s3Svc.deleteFile(fileName);
		String message = fileName + " file deleted successfully";
		if (!status) {
			message = "Unable to delete the file:- " + fileName;
		}
		return new ResponseEntity<>(message, HttpStatus.OK);
	}

	@DeleteMapping(value = "/deleteAll")
	public ResponseEntity<String> deleteAllFiles() {
		int count = s3Svc.deleteAllFiles();
		return new ResponseEntity<>(count + " files deleted", HttpStatus.OK);
	}

	@PutMapping(value = "/copy")
	public ResponseEntity<String> copyFile(@RequestParam String fileName) {
		String copyFileName = s3Svc.copyFile(fileName);
		return new ResponseEntity<>("File copied successfully with new file name as :- " + copyFileName, HttpStatus.OK);
	}

	@GetMapping(value = "/showAllBuckets")
	public ResponseEntity<List<String>> listAllBuckets() {
		return ResponseEntity.ok().body(s3Svc.getAllBuckets());
	}

	@PostMapping(value = "/createBucket")
	public ResponseEntity<String> createBucket() {
		String message = s3Svc.createBucket();
		return new ResponseEntity<>(message, HttpStatus.OK);
	}

	@DeleteMapping(value = "/deleteBucket")
	public ResponseEntity<String> deleteBucket() {
		String message = s3Svc.deleteBucket();
		return new ResponseEntity<>(message, HttpStatus.OK);
	}
}
