# AWS-S3-SpringBoot

AWS-S3 using spring boot.

Goto My Security Credentials in AWS Console at top right corner to generate access key and secrety key

AWS-SDK dependency is used.

Provide above generated keys in application.properties

Provide bucket name and folder name in application.properties

URLs - 

Bucket operations – 
1.	Show all Buckets –
GET URL - http://localhost:8080/s3/showAllBuckets
2.	Create bucket – 
POST URL – http://localhost:8080/s3/createBucket
3.	Delete bucket – 
DELETE URL - http://localhost:8080/s3/deleteBucket

Object operations - 
1. Add file - 
POST URL - http://localhost:8080/s3/upload

2. Download File – 
GET  URL - http://localhost:8080/s3/download?fileName=7e862d40-7974-4835-9c80-cf229b7f27a6_uploadtest_1.txt

3. Display all files – 
GET request URL - http://localhost:8080/s3/showFiles

4. Copy File – 
PUT request URL – http://localhost:8080/s3/copy?fileName=7e862d40-7974-4835-9c80-cf229b7f27a6_uploadtest_1.txt

5. Delete File – 
DELETE request URL – http://localhost:8080/s3/delete?fileName=7e862d40-7974-4835-9c80-cf229b7f27a6_uploadtest_1.txt

6. Delete All files in bucket –
DELETE request URL - http://localhost:8080/s3/deleteAll
