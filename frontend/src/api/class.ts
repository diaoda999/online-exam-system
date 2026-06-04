import api from './axios';
import type { Result, PageData, ClassVO, ClassDetailVO, ClassCreateRequest, ClassUpdateRequest } from '../types';

export const createClass = (data: ClassCreateRequest) =>
  api.post<Result<ClassVO>>('/class', data).then((res) => res.data.data);

export const getClass = (id: number) =>
  api.get<Result<ClassDetailVO>>(`/class/${id}`).then((res) => res.data.data);

export const listClasses = (params: { courseId?: number; page?: number; size?: number }) =>
  api.get<Result<PageData<ClassVO>>>('/class/list', { params }).then((res) => res.data.data);

export const updateClass = (id: number, data: ClassUpdateRequest) =>
  api.put<Result<void>>(`/class/${id}`, data).then((res) => res.data);

export const deleteClass = (id: number) =>
  api.delete<Result<void>>(`/class/${id}`).then((res) => res.data);

export const addStudents = (id: number, studentIds: number[]) =>
  api.post<Result<void>>(`/class/${id}/students`, { studentIds }).then((res) => res.data);

export const removeStudents = (id: number, studentIds: number[]) =>
  api.delete<Result<void>>(`/class/${id}/students`, { data: { studentIds } }).then((res) => res.data);
