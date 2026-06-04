import api from './axios';
import type { Result, PageData, PaperVO, PaperDetailVO, PaperCreateRequest } from '../types';

export const createPaper = (data: PaperCreateRequest) =>
  api.post<Result<PaperVO>>('/paper', data).then((res) => res.data.data);

export const getPaper = (id: number) =>
  api.get<Result<PaperDetailVO>>(`/paper/${id}`).then((res) => res.data.data);

export const listPapers = (params: { creatorId?: number; page?: number; size?: number }) =>
  api.get<Result<PageData<PaperVO>>>('/paper/list', { params }).then((res) => res.data.data);

export const updatePaper = (id: number, data: Partial<PaperCreateRequest>) =>
  api.put<Result<void>>(`/paper/${id}`, data).then((res) => res.data);

export const deletePaper = (id: number) =>
  api.delete<Result<void>>(`/paper/${id}`).then((res) => res.data);
