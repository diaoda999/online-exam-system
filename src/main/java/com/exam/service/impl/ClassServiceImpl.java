package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ResultCode;
import com.exam.model.dto.course.ClassCreateRequest;
import com.exam.model.dto.course.ClassUpdateRequest;
import com.exam.model.entity.ClassEntity;
import com.exam.model.entity.ClassStudent;
import com.exam.model.entity.Course;
import com.exam.model.entity.Role;
import com.exam.model.entity.User;
import com.exam.model.mapper.ClassMapper;
import com.exam.model.mapper.ClassStudentMapper;
import com.exam.model.mapper.CourseMapper;
import com.exam.model.mapper.RoleMapper;
import com.exam.model.mapper.UserMapper;
import com.exam.model.vo.course.ClassDetailVO;
import com.exam.model.vo.course.ClassVO;
import com.exam.model.vo.user.UserVO;
import com.exam.service.ClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 班级服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClassMapper classMapper;
    private final ClassStudentMapper classStudentMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createClass(ClassCreateRequest request, Long teacherId) {
        // 验证课程存在
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "课程不存在");
        }

        // 创建班级
        ClassEntity classEntity = ClassEntity.builder()
                .className(request.getClassName())
                .courseId(request.getCourseId())
                .teacherId(teacherId)
                .build();
        classMapper.insert(classEntity);

        // 添加学生（如果提供了学生列表）
        if (request.getStudentIds() != null && !request.getStudentIds().isEmpty()) {
            addStudents(classEntity.getId(), request.getStudentIds());
        }

        log.info("班级创建成功: className={}, teacherId={}", request.getClassName(), teacherId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateClass(Long id, ClassUpdateRequest request) {
        ClassEntity classEntity = classMapper.selectById(id);
        if (classEntity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班级不存在");
        }

        // 更新班级名称
        if (request.getClassName() != null) {
            classEntity.setClassName(request.getClassName());
            classMapper.updateById(classEntity);
        }

        // 更新学生列表（全量替换）
        if (request.getStudentIds() != null) {
            // 先删除所有旧关联
            classStudentMapper.delete(
                    new LambdaQueryWrapper<ClassStudent>().eq(ClassStudent::getClassId, id)
            );
            // 再添加新关联
            if (!request.getStudentIds().isEmpty()) {
                addStudents(id, request.getStudentIds());
            }
        }

        log.info("班级更新成功: classId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteClass(Long id) {
        ClassEntity classEntity = classMapper.selectById(id);
        if (classEntity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班级不存在");
        }

        // 删除班级-学生关联
        classStudentMapper.delete(
                new LambdaQueryWrapper<ClassStudent>().eq(ClassStudent::getClassId, id)
        );

        // 删除班级
        classMapper.deleteById(id);
        log.info("班级删除: classId={}", id);
    }

    @Override
    public ClassDetailVO getClassById(Long id) {
        ClassEntity classEntity = classMapper.selectById(id);
        if (classEntity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班级不存在");
        }

        Course course = courseMapper.selectById(classEntity.getCourseId());
        User teacher = userMapper.selectById(classEntity.getTeacherId());

        // 查询班级学生数
        Long studentCount = classStudentMapper.selectCount(
                new LambdaQueryWrapper<ClassStudent>().eq(ClassStudent::getClassId, id)
        );

        // 查询班级学生列表
        List<ClassStudent> classStudents = classStudentMapper.selectList(
                new LambdaQueryWrapper<ClassStudent>().eq(ClassStudent::getClassId, id)
        );
        List<UserVO> studentVOs = new ArrayList<>();
        for (ClassStudent cs : classStudents) {
            User student = userMapper.selectById(cs.getStudentId());
            if (student != null) {
                Role role = roleMapper.selectById(student.getRoleId());
                studentVOs.add(UserVO.builder()
                        .id(student.getId())
                        .username(student.getUsername())
                        .realName(student.getRealName())
                        .roleCode(role != null ? role.getRoleCode() : null)
                        .roleName(role != null ? role.getRoleName() : null)
                        .status(student.getStatus())
                        .createTime(student.getCreateTime())
                        .build());
            }
        }

        return ClassDetailVO.builder()
                .id(classEntity.getId())
                .className(classEntity.getClassName())
                .courseId(classEntity.getCourseId())
                .courseName(course != null ? course.getCourseName() : null)
                .teacherId(classEntity.getTeacherId())
                .teacherName(teacher != null ? teacher.getRealName() : null)
                .studentCount(studentCount.intValue())
                .createTime(classEntity.getCreateTime())
                .students(studentVOs)
                .build();
    }

    @Override
    public IPage<ClassVO> listClasses(Long courseId, int page, int size) {
        Page<ClassVO> pageParam = new Page<>(page, size);
        return classMapper.selectClassListWithDetails(pageParam, courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStudents(Long classId, List<Long> studentIds) {
        ClassEntity classEntity = classMapper.selectById(classId);
        if (classEntity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班级不存在");
        }

        for (Long studentId : studentIds) {
            // 检查学生是否存在
            User student = userMapper.selectById(studentId);
            if (student == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "学生不存在: " + studentId);
            }

            // 检查是否已在该班级中
            Long exists = classStudentMapper.selectCount(
                    new LambdaQueryWrapper<ClassStudent>()
                            .eq(ClassStudent::getClassId, classId)
                            .eq(ClassStudent::getStudentId, studentId)
            );
            if (exists > 0) {
                log.warn("学生已在班级中: classId={}, studentId={}", classId, studentId);
                continue;
            }

            ClassStudent cs = ClassStudent.builder()
                    .classId(classId)
                    .studentId(studentId)
                    .build();
            classStudentMapper.insert(cs);
        }

        log.info("学生添加到班级: classId={}, studentCount={}", classId, studentIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeStudents(Long classId, List<Long> studentIds) {
        ClassEntity classEntity = classMapper.selectById(classId);
        if (classEntity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班级不存在");
        }

        for (Long studentId : studentIds) {
            classStudentMapper.delete(
                    new LambdaQueryWrapper<ClassStudent>()
                            .eq(ClassStudent::getClassId, classId)
                            .eq(ClassStudent::getStudentId, studentId)
            );
        }

        log.info("学生从班级移除: classId={}, studentCount={}", classId, studentIds.size());
    }
}
