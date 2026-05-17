package com.example.test.service;

import com.example.test.config.AppProperties;
import com.example.test.domain.TableEntity;
import com.example.test.dto.TableDtos.TableResponse;
import com.example.test.exception.ApiException;
import com.example.test.repository.TableRepository;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TableService {
    private final TableRepository tableRepository;
    private final QRCodeService qrCodeService;
    private final AppProperties properties;

    public TableService(TableRepository tableRepository, QRCodeService qrCodeService, AppProperties properties) {
        this.tableRepository = tableRepository;
        this.qrCodeService = qrCodeService;
        this.properties = properties;
    }

    @Transactional
    public TableResponse createTable(int tableNumber) {
        return createTable(tableNumber, null);
    }

    @Transactional
    public TableResponse createTable(int tableNumber, String baseUrl) {
        if (tableRepository.existsByTableNumber(tableNumber)) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 존재하는 테이블 번호입니다.");
        }
        String tableUrl = resolveBaseUrl(baseUrl) + "/table/" + tableNumber;
        String fileName = "table-" + tableNumber + ".png";
        saveQrPng(fileName, tableUrl);
        TableEntity table = new TableEntity(tableNumber, qrCodeUrl(fileName));
        return TableResponse.from(tableRepository.save(table));
    }

    @Transactional(readOnly = true)
    public TableResponse getByTableNumber(int tableNumber) {
        return tableRepository.findByTableNumber(tableNumber)
                .map(TableResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "테이블을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<TableResponse> getTables() {
        return tableRepository.findAll().stream().map(TableResponse::from).toList();
    }

    @Transactional
    public List<TableResponse> regenerateAllQrCodes(String baseUrl) {
        List<TableEntity> tables = tableRepository.findAll();
        String resolvedBaseUrl = resolveBaseUrl(baseUrl);
        for (TableEntity table : tables) {
            String fileName = "table-" + table.getTableNumber() + ".png";
            String tableUrl = resolvedBaseUrl + "/table/" + table.getTableNumber();
            saveQrPng(fileName, tableUrl);
            table.updateQrCodeUrl(qrCodeUrl(fileName));
        }
        return tables.stream().map(TableResponse::from).toList();
    }

    private String qrCodeUrl(String fileName) {
        return "/qr/" + fileName + "?v=" + System.currentTimeMillis();
    }

    private String resolveBaseUrl(String baseUrl) {
        String candidate = baseUrl == null || baseUrl.isBlank() ? properties.frontendBaseUrl() : baseUrl.trim();
        try {
            URI uri = URI.create(candidate);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException();
            }
            if (uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
            URI normalized = new URI(
                    scheme.toLowerCase(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort(),
                    null,
                    null,
                    null
            );
            return normalized.toString();
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "QR 기준 주소가 올바르지 않습니다.");
        }
    }

    private void saveQrPng(String fileName, String tableUrl) {
        try {
            Path dir = Path.of(properties.qrOutputDir());
            Files.createDirectories(dir);
            Files.write(dir.resolve(fileName), qrCodeService.generatePng(tableUrl));
        } catch (Exception ex) {
            throw new IllegalStateException("QR PNG 저장에 실패했습니다.");
        }
    }
}
