package com.pig4cloud.pig.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.media.api.entity.MediaAlbum;
import com.pig4cloud.pig.media.model.dto.AlbumCreateDTO;
import com.pig4cloud.pig.media.model.dto.AlbumItemSortDTO;

import java.util.List;

public interface MediaAlbumService extends IService<MediaAlbum> {

	MediaAlbum createAlbum(AlbumCreateDTO dto, Long userId);

	boolean updateAlbum(Long albumId, AlbumCreateDTO dto, Long userId);

	boolean deleteAlbum(Long albumId, Long userId);

	boolean addItems(Long albumId, List<Long> fileIds, Long userId);

	boolean removeItem(Long albumId, Long fileId, Long userId);

	boolean sortItems(Long albumId, List<AlbumItemSortDTO> sorts, Long userId);

	boolean isAlbumOwner(Long albumId, Long userId);

}
