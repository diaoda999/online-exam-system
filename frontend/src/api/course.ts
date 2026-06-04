import api from './axios';
import type { Result, PageData, CourseVO, CourseCreateRequest, CourseUpdateRequest } from '../types';

export const createCourse = (data: CourseCreateRequest) =>
  api.post<Result<CourseVO>>('/course', data).then((res) => res.data.data);

export const getCourse = (id: number) =>
  api.get<Result<CourseVO>>(`/course/${id}`).then((res) => res.data.data);

export const listCourses = (params: { teacherId?: number; page?: number; size?: number }) =>
  api.get<Result<PageData<CourseVO>>>('/course/list', { params }).then((res) => res.data.data);

export const updateCourse = (id: number, data: CourseUpdateRequest) =>
  api.put<Result<void>>(`/course/${id}`, data).then((res) => res.data);

export const deleteCourse = (id: number) =>
  api.delete<Result<void>>(`/course/${id}`).then((res) => res.data);
