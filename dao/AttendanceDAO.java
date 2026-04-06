package dao;

import util.DatabaseConnection;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AttendanceDAO {
    
    public boolean markAttendance(int studentId, int courseId, Date date, String status, int markedBy, String remarks) {
        String query = "INSERT INTO attendance (student_id, course_id, attendance_date, status, marked_by, remarks) " +
                       "VALUES (?, ?, ?, ?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE status = ?, remarks = ?, marked_by = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            pstmt.setDate(3, date);
            pstmt.setString(4, status);
            pstmt.setInt(5, markedBy);
            pstmt.setString(6, remarks);
            pstmt.setString(7, status);
            pstmt.setString(8, remarks);
            pstmt.setInt(9, markedBy);
            
            int result = pstmt.executeUpdate();
            return result > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Map<String, Integer> getAttendanceStats(int studentId, int courseId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Present", 0);
        stats.put("Absent", 0);
        stats.put("Late", 0);
        stats.put("Total", 0);
        
        String query = "SELECT status, COUNT(*) as count FROM attendance " +
                       "WHERE student_id = ? AND course_id = ? GROUP BY status";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                stats.put(status, count);
                stats.put("Total", stats.get("Total") + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
    
    public String getAttendanceStatus(int studentId, int courseId, Date date) {
        String query = "SELECT status FROM attendance WHERE student_id = ? AND course_id = ? AND attendance_date = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            pstmt.setDate(3, date);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public double getAttendancePercentage(int studentId, int courseId) {
        String query = "SELECT " +
                       "COUNT(*) as total, " +
                       "SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) as present " +
                       "FROM attendance WHERE student_id = ? AND course_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int total = rs.getInt("total");
                int present = rs.getInt("present");
                
                if (total > 0) {
                    return (present * 100.0) / total;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    public Map<Integer, Map<String, Integer>> getCourseAttendanceStats(int courseId, Date date) {
        Map<Integer, Map<String, Integer>> attendanceData = new HashMap<>();
        
        String query = "SELECT student_id, status FROM attendance WHERE course_id = ? AND attendance_date = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, courseId);
            pstmt.setDate(2, date);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int studentId = rs.getInt("student_id");
                String status = rs.getString("status");
                
                Map<String, Integer> studentData = new HashMap<>();
                studentData.put(status, 1);
                attendanceData.put(studentId, studentData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendanceData;
    }
}