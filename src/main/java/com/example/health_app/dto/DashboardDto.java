package com.example.health_app.dto;

/**
 * 管理者ダッシュボード用 DTO
 */
public class DashboardDto {

    private long totalUsers;
    private long adminUsers;
    private long totalRecords;
    private long todayActiveUsers;

    public DashboardDto(long totalUsers, long adminUsers, long totalRecords, long todayActiveUsers) {
        this.totalUsers = totalUsers;
        this.adminUsers = adminUsers;
        this.totalRecords = totalRecords;
        this.todayActiveUsers = todayActiveUsers;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getAdminUsers() {
        return adminUsers;
    }

    public void setAdminUsers(long adminUsers) {
        this.adminUsers = adminUsers;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public long getTodayActiveUsers() {
        return todayActiveUsers;
    }

    public void setTodayActiveUsers(long todayActiveUsers) {
        this.todayActiveUsers = todayActiveUsers;
    }
}
