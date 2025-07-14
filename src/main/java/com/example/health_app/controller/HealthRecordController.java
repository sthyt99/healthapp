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
import com.example.health_app.entity.HealthRecord;
import com.example.health_app.entity.User;
import com.example.health_app.service.HealthRecordService;
import com.example.health_app.service.UserService;

import jakarta.persistence.EntityNotFoundException;

/**
 * 健康記録のコントローラークラス
 */
@RestController
@RequestMapping("/api/records")
public class HealthRecordController {

	private HealthRecordService recordService;
	private UserService userService;

	public HealthRecordController(HealthRecordService recordService, UserService userService) {
		this.recordService = recordService;
		this.userService = userService;
	}

	/**
	 * 自分の健康記録を作成（認証ユーザー）
	 */
	@PostMapping
	public ResponseEntity<?> createRecord(@RequestBody HealthRecord record, Principal principal) {
		try {

			User user = userService.findByUsernameOrThrow(principal.getName());
			HealthRecord saved = recordService.createRecord(user.getId(), record);

			// DTO化
			return ResponseEntity.ok(toDto(saved));

		} catch (EntityNotFoundException e) {

			// "ユーザーが見つかりません"
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AppMessage.USER_NOT_FOUND);
		}
	}

	/**
	 * 指定ユーザーの健康記録を作成（管理者向け）
	 */
	@PostMapping("/{userId}")
	public ResponseEntity<?> createRecord(@PathVariable Long userId, @RequestBody HealthRecord record) {

		HealthRecord saved = recordService.createRecord(userId, record);

		// DTO化
		return ResponseEntity.ok(toDto(saved));
	}

	/**
	 * 自分の健康記録を取得（認証ユーザー）
	 */
	@GetMapping
	public ResponseEntity<?> getRecords(Principal principal) {
		try {

			User user = userService.findByUsernameOrThrow(principal.getName());
			List<HealthRecord> records = recordService.getRecordsByUserId(user.getId());

			return ResponseEntity.ok(toDtoList(records));

		} catch (EntityNotFoundException e) {

			// "ユーザーが見つかりません"
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AppMessage.USER_NOT_FOUND);
		}
	}

	/**
	 * 指定ユーザーの健康記録を取得（管理者向け）
	 */
	@GetMapping("/{userId}")
	public ResponseEntity<?> getRecords(@PathVariable Long userId) {

		List<HealthRecord> records = recordService.getRecordsByUserId(userId);

		return ResponseEntity.ok(toDtoList(records));
	}

	/**
	 * 健康記録を消去
	 */
	@DeleteMapping("/{recordId}")
	public ResponseEntity<?> deleteRecord(@PathVariable Long recordId) {

		try {

			recordService.deleteRecord(recordId);

			// "記録を消去しました"
			return ResponseEntity.ok(AppMessage.RECORD_DELETE);

			// 健康記録が存在しない場合
		} catch (EntityNotFoundException e) {

			// "記録が存在しません"
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AppMessage.RECORD_NOT_FOUND);
		}
	}

	/**
	 * HealthRecordをDTOに変換
	 */
	private HealthRecordDto toDto(HealthRecord record) {
		return new HealthRecordDto(
				record.getId(),
				record.getRecordDate(),
				record.getWeight(),
				record.getSystolic(),
				record.getDiastolic(),
				record.getBodyTemperature(),
				record.getSteps());
	}

	/**
	 * List<HealthRecordDto>に変換
	 */
	private List<HealthRecordDto> toDtoList(List<HealthRecord> records) {
		return records.stream()
				.map(this::toDto)
				.collect(Collectors.toList());
	}
}
