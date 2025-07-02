package com.example.health_app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.health_app.constant.AppMessage;
import com.example.health_app.entity.HealthRecord;
import com.example.health_app.entity.User;
import com.example.health_app.repository.HealthRecordRepository;
import com.example.health_app.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 健康記録のサービス層
 */
@Service
public class HealthRecordService {

	private final HealthRecordRepository recordRepository;
	private final UserRepository userRepository;

	public HealthRecordService(HealthRecordRepository recordRepository, UserRepository userRepository) {
		this.recordRepository = recordRepository;
		this.userRepository = userRepository;
	}

	/**
	 * 健康記録作成処理
	 * 
	 * @param userId ユーザーID
	 * @param record 健康記録情報
	 * @return 健康記録を保存する
	 */
	public HealthRecord createRecord(Long userId, HealthRecord record) {

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException(AppMessage.USER_NOT_FOUND));

		// ユーザー情報を設定する
		record.setUser(user);

		return recordRepository.save(record);
	}

	/**
	 * ユーザーID検索処理
	 * 
	 * @param userId ユーザーID
	 * @return ユーザーIDの新しい順に健康記録一覧を取得する
	 */
	public List<HealthRecord> getRecordsByUserId(Long userId) {

		return recordRepository.findByUserIdOrderByRecordDateDesc(userId);
	}

	/**
	 * 健康記録削除処理
	 * 
	 * @param recordId 記録ID
	 */
	public void deleteRecord(Long recordId) {

		// 健康記録が存在しない場合
		if (!recordRepository.existsById(recordId)) {

			throw new EntityNotFoundException(AppMessage.RECORD_NOT_FOUND);
		}

		// 健康記録を削除する
		recordRepository.deleteById(recordId);
	}
}
