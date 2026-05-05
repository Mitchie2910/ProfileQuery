package com.hng.nameprocessing.services;

import com.hng.nameprocessing.dtos.UploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

    @Service
    @RequiredArgsConstructor
    public class UploadService {

        private final CSVProcessingService worker;

        public CompletableFuture<UploadResult> handleUpload(MultipartFile file) throws IOException {

            Path tempFile = Files.createTempFile("upload-", ".csv");
            file.transferTo(tempFile);

            return worker.process(tempFile);

        }
    }

