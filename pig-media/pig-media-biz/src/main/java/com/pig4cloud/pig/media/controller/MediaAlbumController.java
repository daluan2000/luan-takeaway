package com.pig4cloud.pig.media.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.media.api.entity.MediaAlbum;
import com.pig4cloud.pig.media.api.entity.MediaAlbumItem;
import com.pig4cloud.pig.media.model.dto.AlbumCreateDTO;
import com.pig4cloud.pig.media.model.dto.AlbumItemAddDTO;
import com.pig4cloud.pig.media.model.dto.AlbumItemSortDTO;
import com.pig4cloud.pig.media.mapper.MediaAlbumItemMapper;
import com.pig4cloud.pig.media.service.MediaAlbumService;
import com.pig4cloud.pig.media.support.MediaAuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 相册管理接口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/media/albums")
@Tag(description = "media-albums", name = "相册管理")
public class MediaAlbumController {

	private final MediaAlbumService mediaAlbumService;

	private final MediaAlbumItemMapper mediaAlbumItemMapper;

	@PostMapping
	@SysLog("创建相册")
	@HasPermission("media_album_add")
	@Operation(summary = "创建相册", description = "创建相册")
	public R<MediaAlbum> create(@Valid @RequestBody AlbumCreateDTO dto) {
		Long userId = MediaAuthUtils.currentUserId();
		return R.ok(mediaAlbumService.createAlbum(dto, userId));
	}

	@PutMapping("/{id}")
	@SysLog("修改相册")
	@HasPermission("media_album_edit")
	@Operation(summary = "修改相册", description = "修改相册")
	public R<?> update(@PathVariable Long id, @Valid @RequestBody AlbumCreateDTO dto) {
		Long userId = MediaAuthUtils.currentUserId();
		return mediaAlbumService.updateAlbum(id, dto, userId) ? R.ok() : R.failed("无权限或相册不存在");
	}

	@GetMapping("/page")
	@Operation(summary = "相册分页", description = "相册分页")
	public R<IPage<MediaAlbum>> page(@ParameterObject Page<MediaAlbum> page, @ParameterObject MediaAlbum query) {
		Long userId = MediaAuthUtils.currentUserId();
		return R.ok(mediaAlbumService.page(page, Wrappers.<MediaAlbum>lambdaQuery()
			.eq(MediaAlbum::getOwnerId, userId)
			.like(StrUtil.isNotBlank(query.getName()), MediaAlbum::getName, query.getName())
			.orderByDesc(MediaAlbum::getCreateTime)));
	}

	@DeleteMapping("/{id}")
	@SysLog("删除相册")
	@HasPermission("media_album_del")
	@Operation(summary = "删除相册", description = "删除相册")
	public R<?> delete(@PathVariable Long id) {
		Long userId = MediaAuthUtils.currentUserId();
		return mediaAlbumService.deleteAlbum(id, userId) ? R.ok() : R.failed("无权限或相册不存在");
	}

	@GetMapping("/{id}/items")
	@Operation(summary = "相册图片列表", description = "相册图片列表")
	public R<List<MediaAlbumItem>> items(@PathVariable Long id) {
		Long userId = MediaAuthUtils.currentUserId();
		if (!mediaAlbumService.isAlbumOwner(id, userId)) {
			return R.failed("无权限或相册不存在");
		}
		return R.ok(mediaAlbumItemMapper.selectList(com.baomidou.mybatisplus.core.toolkit.Wrappers
				.<MediaAlbumItem>lambdaQuery()
				.eq(MediaAlbumItem::getAlbumId, id)
				.orderByAsc(MediaAlbumItem::getSortNo)));
	}

	@PostMapping("/{id}/items")
	@HasPermission("media_album_edit")
	@Operation(summary = "添加图片到相册", description = "添加图片到相册")
	public R<?> addItems(@PathVariable Long id, @Valid @RequestBody AlbumItemAddDTO dto) {
		Long userId = MediaAuthUtils.currentUserId();
		return mediaAlbumService.addItems(id, dto.getFileIds(), userId) ? R.ok() : R.failed("无权限或相册不存在");
	}

	@DeleteMapping("/{id}/items/{fileId}")
	@HasPermission("media_album_edit")
	@Operation(summary = "移除图片", description = "移除图片")
	public R<?> removeItem(@PathVariable Long id, @PathVariable Long fileId) {
		Long userId = MediaAuthUtils.currentUserId();
		return mediaAlbumService.removeItem(id, fileId, userId) ? R.ok() : R.failed("无权限或记录不存在");
	}

	@PutMapping("/{id}/items/sort")
	@HasPermission("media_album_edit")
	@Operation(summary = "图片排序", description = "图片排序")
	public R<?> sort(@PathVariable Long id, @Valid @RequestBody List<AlbumItemSortDTO> dtoList) {
		Long userId = MediaAuthUtils.currentUserId();
		return mediaAlbumService.sortItems(id, dtoList, userId) ? R.ok() : R.failed("无权限或相册不存在");
	}

}
