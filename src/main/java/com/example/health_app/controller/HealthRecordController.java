package com.example.health_app.controller;

import java.util.List;

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
import com.example.health_app.entity.HealthRecord;
import com.example.health_app.service.HealthRecordService;

import jakarta.persistence.EntityNotFoundException;

/**
 * 健康記録のコントローラークラス
 */
@RestController
@RequestMapping("/api/records")
public class HealthRecordController {

	private HealthRecordService recordService;
	
	public HealthRecordController(HealthRecordService recordService) {
		this.recordService = recordService;
	}
	
	/**
	 * 健康記録を作成
	 */
	@PostMapping("/{userId}")
	public ResponseEntity<?> createRecord(@PathVariable Long userId, @RequestBody HealthRecord record) {
		
		HealthRecord saved = recordService.createRecord(userId, record);
		
		return ResponseEntity.ok(saved);
	}
	
	/**
	 * 健康記録一覧を取得
	 */
	@GetMapping("/{userId}")
	public ResponseEntity<?> getRecords(@PathVariable Long userId) {
		
		List<HealthRecord> records = recordService.getRecordsByUserId(userId);
		
		return ResponseEntity.ok(records);
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
}
