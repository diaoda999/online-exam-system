package com.exam.service;

import com.exam.model.vo.course.ClassCourseVO;
import com.exam.model.vo.course.ClassStudentVO;

import java.util.List;

/**
 * 班级学生关联服务接口
 */
public interface ClassStudentService {

    /** 邀请学生加入班级（教师/管理员） */
    void inviteStudents(Long classId, List<Long> studentIds, Long inviterId);

    /** 学生同意加入班级 */
    void acceptInvitation(Long id, Long studentId);

    /** 学生拒绝加入班级 */
    void rejectInvitation(Long id, Long studentId);

    /** 移除班级中的学生（教师/管理员） */
    void removeStudent(Long classId, Long studentId);

    /** 获取班级的学生列表 */
    List<ClassStudentVO> getClassStudents(Long classId);

    /** 获取学生的班级邀请列表 */
    List<ClassStudentVO> getStudentInvitations(Long studentId);

    /** 获取学生的已加入班级列表 */
    List<ClassStudentVO> getStudentClasses(Long studentId);

    /** 添加课程到班级需修读 */
    void addCourseToClass(Long classId, Long courseId, Long adderId);

    /** 从班级需修读中移除课程 */
    void removeCourseFromClass(Long classId, Long courseId);

    /** 获取班级的需修读课程列表 */
    List<ClassCourseVO> getClassCourses(Long classId);

    /** 获取所有班级的需修读课程（学生端查看） */
    List<ClassCourseVO> getAllClassCourses();
}
