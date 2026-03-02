package com.pig4cloud.pig.media.model.vo;

import com.pig4cloud.pig.media.api.entity.MediaShareLink;
import lombok.Data;

import java.util.List;

@Data
public class ShareDetailVO {

	private MediaShareLink share;

	private List<Long> fileIds;

}
