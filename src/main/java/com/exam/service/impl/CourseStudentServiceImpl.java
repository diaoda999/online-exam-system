package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ResultCode;
import com.exam.model.entity.Course;
import com.exam.model.entity.CourseStudent;
import com.exam.model.entity.User;
import com.exam.model.mapper.CourseMapper;
import com.exam.model.mapper.CourseStudentMapper;
import com.exam.model.mapper.UserMapper;
import com.exam.model.vo.course.CourseStudentVO;
import com.exam.service.CourseStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程学生关联服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseStudentServiceImpl implements CourseStudentService {

    private final CourseStudentMapper courseStudentMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void inviteStudents(Long courseId, List<Long> studentIds, Long inviterId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "课程不存在");
        }

        for (Long studentId : studentIds) {
            User student = userMapper.selectById(studentId);
            if (student == null) {
                continue;
            }

            // 检查是否已有记录
            CourseStudent existing = courseStudentMapper.selectOne(
                    new LambdaQueryWrapper<CourseStudent>()
                            .eq(CourseStudent::getCourseId, courseId)
                            .eq(CourseStudent::getStudentId, studentId)
            );

            if (existing != null) {
                if ("REJECTED".equals(existing.getStatus())) {
                    // 之前拒绝过，重新邀请
                    existing.setStatus("PENDING");
                    existing.setInviterId(inviterId);
                    courseStudentMapper.updateById(existing);
                }
                // PENDING 或 ACCEPTED 则跳过
                continue;
            }

            CourseStudent cs = CourseStudent.builder()
                    .courseId(courseId)
                    .studentId(studentId)
                    .status("PENDING")
                    .inviterId(inviterId)
                    .build();
            courseStudentMapper.insert(cs);
        }

        log.info("邀请学生加入课程: courseId={}, studentIds={}", courseId, studentIds);
    }

    @Override
    public void acceptInvitation(Long id, Long studentId) {
        CourseStudent cs = courseStudentMapper.selectById(id);
        if (cs == null || !cs.getStudentId().equals(studentId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "邀请记录不存在");
        }
        if (!"PENDING".equals(cs.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前状态无法操作");
        }
        cs.setStatus("ACCEPTED");
        courseStudentMapper.updateById(cs);
        log.info("学生同意加入课程: studentId={}, courseId={}", studentId, cs.getCourseId());
    }

    @Override
    public void rejectInvitation(Long id, Long studentId) {
        CourseStudent cs = courseStudentMapper.selectById(id);
        if (cs == null || !cs.getStudentId().equals(studentId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "邀请记录不存在");
        }
        if (!"PENDING".equals(cs.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "当前状态无法操作");
        }
        cs.setStatus("REJECTED");
        courseStudentMapper.updateById(cs);
        log.info("学生拒绝加入课程: studentId={}, courseId={}", studentId, cs.getCourseId());
    }

    @Override
    public void removeStudent(Long courseId, Long studentId) {
        CourseStudent cs = courseStudentMapper.selectOne(
                new LambdaQueryWrapper<CourseStudent>()
                        .eq(CourseStudent::getCourseId, courseId)
                        .eq(CourseStudent::getStudentId, studentId)
        );
        if (cs != null) {
            courseStudentMapper.deleteById(cs.getId());
        }
    }

    @Override
    public List<CourseStudentVO> getCourseStudents(Long courseId) {
        List<CourseStudent> list = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>()
                        .eq(CourseStudent::getCourseId, courseId)
                        .orderByDesc(CourseStudent::getCreateTime)
        );
        return convertToVOList(list);
    }

    @Override
    public List<CourseStudentVO> getStudentInvitations(Long studentId) {
        List<CourseStudent> list = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>()
                        .eq(CourseStudent::getStudentId, studentId)
                        .orderByDesc(CourseStudent::getCreateTime)
        );
        return convertToVOList(list);
    }

    @Override
    public List<CourseStudentVO> getStudentCourses(Long studentId) {
        List<CourseStudent> list = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>()
                        .eq(CourseStudent::getStudentId, studentId)
                        .eq(CourseStudent::getStatus, "ACCEPTED")
                        .orderByDesc(CourseStudent::getCreateTime)
        );
        return convertToVOList(list);
    }

    private List<CourseStudentVO> convertToVOList(List<CourseStudent> list) {
        List<CourseStudentVO> voList = new ArrayList<>();
        for (CourseStudent cs : list) {
            Course course = courseMapper.selectById(cs.getCourseId());
            User student = userMapper.selectById(cs.getStudentId());
            User inviter = cs.getInviterId() != null ? userMapper.selectById(cs.getInviterId()) : null;

            voList.add(CourseStudentVO.builder()
                    .id(cs.getId())
                    .courseId(cs.getCourseId())
                    .courseName(course != null ? course.getCourseName() : "未知课程")
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
}
