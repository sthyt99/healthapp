package com.example.health_app.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.health_app.constant.AppMessage;
import com.example.health_app.dto.HealthRecordRequestDto;
import com.example.health_app.entity.HealthRecord;
import com.example.health_app.entity.Role;
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
	 * @param dto 健康記録情報DTO
	 * @return 健康記録を保存する
	 */
	public HealthRecord createRecord(Long userId, HealthRecordRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(AppMessage.USER_NOT_FOUND));

        HealthRecord entity = new HealthRecord();
        entity.setUser(user);
        entity.setRecordDate(dto.getRecordDate());
        entity.setWeight(dto.getWeight());
        entity.setSystolic(dto.getSystolic());
        entity.setDiastolic(dto.getDiastolic());
        entity.setBodyTemperature(dto.getBodyTemperature());
        entity.setSteps(dto.getSteps());

        return recordRepository.save(entity);
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
	 * @param requester 管理Dto
	 */
	public void deleteRecordAs(Long recordId, User requester) {
	    HealthRecord rec = recordRepository.findById(recordId)
	        .orElseThrow(() -> new EntityNotFoundException(AppMessage.RECORD_NOT_FOUND));
	    boolean isOwner = rec.getUser().getId().equals(requester.getId());
	    boolean isAdmin = requester.getRoles().contains(Role.ROLE_ADMIN);
	    if (!isOwner && !isAdmin) {
	        throw new AccessDeniedException(AppMessage.DELETE_NO_ROLE);
	    }
	    recordRepository.delete(rec);
	}

	/**
	 * 健康記録削除処理(管理者用)
	 * 
	 * @param recordId 記録ID
	 */
	public void deleteRecordForAdmin(Long recordId) {

		// 健康記録が存在しない場合
		if (!recordRepository.existsById(recordId)) {

			throw new EntityNotFoundException(AppMessage.RECORD_NOT_FOUND);
		}

		// 健康記録を削除する
		recordRepository.deleteById(recordId);
	}
}
