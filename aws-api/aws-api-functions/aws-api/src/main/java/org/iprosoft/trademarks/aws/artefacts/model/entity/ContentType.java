package org.iprosoft.trademarks.aws.artefacts.model.entity;

public enum ContentType {

	APPLICATION_PDF("application/pdf"), APPLICATION_MP4("application/mp4"), APPLICATION_MP3("application/mp3"),
	APPLICATION_MPEG("application/mpeg"), APPLICATION_WAV("application/wav"), VIDEO_MP4("video/mp4"),
	AUDIO_MP4("audio/mp4"), AUDIO_MP3("audio/mp3"), AUDIO_MPEG("audio/mpeg"), AUDIO_WAV("audio/wav"),

	APPLICATION_TIF("application/tif"), APPLICATION_TIFF("application/tiff"), APPLICATION_X_TIFF("application/x-tiff"),
	APPLICATION_X_TIF("application/x-tif"), APPLICATION_BMP("application/bmp"), APPLICATION_JPEG("application/jpeg"),
	APPLICATION_JPG("application/jpg"), APPLICATION_PJPEG("application/pjpeg"), APPLICATION_PNG("application/png"),
	APPLICATION_GIF("application/gif"), APPLICATION_XML("application/xml"), APPLICATION_XLS("application/xls"),

	IMAGE_TIFF("image/tiff"), IMAGE_TIF("image/tif"), IMAGE_X_TIFF("image/x-tiff"), IMAGE_X_TIF("image/x-tif"),
	IMAGE_BMP("image/bmp"), IMAGE_JPEG("image/jpeg"), IMAGE_JPG("image/jpg"), IMAGE_PJPEG("image/pjpeg"),
	IMAGE_PNG("image/png"), IMAGE_GIF("image/gif"),

	GIF("gif"), BMP("bmp"), PNG("png"), JPEG("jpeg"), JPG("jpg"), PJPEG("pjpeg"), TIF("tif"), TIFF("tiff"),
	X_TIFF("x-tiff"), X_TIF("x-tif"), PDF("pdf"), MP4("mp4"), MP3("mp3"), MPEG("mpeg"), WAV("wav"), XML("xml"),
	XLS("xls");

	private final String contentType;

	ContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

}
