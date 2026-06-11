package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ResultCode;
import com.exam.model.entity.*;
import com.exam.model.mapper.*;
import com.exam.model.vo.course.ClassCourseVO;
import com.exam.model.vo.course.ClassStudentVO;
import com.exam.service.ClassStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassStudentServiceImpl implements ClassStudentService {

    private final ClassStudentMapper classStudentMapper;
    private final ClassCourseMapper classCourseMapper;
    private final ClassMapper classMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void inviteStudents(Long classId, List<Long> studentIds, Long inviterId) {
        ClassEntity cls = classMapper.selectById(classId);
        if (cls == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班级不存在");
        }

        for (Long studentId : studentIds) {
            User student = userMapper.selectById(studentId);
            if (student == null) continue;

            ClassStudent existing = classStudentMapper.selectOne(
                    new LambdaQueryWrapper<ClassStudent>()
                            .eq(ClassStudent::getClassId, classId)
                            .eq(ClassStudent::getStudentId, studentId)
            );

            if (existing != null) {
                if ("REJECTED".equals(existing.getStatus())) {
                    existing.setStatus("PENDING");
                    existing.setInviterId(inviterId);
                    classStudentMapper.updateById(existing);
                }
                continue;
            }

            ClassStudent cs = ClassStudent.builder()
                    .classId(classId)
                    .studentId(studentId)
                    .status("PENDING")
                    .inviterId(inviterId)
                    .build();
            classStudentMapper.insert(cs);
        }
        log.info("邀请学生加入班级: classId={}, studentIds={}", classId, studentIds);
    }

    @Override
    public void acceptInvitation(Long id, Long studentId) {
        ClassStudent cs = classStudentMapper.selectById(id);
        if (cs == null || !cs.getStudentId().equals(studentId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "邀请记录不存在");
        }
        if (!"PENDING".equals(cs.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前状态无法操作");
        }
        cs.setStatus("ACCEPTED");
        classStudentMapper.updateById(cs);
        log.info("学生同意加入班级: studentId={}, classId={}", studentId, cs.getClassId());
    }

    @Override
    public void rejectInvitation(Long id, Long studentId) {
        ClassStudent cs = classStudentMapper.selectById(id);
        if (cs == null || !cs.getStudentId().equals(studentId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "邀请记录不存在");
        }
        if (!"PENDING".equals(cs.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前状态无法操作");
        }
        cs.setStatus("REJECTED");
        classStudentMapper.updateById(cs);
        log.info("学生拒绝加入班级: studentId={}, classId={}", studentId, cs.getClassId());
    }

    @Override
    public void removeStudent(Long classId, Long studentId) {
        ClassStudent cs = classStudentMapper.selectOne(
                new LambdaQueryWrapper<ClassStudent>()
                        .eq(ClassStudent::getClassId, classId)
                        .eq(ClassStudent::getStudentId, studentId)
        );
        if (cs != null) {
            classStudentMapper.deleteById(cs.getId());
        }
    }

    @Override
    public List<ClassStudentVO> getClassStudents(Long classId) {
        List<ClassStudent> list = classStudentMapper.selectList(
                new LambdaQueryWrapper<ClassStudent>()
                        .eq(ClassStudent::getClassId, classId)
                        .orderByDesc(ClassStudent::getCreateTime)
        );
        return convertStudentToVOList(list);
    }

    @Override
    public List<ClassStudentVO> getStudentInvitations(Long studentId) {
        List<ClassStudent> list = classStudentMapper.selectList(
                new LambdaQueryWrapper<ClassStudent>()
                        .eq(ClassStudent::getStudentId, studentId)
                        .orderByDesc(ClassStudent::getCreateTime)
        );
        return convertStudentToVOList(list);
    }

    @Override
    public List<ClassStudentVO> getStudentClasses(Long studentId) {
        List<ClassStudent> list = classStudentMapper.selectList(
                new LambdaQueryWrapper<ClassStudent>()
                        .eq(ClassStudent::getStudentId, studentId)
                        .eq(ClassStudent::getStatus, "ACCEPTED")
                        .orderByDesc(ClassStudent::getCreateTime)
        );
        return convertStudentToVOList(list);
    }

    private List<ClassStudentVO> convertStudentToVOList(List<ClassStudent> list) {
        List<ClassStudentVO> voList = new ArrayList<>();
        for (ClassStudent cs : list) {
            ClassEntity cls = classMapper.selectById(cs.getClassId());
            User student = userMapper.selectById(cs.getStudentId());
            User inviter = cs.getInviterId() != null ? userMapper.selectById(cs.getInviterId()) : null;

            voList.add(ClassStudentVO.builder()
                    .id(cs.getId())
                    .classId(cs.getClassId())
                    .className(cls != null ? cls.getClassName() : "未知班级")
                    .studentId(cs.getStudentId())
                    .studentName(student != null ? student.getRealName() : "未知学生")
                    .studentUsername(student != null ? student.getUsername() : "")
                    .status(cs.getStatus())
                    .inviterId(cs.getInviterId())
                    .inviterName(inviter != null ? inviter.getRealName() : "")
                    .createTime(cs.getCreateTime())
                    .build());
        }
        return voList;
    }

    // ===== 班级需修读课程 =====

    @Override
    @Transactional
    public void addCourseToClass(Long classId, Long courseId, Long adderId) {
        ClassEntity cls = classMapper.selectById(classId);
        if (cls == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班级不存在");
        }
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "课程不存在");
        }

        // 检查是否已存在
        Long exists = classCourseMapper.selectCount(
                new LambdaQueryWrapper<ClassCourse>()
                        .eq(ClassCourse::getClassId, classId)
                        .eq(ClassCourse::getCourseId, courseId)
        );
        if (exists > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该课程已在班级需修读列表中");
        }

        ClassCourse cc = ClassCourse.builder()
                .classId(classId)
                .courseId(courseId)
                .adderId(adderId)
                .build();
        classCourseMapper.insert(cc);
        log.info("添加课程到班级需修读: classId={}, courseId={}", classId, courseId);
    }

    @Override
    @Transactional
    public void removeCourseFromClass(Long classId, Long courseId) {
        classCourseMapper.delete(
                new LambdaQueryWrapper<ClassCourse>()
                        .eq(ClassCourse::getClassId, classId)
                        .eq(ClassCourse::getCourseId, courseId)
        );
        log.info("从班级需修读移除课程: classId={}, courseId={}", classId, courseId);
    }

    @Override
    public List<ClassCourseVO> getClassCourses(Long classId) {
        List<ClassCourse> list = classCourseMapper.selectList(
                new LambdaQueryWrapper<ClassCourse>()
                        .eq(ClassCourse::getClassId, classId)
                        .orderByDesc(ClassCourse::getCreateTime)
        );
        return convertCourseToVOList(list);
    }

    @Override
    public List<ClassCourseVO> getAllClassCourses() {
        List<ClassCourse> list = classCourseMapper.selectList(
                new LambdaQueryWrapper<ClassCourse>()
                        .orderByDesc(ClassCourse::getCreateTime)
        );
        return convertCourseToVOList(list);
    }

    private List<ClassCourseVO> convertCourseToVOList(List<ClassCourse> list) {
        List<ClassCourseVO> voList = new ArrayList<>();
        for (ClassCourse cc : list) {
            ClassEntity cls = classMapper.selectById(cc.getClassId());
            Course course = courseMapper.selectById(cc.getCourseId());
            User adder = cc.getAdderId() != null ? userMapper.selectById(cc.getAdderId()) : null;

            voList.add(ClassCourseVO.builder()
                    .id(cc.getId())
                    .classId(cc.getClassId())
                    .className(cls != null ? cls.getClassName() : "未知班级")
                    .courseId(cc.getCourseId())
                    .courseName(course != null ? course.getCourseName() : "未知课程")
                    .courseCode(course != null ? course.getCourseCode() : "")
                    .adderId(cc.getAdderId())
                    .adderName(adder != null ? adder.getRealName() : "")
                    .createTime(cc.getCreateTime())
                    .build());
        }
        return voList;
    }
}
