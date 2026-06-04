import api from './axios';
import type {
  Result, PageData, ExamVO, ExamDetailVO, ExamCreateRequest, ExamUpdateRequest,
  ExamEnterVO, ExamSaveProgressRequest, ExamSubmitRequest, ExamRecordVO,
} from '../types';

export const createExam = (data: ExamCreateRequest) =>
  api.post<Result<ExamVO>>('/exam', data).then((res) => res.data.data);

export const getExam = (id: number) =>
  api.get<Result<ExamDetailVO>>(`/exam/${id}`).then((res) => res.data.data);

export const listExams = (params: { creatorId?: number; status?: string; page?: number; size?: number }) =>
  api.get<Result<PageData<ExamVO>>>('/exam/list', { params }).then((res) => res.data.data);

export const updateExam = (id: number, data: ExamUpdateRequest) =>
  api.put<Result<void>>(`/exam/${id}`, data).then((res) => res.data);

export const deleteExam = (id: number) =>
  api.delete<Result<void>>(`/exam/${id}`).then((res) => res.data);

export const publishExam = (id: number) =>
  api.post<Result<void>>(`/exam/${id}/publish`).then((res) => res.data);

export const enterExam = (id: number) =>
  api.post<Result<ExamEnterVO>>(`/exam/${id}/enter`).then((res) => res.data.data);

export const saveProgress = (data: ExamSaveProgressRequest) =>
  api.post<Result<void>>('/exam/progress', data).then((res) => res.data);

export const submitExam = (data: ExamSubmitRequest) =>
  api.post<Result<void>>('/exam/submit', data).then((res) => res.data);

export const getRemainingTime = (id: number) =>
  api.get<Result<number>>(`/exam/${id}/remaining`).then((res) => res.data.data);

export const endExam = (id: number) =>
  api.post<Result<void>>(`/exam/${id}/end`).then((res) => res.data);

export const getMyExamRecord = (id: number) =>
  api.get<Result<ExamRecordVO>>(`/exam/${id}/record`).then((res) => res.data.data);
