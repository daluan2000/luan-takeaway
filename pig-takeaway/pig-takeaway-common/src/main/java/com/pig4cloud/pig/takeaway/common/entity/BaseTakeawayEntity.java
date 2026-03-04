package com.pig4cloud.pig.takeaway.common.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class BaseTakeawayEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long id;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	private String delFlag;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	public LocalDateTime getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(LocalDateTime updateTime) {
		this.updateTime = updateTime;
	}

	public String getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(String delFlag) {
		this.delFlag = delFlag;
	}

}
