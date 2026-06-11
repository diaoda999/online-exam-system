import api from './axios';
import type { Result, CourseStudentVO } from '../types';

/** 邀请学生加入课程 */
export const inviteStudents = (courseId: number, studentIds: number[]) =>
  api.post<Result<void>>('/course-student/invite', { courseId, studentIds }).then((res) => res.data);

/** 学生同意加入 */
export const acceptInvitation = (id: number) =>
  api.post<Result<void>>(`/course-student/${id}/accept`).then((res) => res.data);

/** 学生拒绝加入 */
export const rejectInvitation = (id: number) =>
  api.post<Result<void>>(`/course-student/${id}/reject`).then((res) => res.data);

/** 移除课程学生 */
export const removeCourseStudent = (courseId: number, studentId: number) =>
  api.delete<Result<void>>('/course-student/remove', { data: { courseId, studentId } }).then((res) => res.data);

/** 获取课程学生列表 */
export const getCourseStudents = (courseId: number) =>
  api.get<Result<CourseStudentVO[]>>(`/course-student/course/${courseId}`).then((res) => res.data.data);

/** 获取我的邀请 */
export const getMyInvitations = () =>
  api.get<Result<CourseStudentVO[]>>('/course-student/my-invitations').then((res) => res.data.data);

/** 获取我的已加入课程 */
export const getMyCourses = () =>
  api.get<Result<CourseStudentVO[]>>('/course-student/my-courses').then((res) => res.data.data);
