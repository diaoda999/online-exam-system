import React, { useEffect, useState, useCallback, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box, Button, Paper, Typography, Radio, RadioGroup, FormControlLabel,
  Checkbox, FormGroup, TextField, Alert, Dialog, Divider,
  DialogTitle, DialogContent, DialogActions, Snackbar,
} from '@mui/material';
import { ArrowBack, ArrowForward, Send as SendIcon } from '@mui/icons-material';
import { enterExam, saveProgress, submitExam } from '../api/exam';
import type { ExamEnterVO, ExamQuestionVO, QuestionType, AnswerItem } from '../types';
import Timer from '../components/Timer';

/**
 * 从 ExamQuestionVO 的独立选项字段构建选项列表
 * 后端返回 optionA/B/C/D/... 而非 options JSON 字符串
 */
const buildOptions = (q: ExamQuestionVO): { label: string; value: string }[] => {
  const options: { label: string; value: string }[] = [];
  const optionMap: Record<string, string | undefined> = {
    A: q.optionA,
    B: q.optionB,
    C: q.optionC,
    D: q.optionD,
    E: q.optionE,
    F: q.optionF,
    G: q.optionG,
    H: q.optionH,
  };
  for (const [label, value] of Object.entries(optionMap)) {
    if (value !== undefined && value !== null && value !== '') {
      options.push({ label, value });
    }
  }
  return options;
};

