package com.welab.alertsystem.controller;

import com.welab.alertsystem.service.UploadService;
import com.welab.alertsystem.ulit.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/")
public class UploadController {

    UploadService uploadService;

    @Autowired
    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('UPLOADER')")
    public JsonResult uploadCsvFile(@RequestParam("file") MultipartFile file){
        uploadService.uploadFile(file);
        JsonResult jsonResult = new JsonResult();
        jsonResult.setData("Done");
        return jsonResult;
    }
}
