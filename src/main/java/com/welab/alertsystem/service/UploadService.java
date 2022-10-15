package com.welab.alertsystem.service;


import com.welab.alertsystem.model.UploadAlert;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UploadService {
    public ResponseEntity<List<UploadAlert>> uploadFile(MultipartFile file);
}
