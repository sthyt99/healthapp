package com.example.health_app.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.example.health_app.entity.User;
import com.example.health_app.service.HealthRecordService;
import com.example.health_app.service.UserService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/records")
public class UserRecordController {

	private final HealthRecordService recordService;
	private final UserService userService;

	public UserRecordController(HealthRecordService recordService, UserService userService) {
		this.recordService = recordService;
		this.userService = userService;
	}

	/** 自分の記録を作成 */
	@PostMapping("/me")
	public ResponseEntity<?> createMyRecord(@RequestBody HealthRecordRequestDto request, Principal principal) {
		try {
			User me = userService.findByUsernameOrThrow(principal.getName());
			HealthRecord saved = recordService.createRecord(me.getId(), request);
			return ResponseEntity.ok(toDto(saved));
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
	}

	/** 自分の記録を取得 */
	@GetMapping("/me")
	public ResponseEntity<?> getMyRecords(Principal principal) {
		try {
			User me = userService.findByUsernameOrThrow(principal.getName());
			List<HealthRecord> records = recordService.getRecordsByUserId(me.getId());
			return ResponseEntity.ok(toDtoList(records));
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
	}

	/** 自分の記録を削除（所有者 or 管理者のみ） */
	@DeleteMapping("/{recordId}")
	public ResponseEntity<?> deleteMyRecord(@PathVariable Long recordId, Principal principal) {
		try {
			User me = userService.findByUsernameOrThrow(principal.getName());
			recordService.deleteRecordAs(recordId, me); // 所有者チェック込み
			return ResponseEntity.ok(AppMessage.RECORD_DELETE);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (org.springframework.security.access.AccessDeniedException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AppMessage.DELETE_NO_ROLE);
		}
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
				r.getSteps());
	}

	private List<HealthRecordDto> toDtoList(List<HealthRecord> records) {
		return records.stream().map(this::toDto).collect(Collectors.toList());
	}
}
