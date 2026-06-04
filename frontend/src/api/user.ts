import api from './axios';
import type { Result, PageData, UserVO, UserUpdateRequest } from '../types';

export const getUser = (id: number) =>
  api.get<Result<UserVO>>(`/user/${id}`).then((res) => res.data.data);

export const listUsers = (params: { roleCode?: string; status?: number; page?: number; size?: number }) =>
  api.get<Result<PageData<UserVO>>>('/user/list', { params }).then((res) => res.data.data);

export const updateUser = (id: number, data: UserUpdateRequest) =>
  api.put<Result<void>>(`/user/${id}`, data).then((res) => res.data);

export const deleteUser = (id: number) =>
  api.delete<Result<void>>(`/user/${id}`).then((res) => res.data);
