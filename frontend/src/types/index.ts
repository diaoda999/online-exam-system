/** 用户角色 */
export type RoleCode = 'ADMIN' | 'TEACHER' | 'STUDENT';

/** 题目类型 */
export type QuestionType = 1 | 2 | 3 | 4 | 5;

export const QuestionTypeLabels: Record<QuestionType, string> = {
  1: '单选题',
  2: '多选题',
  3: '判断题',
  4: '填空题',
  5: '简答题',
};

export const QuestionTypes: { value: QuestionType; label: string }[] = [
  { value: 1, label: '单选题' },
  { value: 2, label: '多选题' },
  { value: 3, label: '判断题' },
  { value: 4, label: '填空题' },
  { value: 5, label: '简答题' },
];

/** 难度 */
export type Difficulty = 1 | 2 | 3;

export const DifficultyLabels: Record<Difficulty, string> = {
  1: '简单',
  2: '中等',
  3: '困难',
};

export const Difficulties: { value: Difficulty; label: string }[] = [
  { value: 1, label: '简单' },
  { value: 2, label: '中等' },
  { value: 3, label: '困难' },
];

/** 考试状态 */
export type ExamStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'ENDED';

export const ExamStatusLabels: Record<ExamStatus, string> = {
  NOT_STARTED: '未开始',
  IN_PROGRESS: '进行中',
  ENDED: '已结束',
};

/** 考试记录状态 */
export type RecordStatus = 'STARTED' | 'SUBMITTED' | 'GRADED';

export const RecordStatusLabels: Record<RecordStatus, string> = {
  STARTED: '答题中',
  SUBMITTED: '已提交',
  GRADED: '已批改',
};

/** 统一响应 */
export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

/** 分页数据 */
export interface PageData<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/** 登录请求 */
export interface LoginRequest {
  username: string;
  password: string;
}

/** 注册请求 */
export interface RegisterRequest {
  username: string;
  password: string;
  realName: string;
  roleCode: RoleCode;
}

/** 登录响应 */
export interface LoginVO {
  token: string;
  userId: number;
  username: string;
  realName: string;
  roleCode: RoleCode;
}

/** 用户VO */
export interface UserVO {
  id: number;
  username: string;
  realName: string;
  roleCode: RoleCode;
  roleName: string;
  status: number;
  createTime: string;
}

/** 用户更新请求 */
export interface UserUpdateRequest {
  realName?: string;
  status?: number;
}

/** 管理员用户VO（含明文密码） */
export interface AdminUserVO {
  id: number;
  username: string;
  plainPassword: string;
  realName: string;
  roleCode: RoleCode;
  roleName: string;
  status: number;
  createTime: string;
}

/** 班级学生邀请VO */
export interface ClassStudentVO {
  id: number;
  classId: number;
  className: string;
  studentId: number;
  studentName: string;
  studentUsername: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  inviterId: number;
  inviterName: string;
  createTime: string;
}

/** 课程学生VO */
export interface CourseStudentVO {
  id: number;
  courseId: number;
  courseName: string;
  studentId: number;
  studentName: string;
  studentUsername: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  inviterId: number;
  inviterName: string;
  createTime: string;
}

/** 班级需修读课程VO */
export interface ClassCourseVO {
  id: number;
  classId: number;
  className: string;
  courseId: number;
  courseName: string;
  courseCode: string;
  adderId: number;
  adderName: string;
  createTime: string;
}

/** 课程VO */
export interface CourseVO {
  id: number;
  courseName: string;
  courseCode: string;
  teacherId: number;
  teacherName: string;
  description: string;
  createTime: string;
}

/** 课程创建请求 */
export interface CourseCreateRequest {
  courseName: string;
  courseCode: string;
  teacherId: number;
  description?: string;
}

/** 课程更新请求 */
export interface CourseUpdateRequest {
  courseName?: string;
  courseCode?: string;
  teacherId?: number;
  description?: string;
}

/** 班级VO */
export interface ClassVO {
  id: number;
  className: string;
  courseId: number;
  courseName: string;
  studentCount: number;
  createTime: string;
}

/** 班级详情VO */
export interface ClassDetailVO {
  id: number;
  className: string;
  courseId: number;
  courseName: string;
  teacherId: number;
  teacherName: string;
  studentCount: number;
  createTime: string;
  students: UserVO[];
}

/** 班级创建请求 */
export interface ClassCreateRequest {
  className: string;
  courseId: number;
}

/** 班级更新请求 */
export interface ClassUpdateRequest {
  className?: string;
  courseId?: number;
}

/** 题目VO */
export interface QuestionVO {
  id: number;
  questionType: QuestionType;
  difficulty: Difficulty;
  subject: string;
  content: string;
  options: string;
  correctAnswer: string;
  analysis: string;
  score: number;
  bankNames: string[];
  creatorName: string;
  createTime: string;
}

/** 题目创建请求 */
export interface QuestionCreateRequest {
  questionType: QuestionType;
  difficulty: Difficulty;
  subject: string;
  content: string;
  options?: string;
  correctAnswer: string;
  analysis?: string;
  score: number;
  bankIds?: number[];
}

