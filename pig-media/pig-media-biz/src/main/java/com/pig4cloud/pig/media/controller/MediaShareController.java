package com.pig4cloud.pig.media.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.common.security.annotation.Inner;
import com.pig4cloud.pig.media.api.entity.MediaShareLink;
import com.pig4cloud.pig.media.model.dto.ShareCreateDTO;
import com.pig4cloud.pig.media.model.dto.ShareVerifyDTO;
import com.pig4cloud.pig.media.model.vo.ShareDetailVO;
import com.pig4cloud.pig.media.service.MediaShareService;
import com.pig4cloud.pig.media.support.MediaAuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
 * 分享管理接口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/media/shares")
@Tag(description = "media-shares", name = "分享管理")
public class MediaShareController {

	private final MediaShareService mediaShareService;

	@PostMapping
	@SysLog("创建相册分享")
	@HasPermission("media_share_add")
	@Operation(summary = "创建分享链接", description = "创建分享链接")
	public R<MediaShareLink> create(@Valid @RequestBody ShareCreateDTO dto) {
		Long userId = MediaAuthUtils.currentUserId();
		return R.ok(mediaShareService.createShare(dto, userId));
	}

	@GetMapping("/page")
	@Operation(summary = "分享分页", description = "分享分页")
	public R<IPage<MediaShareLink>> page(@ParameterObject Page<MediaShareLink> page,
			@ParameterObject MediaShareLink query) {
		Long userId = MediaAuthUtils.currentUserId();
		return R.ok(mediaShareService.page(page, Wrappers.<MediaShareLink>lambdaQuery()
			.eq(MediaShareLink::getOwnerId, userId)
			.eq(query.getAlbumId() != null, MediaShareLink::getAlbumId, query.getAlbumId())
			.orderByDesc(MediaShareLink::getCreateTime)));
	}

	@DeleteMapping("/{id}")
	@SysLog("关闭相册分享")
	@HasPermission("media_share_del")
	@Operation(summary = "关闭分享", description = "关闭分享")
	public R<?> delete(@PathVariable Long id) {
		Long userId = MediaAuthUtils.currentUserId();
		return mediaShareService.disableShare(id, userId) ? R.ok() : R.failed("无权限或分享不存在");
	}

	@Inner(false)
	@GetMapping("/{token}")
	@Operation(summary = "分享详情", description = "分享详情（匿名可访问）")
	public R<ShareDetailVO> detail(@PathVariable String token) {
		return R.ok(mediaShareService.detail(token));
	}

	@Inner(false)
	@PostMapping("/{token}/verify")
	@Operation(summary = "校验提取码", description = "校验提取码")
	public R<?> verify(@PathVariable String token, @RequestBody(required = false) ShareVerifyDTO dto) {
		ShareVerifyDTO verifyDTO = dto == null ? new ShareVerifyDTO() : dto;
		return mediaShareService.verify(token, verifyDTO) ? R.ok() : R.failed("提取码错误");
	}

}
