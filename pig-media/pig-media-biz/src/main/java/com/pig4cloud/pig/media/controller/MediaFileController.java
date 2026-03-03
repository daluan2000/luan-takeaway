package com.pig4cloud.pig.media.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.file.core.FileTemplate;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.common.security.annotation.Inner;
import com.pig4cloud.pig.media.api.entity.MediaFile;
import com.pig4cloud.pig.media.model.dto.FilePresignDTO;
import com.pig4cloud.pig.media.model.dto.UploadConfirmDTO;
import com.pig4cloud.pig.media.service.MediaFileService;
import com.pig4cloud.pig.media.support.MediaAuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * 图片管理接口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/media/files")
@Tag(description = "media-files", name = "图片管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class MediaFileController {

	private final MediaFileService mediaFileService;

	private final FileTemplate fileTemplate;

	@PostMapping("/upload")
	@SysLog("上传图片")
	@HasPermission("media_file_upload")
	@Operation(summary = "上传图片", description = "上传图片")
	public R<Map<String, Object>> upload(@RequestPart("file") MultipartFile file, HttpServletRequest request) {
		System.out.println("上传文件: " + file.getOriginalFilename() + ", 大小: " + file.getSize());
		Long userId = MediaAuthUtils.currentUserId();
		Map<String, Object> result = mediaFileService.upload(file, userId);
		Object bucketName = result.get("bucketName");
		Object objectKey = result.get("objectKey");
		if (bucketName != null && objectKey != null) {
			result.put("viewUrl", buildContextPathUrl(request,
					String.format("/media/files/object/%s/%s", bucketName, objectKey)));
		}
		return R.ok(result);
	}

	@PostMapping("/presign")
	@HasPermission("media_file_upload")
	@Operation(summary = "获取预签名参数", description = "获取预签名参数")
	public R<Map<String, Object>> presign(@Valid @RequestBody FilePresignDTO dto) {
		Long userId = MediaAuthUtils.currentUserId();
		return R.ok(mediaFileService.presign(dto, userId));
	}

	@PostMapping("/confirm")
	@HasPermission("media_file_upload")
	@Operation(summary = "确认上传并入库", description = "确认上传并入库")
	public R<MediaFile> confirm(@Valid @RequestBody UploadConfirmDTO dto) {
		Long userId = MediaAuthUtils.currentUserId();
		return R.ok(mediaFileService.confirmUpload(dto, userId));
	}

	@GetMapping("/page")
	@Operation(summary = "我的图片分页", description = "我的图片分页")
	public R<IPage<MediaFile>> page(@ParameterObject Page<MediaFile> page, @ParameterObject MediaFile query) {
		Long userId = MediaAuthUtils.currentUserId();
		return R.ok(mediaFileService.page(page, Wrappers.<MediaFile>lambdaQuery()
			.eq(MediaFile::getOwnerId, userId)
			.like(StrUtil.isNotBlank(query.getOriginName()), MediaFile::getOriginName, query.getOriginName())
			.orderByDesc(MediaFile::getCreateTime)));
	}

	@DeleteMapping("/{id}")
	@SysLog("删除图片")
	@HasPermission("media_file_del")
	@Operation(summary = "删除图片", description = "删除图片")
	public R<?> delete(@PathVariable Long id) {
		Long userId = MediaAuthUtils.currentUserId();
		boolean removed = mediaFileService.deleteFile(id, userId);
		return removed ? R.ok() : R.failed("无权限或图片不存在");
	}

	@Inner(false)
	@GetMapping("/object/{bucket}/{objectKey:.+}")
	@Operation(summary = "获取对象流", description = "获取对象流")
	public void file(@PathVariable String bucket, @PathVariable String objectKey, HttpServletResponse response) {
		try (InputStream inputStream = (InputStream) fileTemplate.getObject(bucket, objectKey)) {
			response.setContentType("application/octet-stream; charset=UTF-8");
			response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=" + objectKey.substring(objectKey.lastIndexOf('/') + 1));
			IoUtil.copy(inputStream, response.getOutputStream());
		}
		catch (Exception ignored) {
		}
	}

	@Inner(false)
	@GetMapping("/{id}/download-url")
	@Operation(summary = "获取下载地址", description = "获取下载地址")
	public R<String> getDownloadUrl(@PathVariable Long id, HttpServletRequest request) {
		MediaFile file = mediaFileService.getById(id);
		if (file == null) {
			return R.failed("图片不存在");
		}
		return R.ok(buildContextPathUrl(request, String.format("/media/files/%s/download", id)));
	}

	@Inner(false)
	@GetMapping("/{id}/download")
	@Operation(summary = "按ID下载图片", description = "按ID下载图片")
	public void download(@PathVariable Long id, HttpServletResponse response) {
		MediaFile file = mediaFileService.getById(id);
		if (file == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		try (InputStream inputStream = (InputStream) fileTemplate.getObject(file.getBucketName(), file.getObjectKey())) {
			response.setContentType(StrUtil.blankToDefault(file.getContentType(), "application/octet-stream"));
			response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
					"attachment; filename=" + StrUtil.blankToDefault(file.getOriginName(), String.valueOf(id)));
			IoUtil.copy(inputStream, response.getOutputStream());
		}
		catch (Exception ignored) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	private String buildContextPathUrl(HttpServletRequest request, String path) {
		String contextPath = request.getContextPath();
		if (StrUtil.isBlank(contextPath) || "/".equals(contextPath)) {
			return path;
		}
		return contextPath + path;
	}

}
