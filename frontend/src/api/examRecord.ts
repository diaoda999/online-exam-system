import api from './axios';
import type { Result, PageData, ExamRecordVO, ExamRecordDetailVO, GradeRequest } from '../types';

export const listRecords = (params: { examId: number; status?: string; page?: number; size?: number }) =>
  api.get<Result<PageData<ExamRecordVO>>>('/exam-record/list', { params }).then((res) => res.data.data);

export const getRecordDetail = (id: number) =>
  api.get<Result<ExamRecordDetailVO>>(`/exam-record/${id}`).then((res) => res.data.data);

export const gradeObjective = (examId: number) =>
  api.post<Result<void>>(`/exam-record/${examId}/grade-objective`).then((res) => res.data);

export const gradeSubjective = (data: GradeRequest) =>
  api.post<Result<void>>('/exam-record/grade-subjective', data).then((res) => res.data);
