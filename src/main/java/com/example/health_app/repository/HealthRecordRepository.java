package com.example.health_app.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
	
	/**
	 * 当日記録を検索
	 */
	List<HealthRecord> findByRecordDate(LocalDate recordDate);
	
	@Query("SELECT COUNT(DISTINCT r.user.id) FROM HealthRecord r WHERE r.recordDate = :date")
	long countDistinctUsersByRecordDate(@Param("date") LocalDate date);

}
