package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.dto.course.ClassCreateRequest;
import com.exam.model.dto.course.ClassUpdateRequest;
import com.exam.model.vo.course.ClassDetailVO;
import com.exam.model.vo.course.ClassVO;

import java.util.List;

/**
 * 班级服务接口
 */
public interface ClassService {

    /**
     * 创建班级
     *
     * @param request   创建班级请求
     * @param teacherId 教师ID
     */
    void createClass(ClassCreateRequest request, Long teacherId);

    /**
     * 更新班级
     *
     * @param id      班级ID
     * @param request 更新请求
     */
    void updateClass(Long id, ClassUpdateRequest request);

    /**
     * 删除班级
     *
     * @param id 班级ID
     */
    void deleteClass(Long id);

    /**
     * 根据ID获取班级详情
     *
     * @param id 班级ID
     * @return 班级详情视图对象
     */
    ClassDetailVO getClassById(Long id);

    /**
     * 分页查询班级列表
     *
     * @param courseId 课程ID（可选）
     * @param page     页码
     * @param size     每页数量
     * @return 分页结果
     */
    IPage<ClassVO> listClasses(Long courseId, int page, int size);

    /**
     * 添加学生到班级
     *
     * @param classId    班级ID
     * @param studentIds 学生ID列表
     */
    void addStudents(Long classId, List<Long> studentIds);

    /**
     * 从班级移除学生
     *
     * @param classId    班级ID
     * @param studentIds 学生ID列表
     */
    void removeStudents(Long classId, List<Long> studentIds);
}
