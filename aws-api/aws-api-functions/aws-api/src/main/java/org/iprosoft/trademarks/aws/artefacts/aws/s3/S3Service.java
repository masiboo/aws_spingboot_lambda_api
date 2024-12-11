package org.iprosoft.trademarks.aws.artefacts.aws.s3;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iprosoft.trademarks.aws.artefacts.model.dto.S3ObjectMetadata;
import org.iprosoft.trademarks.aws.artefacts.util.DebugInfoUtil;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest.Builder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;
import software.amazon.awssdk.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

	@Getter
	private final S3Client s3Client;

	private final S3Presigner signer;

	public static String encode(final String string) {
		return URLEncoder.encode(string, StandardCharsets.UTF_8);
	}

	public static String decode(final String string) {
		return URLDecoder.decode(string, StandardCharsets.UTF_8);
	}

	public static byte[] toByteArray(final InputStream is) throws IOException {
		return IoUtils.toByteArray(is);
	}

	public void copyObject(final String sourceBucket, final String sourceKey, final String destinationBucket,
			final String destinationKey, final String contentType) {
		CopyObjectRequest.Builder req = CopyObjectRequest.builder()
			.copySource(encode(sourceBucket + "/" + sourceKey))
			.destinationBucket(destinationBucket)
			.destinationKey(destinationKey);

		if (contentType != null) {
			req = req.contentType(contentType);
		}

		s3Client.copyObject(req.build());
	}

	public void createBucket(final String bucket) {
		s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
		s3Client.putBucketVersioning(PutBucketVersioningRequest.builder()
			.bucket(bucket)
			.versioningConfiguration(VersioningConfiguration.builder().status(BucketVersioningStatus.ENABLED).build())
			.build());
	}

	public void deleteAllFiles(final String bucket) {
		boolean isDone = false;

		while (!isDone) {

			ListObjectsRequest req = ListObjectsRequest.builder().bucket(bucket).build();
			ListObjectsResponse resp = s3Client.listObjects(req);

			for (S3Object s3Object : resp.contents()) {
				deleteObject(bucket, s3Object.key());
			}

			isDone = !resp.isTruncated();
		}
	}

	public void deleteAllObjectTags(final String bucket, final String key) {
		DeleteObjectTaggingRequest req = DeleteObjectTaggingRequest.builder().bucket(bucket).key(key).build();
		s3Client.deleteObjectTagging(req);
	}

	public void deleteObject(final String bucket, final String key) {
		s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
	}

	public boolean exists(final String bucket) {
		try {
			s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
			return true;
		}
		catch (NoSuchBucketException e) {
			return false;
		}
	}

	public InputStream getContentAsInputStream(final String distributionBucket, final String key) {
		GetObjectRequest get = GetObjectRequest.builder().bucket(distributionBucket).key(key).build();
		ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(get);

		return response.asInputStream();
	}

	public String getContentAsString(final String bucket, final String key, final String versionId) {

		GetObjectRequest gr = GetObjectRequest.builder().bucket(bucket).key(key).versionId(versionId).build();
		ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(gr);

		return response.asUtf8String();
	}

	public GetBucketNotificationConfigurationResponse getNotifications(final String bucket) {
		GetBucketNotificationConfigurationRequest req = GetBucketNotificationConfigurationRequest.builder()
			.bucket(bucket)
			.build();
		return s3Client.getBucketNotificationConfiguration(req);
	}

	public S3ObjectMetadata getObjectMetadata(final String bucket, final String key) {
		HeadObjectRequest hr = HeadObjectRequest.builder().bucket(bucket).key(key).build();
		S3ObjectMetadata md = new S3ObjectMetadata();
		try {
			HeadObjectResponse resp = s3Client.headObject(hr);
			Map<String, String> metadata = resp.metadata();
			md.setObjectExists(true);
			md.setContentType(resp.contentType());
			md.setMetadata(metadata);
			md.setEtag(resp.eTag());
			md.setContentLength(resp.contentLength());
		}
		catch (NoSuchKeyException e) {
			md.setObjectExists(false);
		}
		return md;
	}

	public GetObjectTaggingResponse getObjectTags(final String bucket, final String key) {
		GetObjectTaggingRequest req = GetObjectTaggingRequest.builder().bucket(bucket).key(key).build();
		return s3Client.getObjectTagging(req);
	}

	public ListObjectVersionsResponse getObjectVersions(final String bucket, final String prefix,
			final String keyMarker) {
		ListObjectVersionsRequest req = ListObjectVersionsRequest.builder()
			.bucket(bucket)
			.prefix(prefix)
			.keyMarker(keyMarker)
			.build();
		return s3Client.listObjectVersions(req);
	}

	public Set<String> listObjectKeys(final String bucket) {
		Set<String> objKeys = new TreeSet<>();
		ListObjectsResponse listObjectsResp = listObjects(bucket, null);
		List<S3Object> objectList = listObjectsResp.contents();
		ListIterator<S3Object> listIterator = objectList.listIterator();
		while (listIterator.hasNext()) {
			objKeys.add(listIterator.next().key());
		}
		return objKeys;
	}

	public ListObjectsResponse listObjects(final String bucket, final String prefix) {
		Builder listbuilder = ListObjectsRequest.builder().bucket(bucket);

		if (prefix != null) {
			listbuilder = listbuilder.prefix(prefix);
		}

		return s3Client.listObjects(listbuilder.build());
	}

	public URL presignGetUrl(final String bucket, final String key, final Duration duration, final String versionId) {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.versionId(versionId)
			.build();
		GetObjectPresignRequest getRequest = GetObjectPresignRequest.builder()
			.signatureDuration(duration)
			.getObjectRequest(getObjectRequest)
			.build();
		PresignedGetObjectRequest req = signer.presignGetObject(getRequest);
		return req.url();
	}

	public URL presignPostUrl(final String bucket, final String key, final Duration duration,
			final Optional<Long> contentLength) {
		UploadPartRequest.Builder uploadBuilder = UploadPartRequest.builder().bucket(bucket).key(key);
		if (contentLength.isPresent()) {
			uploadBuilder = uploadBuilder.contentLength(contentLength.get());
		}
		UploadPartPresignRequest preReq = UploadPartPresignRequest.builder()
			.signatureDuration(duration)
			.uploadPartRequest(uploadBuilder.build())
			.build();
		PresignedUploadPartRequest req = signer.presignUploadPart(preReq);
		return req.url();
	}

	public URL presignPutUrl(final String bucket, final String key, final Duration duration) {
		PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket).key(key).build();
		PutObjectPresignRequest putRequest = PutObjectPresignRequest.builder()
			.signatureDuration(duration)
			.putObjectRequest(putObjectRequest)
			.build();
		PresignedPutObjectRequest req = signer.presignPutObject(putRequest);
		return req.url();
	}

	public URL presignPutUrl(final String bucket, final String key, final Duration duration,
			Map<String, String> metadata) {
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.metadata(metadata)
			.build();
		PutObjectPresignRequest putRequest = PutObjectPresignRequest.builder()
			.signatureDuration(duration)
			.putObjectRequest(putObjectRequest)
			.build();
		PresignedPutObjectRequest req = signer.presignPutObject(putRequest);

		return req.url();
	}

	public PutObjectResponse putObject(final String bucket, final String key, final byte[] data,
			final String contentType) {
		return putObject(bucket, key, data, contentType, null);
	}

	public PutObjectResponse putObject(final String bucket, final String key, final byte[] data,
			final String contentType, final Map<String, String> metadata) {
		int contentLength = data.length;
		PutObjectRequest.Builder build = PutObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.contentLength((long) contentLength);

		if (contentType != null) {
			build.contentType(contentType);
		}

		if (metadata != null) {
			build.metadata(metadata);
		}

		return s3Client.putObject(build.build(), RequestBody.fromBytes(data));
	}

	public String putFile(String s3Bucket, String s3Key, File inputFile, String contentType) {
		PutObjectRequest request = PutObjectRequest.builder()
			.bucket(s3Bucket)
			.key(s3Key)
			.contentType(contentType)
			.build();
		// Upload the file to S3
		PutObjectResponse response = s3Client.putObject(request, RequestBody.fromFile(inputFile));
		log.info("PutObjectResponse; " + response.toString());
		// Verify the response
		if (response.sdkHttpResponse().isSuccessful()) {
			log.info("File successfully uploaded to S3. ETag: " + response.eTag());
		}
		else {
			log.error("Failed to upload file to S3. HTTP Status Code: " + response.sdkHttpResponse().statusCode());
			throw new RuntimeException(
					"Failed to upload file to S3. Status Code: " + response.sdkHttpResponse().statusCode());
		}
		return presignGetUrl(s3Bucket, s3Key, Duration.ofHours(2), null).toString();
	}

	public PutObjectResponse putObject(final String bucket, final String key, final InputStream is,
			final String contentType) throws IOException {
		byte[] data = toByteArray(is);
		return putObject(bucket, key, data, contentType, null);
	}

	public void setObjectTag(final String bucket, final String key, final String tagKey, final String tagValue) {
		Collection<Tag> tagSet = Collections.singletonList(Tag.builder().key(tagKey).value(tagValue).build());
		setObjectTags(bucket, key, tagSet);
	}

	public void setObjectTag(final String bucket, final String key, Map<String, String> tagMap) {
		Collection<Tag> tagList = new ArrayList<>();
		tagMap.forEach((tagKey, tagValue) -> tagList.add(Tag.builder().key(tagKey).value(tagValue).build()));
		setObjectTags(bucket, key, tagList);
	}

	public void setObjectTags(final String bucket, final String key, final Collection<Tag> tags) {

		Tagging tagging = Tagging.builder().tagSet(tags).build();
		PutObjectTaggingRequest req = PutObjectTaggingRequest.builder()
			.bucket(bucket)
			.key(key)
			.tagging(tagging)
			.build();
		s3Client.putObjectTagging(req);
	}

	public ResponseInputStream<GetObjectResponse> getObject(String bucket, String fileName) {
		ResponseInputStream<GetObjectResponse> s3Object;
		try {
			log.info("Retrieving S3 Object with key {}", fileName);
			log.info("Retrieving S3 Object from bucket: {}", bucket);
			s3Object = s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(fileName).build());
		}
		catch (SdkClientException sce) {
			log.error("S3Client request was rejected with an error response for object: " + fileName
					+ " and the error message was " + sce.getMessage(), sce);
			throw sce;
		}
		catch (SdkServiceException sse) {
			log.error("S3Client encountered an internal error while trying to access S3 for object: " + fileName
					+ " and the error message was " + sse.getMessage(), sse);
			throw sse;
		}
		return s3Object;
	}

	public boolean isObjectExist(String bucket, String key) {
		try {
			HeadObjectRequest headObjectRequest = HeadObjectRequest.builder().bucket(bucket).key(key).build();
			HeadObjectResponse response = s3Client.headObject(headObjectRequest);
			return response != null;
		}
		catch (S3Exception e) {
			DebugInfoUtil.logExceptionDetails(e);
			if (e.statusCode() == 404) {
				return false;
			}
			else {
				throw e;
			}
		}
	}

	public boolean bucketExists(final String bucket) {
		try {
			s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
			log.info("S3 bucket exists: {}", bucket);
			return true;
		}
		catch (S3Exception e) {
			DebugInfoUtil.logExceptionDetails(e);
			if (e.statusCode() == 404) {
				log.warn("S3 bucket does not exist: {}", bucket);
				return false;
			}
			else {
				log.error("Error checking bucket existence for {}: {}", bucket, e.awsErrorDetails().errorMessage());
				throw e;
			}
		}
	}

	public S3ObjectMetadata getObjectMetadata(final S3Client s3, final String bucket, final String key) {
		HeadObjectRequest hr = HeadObjectRequest.builder().bucket(bucket).key(key).build();
		S3ObjectMetadata md = new S3ObjectMetadata();
		try {
			HeadObjectResponse resp = s3.headObject(hr);
			Map<String, String> metadata = resp.metadata();
			md.setObjectExists(true);
			md.setContentType(resp.contentType());
			md.setMetadata(metadata);
			md.setEtag(resp.eTag());
			md.setContentLength(resp.contentLength());
		}
		catch (NoSuchKeyException e) {
			md.setObjectExists(false);
		}

		return md;
	}

	public Boolean isFileExist(String s3Bucket, String s3Key) {
		GetObjectRequest request = GetObjectRequest.builder().bucket(s3Bucket).key(s3Key).build();
		try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request)) {
			log.info("Content-Type: {}", response.response().contentType());
			log.info("Content-Length: {}", response.response().contentLength());
			log.info("File exists in S3.");
			return true;
		}
		catch (S3Exception e) {
			if (e.statusCode() == 404) {
				log.warn("File does not exist in S3 bucket.");
				return false;
			}
			else {
				log.error("Failed to check file in S3. HTTP Status Code: {}", e.statusCode());
				return false;
			}
		}
		catch (IOException e) {
			log.error("I/O error while reading S3 object content.");
			throw new RuntimeException("I/O error while reading S3 object content.", e);
		}
	}

}
