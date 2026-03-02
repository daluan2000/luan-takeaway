package com.pig4cloud.pig.media.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.media.api.entity.MediaAlbum;
import com.pig4cloud.pig.media.api.entity.MediaAlbumItem;
import com.pig4cloud.pig.media.api.entity.MediaFile;
import com.pig4cloud.pig.media.mapper.MediaAlbumItemMapper;
import com.pig4cloud.pig.media.mapper.MediaAlbumMapper;
import com.pig4cloud.pig.media.mapper.MediaFileMapper;
import com.pig4cloud.pig.media.model.dto.AlbumCreateDTO;
import com.pig4cloud.pig.media.model.dto.AlbumItemSortDTO;
import com.pig4cloud.pig.media.service.MediaAlbumService;
import com.pig4cloud.pig.media.support.MediaConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 相册服务实现
 */
@Service
@RequiredArgsConstructor
public class MediaAlbumServiceImpl extends ServiceImpl<MediaAlbumMapper, MediaAlbum> implements MediaAlbumService {

	private final MediaAlbumItemMapper mediaAlbumItemMapper;

	private final MediaFileMapper mediaFileMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public MediaAlbum createAlbum(AlbumCreateDTO dto, Long userId) {
		MediaAlbum album = new MediaAlbum();
		album.setOwnerId(userId);
		album.setName(dto.getName());
		album.setDescription(dto.getDescription());
		album.setCoverFileId(dto.getCoverFileId());
		album.setVisibleScope(StrUtil.blankToDefault(dto.getVisibleScope(), MediaConstants.ALBUM_SCOPE_PRIVATE));
		this.save(album);
		return album;
	}

	@Override
	public boolean updateAlbum(Long albumId, AlbumCreateDTO dto, Long userId) {
		MediaAlbum album = this.getById(albumId);
		if (album == null || !userId.equals(album.getOwnerId())) {
			return false;
		}
		album.setName(dto.getName());
		album.setDescription(dto.getDescription());
		album.setCoverFileId(dto.getCoverFileId());
		if (StrUtil.isNotBlank(dto.getVisibleScope())) {
			album.setVisibleScope(dto.getVisibleScope());
		}
		return this.updateById(album);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteAlbum(Long albumId, Long userId) {
		MediaAlbum album = this.getById(albumId);
		if (album == null || !userId.equals(album.getOwnerId())) {
			return false;
		}
		mediaAlbumItemMapper.delete(Wrappers.<MediaAlbumItem>lambdaQuery().eq(MediaAlbumItem::getAlbumId, albumId));
		return this.removeById(albumId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean addItems(Long albumId, List<Long> fileIds, Long userId) {
		if (!isAlbumOwner(albumId, userId)) {
			return false;
		}
		if (fileIds == null || fileIds.isEmpty()) {
			return true;
		}
		List<MediaAlbumItem> saveList = new ArrayList<>();
		for (Long fileId : fileIds) {
			MediaFile file = mediaFileMapper.selectById(fileId);
			if (file == null || !userId.equals(file.getOwnerId())) {
				continue;
			}
			Long count = mediaAlbumItemMapper.selectCount(Wrappers.<MediaAlbumItem>lambdaQuery()
				.eq(MediaAlbumItem::getAlbumId, albumId)
				.eq(MediaAlbumItem::getFileId, fileId));
			if (count != null && count > 0) {
				continue;
			}
			MediaAlbumItem item = new MediaAlbumItem();
			item.setAlbumId(albumId);
			item.setFileId(fileId);
			item.setSortNo(0);
			saveList.add(item);
		}
		for (MediaAlbumItem item : saveList) {
			mediaAlbumItemMapper.insert(item);
		}
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeItem(Long albumId, Long fileId, Long userId) {
		if (!isAlbumOwner(albumId, userId)) {
			return false;
		}
		return mediaAlbumItemMapper.delete(Wrappers.<MediaAlbumItem>lambdaQuery()
			.eq(MediaAlbumItem::getAlbumId, albumId)
			.eq(MediaAlbumItem::getFileId, fileId)) > 0;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean sortItems(Long albumId, List<AlbumItemSortDTO> sorts, Long userId) {
		if (!isAlbumOwner(albumId, userId)) {
			return false;
		}
		if (sorts == null || sorts.isEmpty()) {
			return true;
		}
		for (AlbumItemSortDTO sort : sorts) {
			MediaAlbumItem item = mediaAlbumItemMapper.selectById(sort.getId());
			if (item == null || !albumId.equals(item.getAlbumId())) {
				continue;
			}
			item.setSortNo(sort.getSortNo());
			mediaAlbumItemMapper.updateById(item);
		}
		return true;
	}

	@Override
	public boolean isAlbumOwner(Long albumId, Long userId) {
		MediaAlbum album = this.getById(albumId);
		return album != null && userId.equals(album.getOwnerId());
	}

}
