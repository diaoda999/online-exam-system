package com.exam.service;

import com.exam.model.vo.course.CourseStudentVO;

import java.util.List;

/**
 * 课程学生关联服务接口
 */
public interface CourseStudentService {

    /**
     * 邀请学生加入课程（教师/管理员）
     */
    void inviteStudents(Long courseId, List<Long> studentIds, Long inviterId);

    /**
     * 学生同意加入课程
     */
    void acceptInvitation(Long id, Long studentId);

    /**
     * 学生拒绝加入课程
     */
    void rejectInvitation(Long id, Long studentId);

    /**
     * 移除课程中的学生（教师/管理员）
     */
    void removeStudent(Long courseId, Long studentId);

    /**
     * 获取课程的学生列表
     */
    List<CourseStudentVO> getCourseStudents(Long courseId);

    /**
     * 获取学生的课程邀请列表
     */
    List<CourseStudentVO> getStudentInvitations(Long studentId);

    /**
     * 获取学生的已加入课程列表
     */
    List<CourseStudentVO> getStudentCourses(Long studentId);
}
