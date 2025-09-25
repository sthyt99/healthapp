package com.example.health_app.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.health_app.constant.AppMessage;
import com.example.health_app.dto.HealthRecordDto;
import com.example.health_app.dto.HealthRecordRequestDto;
import com.example.health_app.entity.HealthRecord;
import com.example.health_app.service.HealthRecordService;

@RestController
@RequestMapping("/api/admin/records")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRecordController {

    private final HealthRecordService recordService;

    public AdminRecordController(HealthRecordService recordService) {
        this.recordService = recordService;
    }

    /** 任意ユーザーの記録を作成 */
    @PostMapping("/{userId}")
    public ResponseEntity<HealthRecordDto> createRecordForUser(
            @PathVariable Long userId, @RequestBody HealthRecordRequestDto request) {
        HealthRecord saved = recordService.createRecord(userId, request);
        return ResponseEntity.ok(toDto(saved));
    }

    /** 任意ユーザーの記録取得 */
    @GetMapping("/{userId}")
    public ResponseEntity<List<HealthRecordDto>> getRecordsByUser(@PathVariable Long userId) {
        List<HealthRecord> records = recordService.getRecordsByUserId(userId);
        return ResponseEntity.ok(toDtoList(records));
    }

    /** 任意ユーザーの記録削除（無条件で削除可） */
    @DeleteMapping("/{recordId}")
    public ResponseEntity<?> deleteRecord(@PathVariable Long recordId) {
        recordService.deleteRecordForAdmin(recordId);
        return ResponseEntity.ok(AppMessage.RECORD_DELETE);
    }

    // ---- DTO 変換 ----
    private HealthRecordDto toDto(HealthRecord r) {
        return new HealthRecordDto(
                r.getId(),
                r.getRecordDate(),
                r.getWeight(),
                r.getSystolic(),
                r.getDiastolic(),
                r.getBodyTemperature(),
                r.getSteps()
        );
    }

    private List<HealthRecordDto> toDtoList(List<HealthRecord> records) {
        return records.stream().map(this::toDto).collect(Collectors.toList());
    }
}
