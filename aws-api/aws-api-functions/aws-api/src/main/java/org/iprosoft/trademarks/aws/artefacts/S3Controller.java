package org.iprosoft.trademarks.aws.artefacts;

import org.iprosoft.trademarks.aws.artefacts.aws.s3.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
// @RequestMapping("/file")
public class S3Controller {

	@Autowired
	private S3Service service;

	/*
	 * @PostMapping("/upload") public ResponseEntity<String>
	 * uploadFile(@RequestParam(value = "file") MultipartFile file) { return new
	 * ResponseEntity<>(service.uploadFile(file), HttpStatus.OK); }
	 *
	 * @GetMapping("/download/{fileName}") public ResponseEntity<ByteArrayResource>
	 * downloadFile(@PathVariable String fileName) { byte[] data =
	 * service.downloadFile(fileName); ByteArrayResource resource = new
	 * ByteArrayResource(data); return ResponseEntity.ok() .contentLength(data.length)
	 * .header("Content-type", "application/octet-stream") .header("Content-disposition",
	 * "attachment; filename=\"" + fileName + "\"") .body(resource); }
	 *
	 * @DeleteMapping("/delete/{fileName}") public ResponseEntity<String>
	 * deleteFile(@PathVariable String fileName) { return new
	 * ResponseEntity<>(service.deleteFile(fileName), HttpStatus.OK); }
	 */
	@GetMapping("/isObjectExist/{bucket}/{fileName}")
	String isObjectExist(@PathVariable String bucket, @PathVariable String fileName) {
		// return service.isObjectExist(fileName).toString();
		// return S3Util.doesObjectExist2( service, bucket, fileName).toString();
		// return service.isFileExist(bucket, fileName).toString();
		return "";
	}

}