const ExamRoom: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const examId = Number(id);

  const [examData, setExamData] = useState<ExamEnterVO | null>(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [answers, setAnswers] = useState<Record<number, string>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [confirmMessage, setConfirmMessage] = useState('');
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });
  const autoSaveTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const submittedRef = useRef(false);
  const savingRef = useRef<Record<number, boolean>>({});

  // 进入考试并加载已保存进度
  useEffect(() => {
    const enter = async () => {
      try {
        const data = await enterExam(examId);
        setExamData(data);
        localStorage.setItem('examToken', data.examToken);

        // 从 enterExam 返回的 savedAnswers 恢复答题进度
        if (data.savedAnswers && Object.keys(data.savedAnswers).length > 0) {
          setAnswers(data.savedAnswers);
        }

        setLoading(false);
      } catch (err: any) {
        setError(err.message || '进入考试失败');
        setLoading(false);
      }
    };
    enter();
  }, [examId]);

  // 自动保存进度（每30秒）
  useEffect(() => {
    if (!examData) return;
    autoSaveTimerRef.current = setInterval(() => {
      Object.entries(answers).forEach(([qId, answer]) => {
        if (answer && !savingRef.current[Number(qId)]) {
          savingRef.current[Number(qId)] = true;
          saveProgress({
            examToken: examData.examToken,
            questionId: Number(qId),
            answer,
          }).catch(() => { /* silent */ }).finally(() => {
            savingRef.current[Number(qId)] = false;
          });
        }
      });
    }, 30000);
    return () => {
      if (autoSaveTimerRef.current) clearInterval(autoSaveTimerRef.current);
    };
  }, [examData, answers]);

  // 退出提示
  useEffect(() => {
    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
      if (!submittedRef.current) {
        e.preventDefault();
      }
    };
    // 禁用右键
    const handleContextMenu = (e: MouseEvent) => e.preventDefault();
    window.addEventListener('beforeunload', handleBeforeUnload);
    document.addEventListener('contextmenu', handleContextMenu);
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
      document.removeEventListener('contextmenu', handleContextMenu);
    };
  }, []);

  /**
   * 处理答案变更，同时保存进度到后端 Redis
   */
  const handleAnswerChange = useCallback((questionId: number, value: string) => {
    setAnswers((prev) => {
      const newAnswers = { ...prev, [questionId]: value };

      // 答案变更时保存到后端 Redis（防抖：避免频繁请求）
      if (examData && value && !savingRef.current[questionId]) {
        savingRef.current[questionId] = true;
        saveProgress({
          examToken: examData.examToken,
          questionId,
          answer: value,
        }).catch(() => { /* silent */ }).finally(() => {
          savingRef.current[questionId] = false;
        });
      }

      return newAnswers;
    });
  }, [examData]);

  const handleTimeUp = useCallback(() => {
    if (!submittedRef.current && examData) {
      doSubmit(true);
    }
  }, [examData, answers]);

  /**
   * 点击交卷按钮：先检查答题完整性，再弹出确认对话框
   */
  const handleConfirmSubmit = () => {
    if (!examData) return;
    const questions = examData.questions;
    const answeredCount = questions.filter((q) => answers[q.questionId] && answers[q.questionId].trim() !== '').length;
    const unansweredCount = questions.length - answeredCount;

    if (unansweredCount > 0) {
      setConfirmMessage(`有题目没有回答，是否交卷？`);
    } else {
      setConfirmMessage(`确认交卷？`);
    }
    setConfirmOpen(true);
  };

  /**
   * 实际执行提交
   */
  const doSubmit = async (isAuto: boolean = false) => {
    if (submittedRef.current) return;
    submittedRef.current = true;

    try {
      const answerList: AnswerItem[] = Object.entries(answers).map(([qId, answer]) => ({
        questionId: Number(qId),
        answer: answer || '',
      }));

      await submitExam({
        examToken: examData!.examToken,
        answers: answerList,
      });

      localStorage.removeItem('examToken');
      setSnackbar({ open: true, message: isAuto ? '考试时间到，已自动提交' : '提交成功', severity: 'success' });
      setTimeout(() => navigate('/my-exams'), 2000);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
      submittedRef.current = false;
    }
  };

  const handleSaveProgress = async () => {
    if (!examData) return;
    try {
      // 保存所有答案到后端
      const savePromises = Object.entries(answers).map(([qId, answer]) => {
        if (answer) {
          return saveProgress({
            examToken: examData.examToken,
            questionId: Number(qId),
            answer,
          });
        }
        return Promise.resolve();
      });
      await Promise.all(savePromises);
      setSnackbar({ open: true, message: '已保存进度', severity: 'success' });
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  if (loading) {
    return <Box sx={{ p: 4 }}><Typography>正在进入考试...</Typography></Box>;
  }

  if (error) {
    return (
      <Box sx={{ p: 4 }}>
        <Alert severity="error">{error}</Alert>
        <Button sx={{ mt: 2 }} onClick={() => navigate('/my-exams')}>返回</Button>
      </Box>
    );
  }

  if (!examData) return null;

  const questions = examData.questions;
  const currentQuestion = questions[currentIndex];
  const answeredCount = questions.filter((q) => answers[q.questionId] && answers[q.questionId].trim() !== '').length;
  const isLast = currentIndex === questions.length - 1;
  const isFirst = currentIndex === 0;

  const renderQuestionContent = (q: ExamQuestionVO) => {
    const userAnswer = answers[q.questionId] || '';

    // 从后端返回的 optionA/B/C/D/... 字段构建选项列表
    const options = buildOptions(q);

    switch (q.questionType as QuestionType) {
      case 1: // 单选题
        return (
          <RadioGroup value={userAnswer} onChange={(e) => handleAnswerChange(q.questionId, e.target.value)}>
            {options.map((opt) => (
              <FormControlLabel
                key={opt.label}
                value={opt.label}
                control={<Radio />}
                label={`${opt.label}. ${opt.value}`}
                sx={{ mb: 1, p: 1, borderRadius: 1, bgcolor: userAnswer === opt.label ? '#e3f2fd' : 'transparent' }}
              />
            ))}
          </RadioGroup>
        );

      case 2: // 多选题
        const selectedValues = userAnswer ? userAnswer.split(',').filter(v => v) : [];
        return (
          <FormGroup>
            {options.map((opt) => {
              const checked = selectedValues.includes(opt.label);
              return (
                <FormControlLabel
                  key={opt.label}
                  control={<Checkbox checked={checked} onChange={() => {
                    const newValues = checked
                      ? selectedValues.filter((v: string) => v !== opt.label)
                      : [...selectedValues, opt.label];
                    const newAnswer = newValues.sort().join(',');
                    handleAnswerChange(q.questionId, newAnswer);
                  }} />}
                  label={`${opt.label}. ${opt.value}`}
                  sx={{ mb: 1, p: 1, borderRadius: 1, bgcolor: checked ? '#e3f2fd' : 'transparent' }}
                />
              );
            })}
          </FormGroup>
        );

      case 3: // 判断题
        return (
          <RadioGroup value={userAnswer} onChange={(e) => handleAnswerChange(q.questionId, e.target.value)}>
            <FormControlLabel value="正确" control={<Radio />} label="正确" sx={{ mb: 1, p: 1, borderRadius: 1, bgcolor: userAnswer === '正确' ? '#e3f2fd' : 'transparent' }} />
            <FormControlLabel value="错误" control={<Radio />} label="错误" sx={{ mb: 1, p: 1, borderRadius: 1, bgcolor: userAnswer === '错误' ? '#e3f2fd' : 'transparent' }} />
          </RadioGroup>
        );

      case 4: // 填空题
        return (
          <TextField
            fullWidth multiline rows={2}
            placeholder="请输入答案"
            value={userAnswer}
            onChange={(e) => handleAnswerChange(q.questionId, e.target.value)}
          />
        );

      case 5: // 简答题
        return (
          <TextField
            fullWidth multiline rows={6}
            placeholder="请输入答案"
            value={userAnswer}
            onChange={(e) => handleAnswerChange(q.questionId, e.target.value)}
          />
        );

      default:
        return null;
    }
  };

  const getQuestionTypeLabel = (type: QuestionType) => {
    const map: Record<number, string> = { 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '简答题' };
    return map[type] || '未知';
  };

  return (
    <Box className="exam-fullscreen" sx={{ p: 0 }}>
      {/* 顶部栏 */}
      <Box sx={{ bgcolor: '#1976d2', color: 'white', p: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography variant="h6" sx={{ color: 'white' }}>{examData.examName}</Typography>
        <Timer remainingSeconds={examData.remainingSeconds} onTimeUp={handleTimeUp} />
        <Box sx={{ color: 'white', textAlign: 'right' }}>
          <Typography variant="body2" sx={{ color: 'white' }}>已答 {answeredCount}/{questions.length}</Typography>
        </Box>
      </Box>

      <Box sx={{ display: 'flex', height: 'calc(100vh - 64px)' }}>
        {/* 左侧题目导航 */}
        <Box sx={{ width: 200, bgcolor: 'white', borderRight: '1px solid #e0e0e0', p: 2, overflowY: 'auto' }}>
          <Typography variant="subtitle2" gutterBottom>题目导航</Typography>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
            {questions.map((q, idx) => {
              const answered = !!(answers[q.questionId] && answers[q.questionId].trim());
              const isCurrent = idx === currentIndex;
              return (
                <Button
                  key={q.questionId}
                  variant={isCurrent ? 'contained' : answered ? 'outlined' : 'text'}
                  color={answered ? 'success' : 'primary'}
                  size="small"
                  sx={{ minWidth: 36, height: 36, m: 0.25 }}
                  onClick={() => setCurrentIndex(idx)}
                >
                  {idx + 1}
                </Button>
              );
            })}
          </Box>
          <Box sx={{ mt: 2, fontSize: 12, color: 'text.secondary' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
              <Box sx={{ width: 12, height: 12, bgcolor: '#4caf50', borderRadius: '50%' }} /> 已答
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box sx={{ width: 12, height: 12, bgcolor: '#e0e0e0', borderRadius: '50%' }} /> 未答
            </Box>
          </Box>
        </Box>

        {/* 中间答题区 */}
        <Box sx={{ flex: 1, p: 4, overflowY: 'auto' }}>
          <Paper sx={{ p: 4, maxWidth: 900, mx: 'auto' }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6">
                第 {currentIndex + 1} 题 · {getQuestionTypeLabel(currentQuestion.questionType as QuestionType)}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                分值: {currentQuestion.score}分
              </Typography>
            </Box>

            <Divider sx={{ mb: 3 }} />

            <Typography variant="body1" sx={{ mb: 3, fontSize: 18, lineHeight: 1.8 }}>
              {currentQuestion.content}
            </Typography>

            {renderQuestionContent(currentQuestion)}
          </Paper>

          {/* 底部导航 */}
          <Box sx={{ maxWidth: 900, mx: 'auto', mt: 2, display: 'flex', justifyContent: 'space-between' }}>
            <Button
              variant="outlined"
              startIcon={<ArrowBack />}
              disabled={isFirst}
              onClick={() => setCurrentIndex(currentIndex - 1)}
            >
              上一题
            </Button>
            <Box sx={{ display: 'flex', gap: 2 }}>
              <Button variant="outlined" onClick={handleSaveProgress}>
                保存进度
              </Button>
              <Button
                variant="contained"
                color="error"
                startIcon={<SendIcon />}
                onClick={handleConfirmSubmit}
              >
                交卷
              </Button>
            </Box>
            <Button
              variant="outlined"
              endIcon={<ArrowForward />}
              disabled={isLast}
              onClick={() => setCurrentIndex(currentIndex + 1)}
            >
              下一题
            </Button>
          </Box>
        </Box>
      </Box>

      {/* 交卷确认对话框 */}
      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle>确认交卷</DialogTitle>
        <DialogContent>
          <Typography>
            {confirmMessage}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)}>继续答题</Button>
          <Button variant="contained" color="error" onClick={() => { setConfirmOpen(false); doSubmit(false); }}>确认交卷</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default ExamRoom;
