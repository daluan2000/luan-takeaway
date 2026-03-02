package com.pig4cloud.pig.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.media.api.entity.MediaShareLink;
import com.pig4cloud.pig.media.model.dto.ShareCreateDTO;
import com.pig4cloud.pig.media.model.dto.ShareVerifyDTO;
import com.pig4cloud.pig.media.model.vo.ShareDetailVO;

public interface MediaShareService extends IService<MediaShareLink> {

	MediaShareLink createShare(ShareCreateDTO dto, Long userId);

	boolean disableShare(Long shareId, Long userId);

	ShareDetailVO detail(String token);

	boolean verify(String token, ShareVerifyDTO dto);

}
