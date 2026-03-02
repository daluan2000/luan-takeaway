package com.pig4cloud.pig.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.media.api.entity.MediaFile;
import com.pig4cloud.pig.media.model.dto.FilePresignDTO;
import com.pig4cloud.pig.media.model.dto.UploadConfirmDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface MediaFileService extends IService<MediaFile> {

	Map<String, Object> upload(MultipartFile file, Long userId);

	Map<String, Object> presign(FilePresignDTO dto, Long userId);

	MediaFile confirmUpload(UploadConfirmDTO dto, Long userId);

	boolean deleteFile(Long fileId, Long userId);

}
