import api from './axios';
import type { Result, PageData, QuestionVO, QuestionCreateRequest, QuestionUpdateRequest } from '../types';

export const createQuestion = (data: QuestionCreateRequest) =>
  api.post<Result<QuestionVO>>('/question', data).then((res) => res.data.data);

export const getQuestion = (id: number) =>
  api.get<Result<QuestionVO>>(`/question/${id}`).then((res) => res.data.data);

export const listQuestions = (params: {
  questionType?: number;
  difficulty?: number;
  subject?: string;
  keyword?: string;
  bankId?: number;
  page?: number;
  size?: number;
}) => api.get<Result<PageData<QuestionVO>>>('/question/list', { params }).then((res) => res.data.data);

export const updateQuestion = (id: number, data: QuestionUpdateRequest) =>
  api.put<Result<void>>(`/question/${id}`, data).then((res) => res.data);

export const deleteQuestion = (id: number) =>
  api.delete<Result<void>>(`/question/${id}`).then((res) => res.data);
