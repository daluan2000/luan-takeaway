package com.pig4cloud.pig.media.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.media.api.entity.MediaAlbum;
import com.pig4cloud.pig.media.api.entity.MediaAlbumItem;
import com.pig4cloud.pig.media.api.entity.MediaShareAccessLog;
import com.pig4cloud.pig.media.api.entity.MediaShareLink;
import com.pig4cloud.pig.media.config.MediaProperties;
import com.pig4cloud.pig.media.mapper.MediaAlbumItemMapper;
import com.pig4cloud.pig.media.mapper.MediaAlbumMapper;
import com.pig4cloud.pig.media.mapper.MediaShareAccessLogMapper;
import com.pig4cloud.pig.media.mapper.MediaShareLinkMapper;
import com.pig4cloud.pig.media.model.dto.ShareCreateDTO;
import com.pig4cloud.pig.media.model.dto.ShareVerifyDTO;
import com.pig4cloud.pig.media.model.vo.ShareDetailVO;
import com.pig4cloud.pig.media.service.MediaShareService;
import com.pig4cloud.pig.media.support.MediaConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 分享服务实现
 */
@Service
@RequiredArgsConstructor
public class MediaShareServiceImpl extends ServiceImpl<MediaShareLinkMapper, MediaShareLink> implements MediaShareService {

	private final MediaAlbumMapper mediaAlbumMapper;

	private final MediaAlbumItemMapper mediaAlbumItemMapper;

	private final MediaShareAccessLogMapper mediaShareAccessLogMapper;

	private final MediaProperties mediaProperties;

	@Override
	public MediaShareLink createShare(ShareCreateDTO dto, Long userId) {
		MediaAlbum album = mediaAlbumMapper.selectById(dto.getAlbumId());
		if (album == null || !userId.equals(album.getOwnerId())) {
			throw new IllegalArgumentException("相册不存在或无权限");
		}
		MediaShareLink share = new MediaShareLink();
		share.setShareToken(IdUtil.fastSimpleUUID());
		share.setOwnerId(userId);
		share.setAlbumId(dto.getAlbumId());
		share.setCode(StrUtil.blankToDefault(dto.getCode(), null));
		int expireHours = dto.getExpireHours() == null ? mediaProperties.getShare().getDefaultExpireHours()
				: dto.getExpireHours();
		int maxViewCount = dto.getMaxViewCount() == null ? mediaProperties.getShare().getDefaultMaxViewCount()
				: dto.getMaxViewCount();
		share.setExpireAt(LocalDateTime.now().plusHours(Math.max(expireHours, 1)));
		share.setMaxViewCount(Math.max(maxViewCount, 1));
		share.setCurrentViewCount(0);
		share.setStatus(MediaConstants.SHARE_STATUS_ACTIVE);
		this.save(share);
		return share;
	}

	@Override
	public boolean disableShare(Long shareId, Long userId) {
		MediaShareLink share = this.getById(shareId);
		if (share == null || !userId.equals(share.getOwnerId())) {
			return false;
		}
		share.setStatus(MediaConstants.SHARE_STATUS_DISABLED);
		return this.updateById(share);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ShareDetailVO detail(String token) {
		MediaShareLink share = this.getOne(Wrappers.<MediaShareLink>lambdaQuery()
			.eq(MediaShareLink::getShareToken, token)
			.last("limit 1"), false);
		if (share == null) {
			throw new IllegalArgumentException("分享不存在");
		}
		if (!MediaConstants.SHARE_STATUS_ACTIVE.equals(share.getStatus())) {
			throw new IllegalArgumentException("分享已关闭");
		}
		if (share.getExpireAt() != null && share.getExpireAt().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("分享已过期");
		}
		if (share.getCurrentViewCount() != null && share.getMaxViewCount() != null
				&& share.getCurrentViewCount() >= share.getMaxViewCount()) {
			throw new IllegalArgumentException("分享访问次数已达上限");
		}

		List<MediaAlbumItem> items = mediaAlbumItemMapper.selectList(Wrappers.<MediaAlbumItem>lambdaQuery()
			.eq(MediaAlbumItem::getAlbumId, share.getAlbumId())
			.orderByAsc(MediaAlbumItem::getSortNo));

		share.setCurrentViewCount(share.getCurrentViewCount() + 1);
		this.updateById(share);

		MediaShareAccessLog accessLog = new MediaShareAccessLog();
		accessLog.setShareId(share.getId());
		accessLog.setAccessTime(LocalDateTime.now());
		mediaShareAccessLogMapper.insert(accessLog);

		ShareDetailVO vo = new ShareDetailVO();
		vo.setShare(share);
		if (items == null || items.isEmpty()) {
			vo.setFileIds(Collections.emptyList());
		}
		else {
			vo.setFileIds(items.stream().map(MediaAlbumItem::getFileId).toList());
		}
		return vo;
	}

	@Override
	public boolean verify(String token, ShareVerifyDTO dto) {
		MediaShareLink share = this.getOne(Wrappers.<MediaShareLink>lambdaQuery()
			.eq(MediaShareLink::getShareToken, token)
			.last("limit 1"), false);
		if (share == null) {
			return false;
		}
		if (StrUtil.isBlank(share.getCode())) {
			return true;
		}
		return StrUtil.equals(share.getCode(), dto.getCode());
	}

}
