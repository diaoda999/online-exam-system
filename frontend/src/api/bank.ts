import api from './axios';
import type { Result, PageData, BankVO, BankDetailVO, BankCreateRequest, BankUpdateRequest } from '../types';

export const createBank = (data: BankCreateRequest) =>
  api.post<Result<BankVO>>('/bank', data).then((res) => res.data.data);

export const getBank = (id: number) =>
  api.get<Result<BankDetailVO>>(`/bank/${id}`).then((res) => res.data.data);

export const listBanks = (params: { keyword?: string; page?: number; size?: number }) =>
  api.get<Result<PageData<BankVO>>>('/bank/list', { params }).then((res) => res.data.data);

export const updateBank = (id: number, data: BankUpdateRequest) =>
  api.put<Result<void>>(`/bank/${id}`, data).then((res) => res.data);

export const deleteBank = (id: number) =>
  api.delete<Result<void>>(`/bank/${id}`).then((res) => res.data);

export const addQuestions = (id: number, questionIds: number[]) =>
  api.post<Result<void>>(`/bank/${id}/questions`, { questionIds }).then((res) => res.data);

export const removeQuestions = (id: number, questionIds: number[]) =>
  api.delete<Result<void>>(`/bank/${id}/questions`, { data: { questionIds } }).then((res) => res.data);