/** 题目更新请求 */
export interface QuestionUpdateRequest {
  questionType?: QuestionType;
  difficulty?: Difficulty;
  subject?: string;
  content?: string;
  options?: string;
  correctAnswer?: string;
  analysis?: string;
  score?: number;
  bankIds?: number[];
}

/** 题库VO */
export interface BankVO {
  id: number;
  bankName: string;
  description: string;
  questionCount: number;
  creatorName: string;
  createTime: string;
}

/** 题库详情VO */
export interface BankDetailVO {
  id: number;
  bankName: string;
  description: string;
  questionCount: number;
  questions: QuestionVO[];
}

/** 题库创建请求 */
export interface BankCreateRequest {
  bankName: string;
  description?: string;
}

/** 题库更新请求 */
export interface BankUpdateRequest {
  bankName?: string;
  description?: string;
}

/** 试卷VO */
export interface PaperVO {
  id: number;
  paperName: string;
  paperType: number;
  totalScore: number;
  questionCount: number;
  creatorName: string;
  createTime: string;
}

/** 试卷题目VO */
export interface PaperQuestionVO {
  id: number;
  questionId: number;
  questionType: QuestionType;
  content: string;
  options: string;
  score: number;
  sortOrder: number;
}

/** 试卷规则VO */
export interface PaperRuleVO {
  id: number;
  questionType: QuestionType;
  difficulty: Difficulty;
  questionCount: number;
  scorePerQuestion: number;
}

/** 试卷详情VO */
export interface PaperDetailVO {
  id: number;
  paperName: string;
  paperType: number;
  totalScore: number;
  questions: PaperQuestionVO[];
  rules: PaperRuleVO[];
}

/** 试卷创建请求 */
export interface PaperCreateRequest {
  paperName: string;
  paperType: number;
  questions?: { questionId: number; score: number }[];
  rules?: { questionType: QuestionType; difficulty: Difficulty; questionCount: number; scorePerQuestion: number }[];
}

/** 考试VO */
export interface ExamVO {
  id: number;
  examName: string;
  paperId: number;
  paperName: string;
  classId: number;
  className: string;
  startTime: string;
  endTime: string;
  duration: number;
  status: ExamStatus;
  creatorName: string;
  studentCount: number;
  submittedCount: number;
  createTime: string;
}

/** 考试详情VO */
export interface ExamDetailVO {
  id: number;
  examName: string;
  paperId: number;
  paperName: string;
  classId: number;
  className: string;
  startTime: string;
  endTime: string;
  duration: number;
  status: ExamStatus;
  creatorName: string;
  studentCount: number;
  submittedCount: number;
  paper: PaperDetailVO;
}

/** 考试创建请求 */
export interface ExamCreateRequest {
  examName: string;
  paperId: number;
  classId: number;
  startTime: string;
  endTime: string;
  duration: number;
}

/** 考试更新请求 */
export interface ExamUpdateRequest {
  examName?: string;
  paperId?: number;
  classId?: number;
  startTime?: string;
  endTime?: string;
  duration?: number;
}

/** 考试题目VO（进入考试返回） */
export interface ExamQuestionVO {
  questionId: number;
  questionType: QuestionType;
  difficulty: Difficulty;
  subject: string;
  content: string;
  optionA?: string;
  optionB?: string;
  optionC?: string;
  optionD?: string;
  optionE?: string;
  optionF?: string;
  optionG?: string;
  optionH?: string;
  score: number;
  sortOrder: number;
}

/** 进入考试返回 */
export interface ExamEnterVO {
  examToken: string;
  examName: string;
  duration: number;
  remainingSeconds: number;
  questions: ExamQuestionVO[];
  savedAnswers: Record<number, string>;
}

/** 答题项 */
export interface AnswerItem {
  questionId: number;
  answer: string;
}

/** 考试提交请求 */
export interface ExamSubmitRequest {
  examToken: string;
  answers: AnswerItem[];
}

/** 保存进度请求 */
export interface ExamSaveProgressRequest {
  examToken: string;
  questionId: number;
  answer: string;
}

/** 考试记录VO */
export interface ExamRecordVO {
  id: number;
  examId: number;
  userId: number;
  examName: string;
  username: string;
  realName: string;
  status: RecordStatus;
  totalScore: number;
  objectiveScore: number;
  subjectiveScore: number;
  submitTime: string;
  createTime: string;
}

/** 考试答案VO */
export interface ExamAnswerVO {
  id: number;
  recordId: number;
  questionId: number;
  questionType: QuestionType;
  content: string;
  options: string;
  correctAnswer: string;
  studentAnswer: string;
  score: number;
  isCorrect: number | null;
  analysis: string;
}

/** 考试记录详情VO */
export interface ExamRecordDetailVO {
  id: number;
  examId: number;
  userId: number;
  username: string;
  realName: string;
  status: RecordStatus;
  totalScore: number;
  objectiveScore: number;
  subjectiveScore: number;
  answers: ExamAnswerVO[];
}

/** 批改项 */
export interface GradeItem {
  answerId: number;
  score: number;
  isCorrect: number;
}

/** 批改请求 */
export interface GradeRequest {
  answers: GradeItem[];
}
