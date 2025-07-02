package com.example.health_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.health_app.entity.HealthRecord;

/**
 * 健康記録用リポジトリ
 */
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {

	/**
	 * 指定ユーザーの記録一覧検索
	 */
	List<HealthRecord> findByUserId(Long userId);

	/**
	 * 日付の新しい順に記録一覧検索
	 */
	List<HealthRecord> findByUserIdOrderByRecordDateDesc(Long userId);
}
