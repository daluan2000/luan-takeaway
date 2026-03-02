package com.pig4cloud.pig.media.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.common.file.core.FileProperties;
import com.pig4cloud.pig.common.file.core.FileTemplate;
import com.pig4cloud.pig.common.file.oss.service.OssTemplate;
import com.pig4cloud.pig.media.api.entity.MediaAlbumItem;
import com.pig4cloud.pig.media.api.entity.MediaFile;
import com.pig4cloud.pig.media.mapper.MediaAlbumItemMapper;
import com.pig4cloud.pig.media.mapper.MediaFileMapper;
import com.pig4cloud.pig.media.model.dto.FilePresignDTO;
import com.pig4cloud.pig.media.model.dto.UploadConfirmDTO;
import com.pig4cloud.pig.media.service.MediaFileService;
import com.pig4cloud.pig.media.support.MediaConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 图片服务实现
 */
@Service
@RequiredArgsConstructor
public class MediaFileServiceImpl extends ServiceImpl<MediaFileMapper, MediaFile> implements MediaFileService {

	private final FileTemplate fileTemplate;

	private final FileProperties fileProperties;

	private final MediaAlbumItemMapper mediaAlbumItemMapper;

	private final com.pig4cloud.pig.media.config.MediaProperties mediaProperties;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> upload(MultipartFile file, Long userId) {
		validateFile(file);
		String objectKey = buildObjectKey(userId, file.getOriginalFilename());

		try (InputStream inputStream = file.getInputStream()) {
			fileTemplate.putObject(fileProperties.getBucketName(), objectKey, inputStream, file.getContentType());
		}
		catch (Exception e) {
			throw new IllegalStateException("上传图片失败: " + e.getMessage(), e);
		}

		MediaFile mediaFile = buildMediaFile(userId, objectKey, file.getOriginalFilename(), file.getContentType(),
				file.getSize(), null);
		this.save(mediaFile);

		Map<String, Object> result = new HashMap<>(8);
		result.put("id", mediaFile.getId());
		result.put("bucketName", mediaFile.getBucketName());
		result.put("objectKey", objectKey);
		result.put("originName", mediaFile.getOriginName());
		result.put("contentType", mediaFile.getContentType());
		result.put("fileSize", mediaFile.getFileSize());
		result.put("viewUrl", buildViewUrl(objectKey));
		return result;
	}

	@Override
	public Map<String, Object> presign(FilePresignDTO dto, Long userId) {
		String objectKey = buildObjectKey(userId, dto.getFileName());
		Map<String, Object> result = new HashMap<>(6);
		result.put("bucketName", fileProperties.getBucketName());
		result.put("objectKey", objectKey);
		result.put("uploadMethod", "PUT");
		result.put("expireSeconds", 600);
		result.put("confirmApi", "/media/files/confirm");
		if (fileTemplate instanceof OssTemplate ossTemplate) {
			result.put("previewUrl", ossTemplate.getObjectURL(fileProperties.getBucketName(), objectKey, 600));
		}
		return result;
	}

	@Override
	public MediaFile confirmUpload(UploadConfirmDTO dto, Long userId) {
		MediaFile mediaFile = buildMediaFile(userId, dto.getObjectKey(), dto.getOriginName(), dto.getContentType(),
				dto.getFileSize(), dto.getMd5());
		this.save(mediaFile);
		return mediaFile;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteFile(Long fileId, Long userId) {
		MediaFile file = this.getById(fileId);
		if (file == null || !userId.equals(file.getOwnerId())) {
			return false;
		}
		try {
			fileTemplate.removeObject(file.getBucketName(), file.getObjectKey());
		}
		catch (Exception ignored) {
		}
		mediaAlbumItemMapper.delete(Wrappers.<MediaAlbumItem>lambdaQuery().eq(MediaAlbumItem::getFileId, fileId));
		return this.removeById(fileId);
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("图片不能为空");
		}
		long maxSize = mediaProperties.getUpload().getMaxSize();
		if (file.getSize() > maxSize) {
			throw new IllegalArgumentException("图片大小超过限制");
		}
		String contentType = StrUtil.blankToDefault(file.getContentType(), "");
		Set<String> allowTypes = StrUtil.split(mediaProperties.getUpload().getAllowedTypes(), ',').stream()
				.map(String::trim)
				.collect(Collectors.toSet());
		if (!allowTypes.contains(contentType)) {
			throw new IllegalArgumentException("图片类型不支持: " + contentType);
		}
	}

	private MediaFile buildMediaFile(Long userId, String objectKey, String originName, String contentType, Long fileSize,
			String md5) {
		MediaFile mediaFile = new MediaFile();
		mediaFile.setOwnerId(userId);
		mediaFile.setBucketName(fileProperties.getBucketName());
		mediaFile.setObjectKey(objectKey);
		mediaFile.setOriginName(originName);
		mediaFile.setContentType(contentType);
		mediaFile.setFileSize(fileSize);
		mediaFile.setMd5(md5);
		mediaFile.setStatus(MediaConstants.FILE_STATUS_ACTIVE);
		return mediaFile;
	}

	private String buildObjectKey(Long userId, String originName) {
		LocalDate today = LocalDate.now();
		String ext = FileUtil.extName(originName);
		String name = IdUtil.fastSimpleUUID();
		if (StrUtil.isBlank(ext)) {
			return String.format("images/%s/%d/%02d/%02d/%s", userId, today.getYear(), today.getMonthValue(),
					today.getDayOfMonth(), name);
		}
		return String.format("images/%s/%d/%02d/%02d/%s.%s", userId, today.getYear(), today.getMonthValue(),
				today.getDayOfMonth(), name, ext);
	}

	private String buildViewUrl(String objectKey) {
		return String.format("/media/files/object/%s/%s", fileProperties.getBucketName(), objectKey);
	}

}
