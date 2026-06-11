import api from './axios';
import type { Result, ClassStudentVO, ClassCourseVO } from '../types';

// ===== 班级学生邀请 =====

/** 邀请学生加入班级 */
export const inviteStudents = (classId: number, studentIds: number[]) =>
  api.post<Result<void>>('/class-student/invite', { classId, studentIds }).then((res) => res.data);

/** 学生同意加入 */
export const acceptInvitation = (id: number) =>
  api.post<Result<void>>(`/class-student/${id}/accept`).then((res) => res.data);

/** 学生拒绝加入 */
export const rejectInvitation = (id: number) =>
  api.post<Result<void>>(`/class-student/${id}/reject`).then((res) => res.data);

/** 移除班级学生 */
export const removeClassStudent = (classId: number, studentId: number) =>
  api.delete<Result<void>>('/class-student/remove', { data: { classId, studentId } }).then((res) => res.data);

/** 获取班级学生列表 */
export const getClassStudents = (classId: number) =>
  api.get<Result<ClassStudentVO[]>>(`/class-student/class/${classId}`).then((res) => res.data.data);

/** 获取我的班级邀请 */
export const getMyInvitations = () =>
  api.get<Result<ClassStudentVO[]>>('/class-student/my-invitations').then((res) => res.data.data);

/** 获取我的已加入班级 */
export const getMyClasses = () =>
  api.get<Result<ClassStudentVO[]>>('/class-student/my-classes').then((res) => res.data.data);

// ===== 班级需修读课程 =====

/** 添加课程到班级需修读 */
export const addCourseToClass = (classId: number, courseId: number) =>
  api.post<Result<void>>('/class-student/class-course/add', { classId, courseId }).then((res) => res.data);

/** 从班级需修读移除课程 */
export const removeCourseFromClass = (classId: number, courseId: number) =>
  api.delete<Result<void>>('/class-student/class-course/remove', { data: { classId, courseId } }).then((res) => res.data);

/** 获取班级的需修读课程列表 */
export const getClassCourses = (classId: number) =>
  api.get<Result<ClassCourseVO[]>>(`/class-student/class-course/class/${classId}`).then((res) => res.data.data);

/** 获取所有班级的需修读课程（所有人可见） */
export const getAllClassCourses = () =>
  api.get<Result<ClassCourseVO[]>>('/class-student/class-course/all').then((res) => res.data.data);
