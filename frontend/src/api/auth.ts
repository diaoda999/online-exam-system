import api from './axios';
import type { Result, LoginRequest, RegisterRequest, LoginVO } from '../types';

export const login = (data: LoginRequest) =>
  api.post<Result<LoginVO>>('/user/login', data).then((res) => res.data.data);

export const register = (data: RegisterRequest) =>
  api.post<Result<void>>('/user/register', data).then((res) => res.data);
