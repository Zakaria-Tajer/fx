package com.warehouse.warehouse.controllers;

import com.warehouse.warehouse.Dto.ResultDto;
import com.warehouse.warehouse.services.DealService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/csv/import")
@RequiredArgsConstructor
public class DealController {


    private final DealService dealService;

    @PostMapping
    public ResultDto importDeals(@RequestParam("file") MultipartFile file) throws IOException {
        return dealService.importCsv(file);

    }
}