package com.welab.alertsystem.service.impl;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.welab.alertsystem.model.UploadAlert;
import com.welab.alertsystem.DAO.AlertDao;
import com.welab.alertsystem.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Service
public class UploadServiceImpl implements UploadService {

    AlertDao alertDao;

    @Autowired
    public UploadServiceImpl(AlertDao alertDao) {
        this.alertDao = alertDao;
    }

    @Override
    public ResponseEntity<List<UploadAlert>> uploadFile(MultipartFile file) {
        List<UploadAlert> employees = convertToModel(file, UploadAlert.class);
        alertDao.insertAlertBatch(employees);
        return null;
    }

    public static <T, is> List<T> convertToModel(MultipartFile file, Class<T> responseType) {
        List<T> models;
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<?> csvToBean = new CsvToBeanBuilder(reader)
                    .withType(responseType)
                    .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            models = (List<T>) csvToBean.parse();
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getCause().getMessage());
        }

        return models;
    }
}
