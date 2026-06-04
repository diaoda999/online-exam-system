import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Button, Typography, Paper, TextField, MenuItem, Divider, Snackbar, Alert,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, IconButton,
  Chip,
} from '@mui/material';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowBack, Check as CheckIcon } from '@mui/icons-material';
import { getRecordDetail, gradeObjective, gradeSubjective } from '../api/examRecord';
import type { ExamRecordDetailVO, ExamAnswerVO, QuestionType, GradeItem } from '../types';
import { QuestionTypeLabels } from '../types';

const GradingDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const recordId = Number(id);

  const [detail, setDetail] = useState<ExamRecordDetailVO | null>(null);
  const [scores, setScores] = useState<Record<number, number>>({});
  const [corrects, setCorrects] = useState<Record<number, number>>({});
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });
  const [loading, setLoading] = useState(true);

  const fetchData = useCallback(async () => {
    try {
      const data = await getRecordDetail(recordId);
      setDetail(data);
      // 初始化主观题分值
      const initScores: Record<number, number> = {};
      const initCorrects: Record<number, number> = {};
      data.answers.forEach((a) => {
        if (a.score >= 0) initScores[a.id] = a.score;
        if (a.isCorrect !== null) initCorrects[a.id] = a.isCorrect;
      });
      setScores(initScores);
      setCorrects(initCorrects);
      setLoading(false);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
      setLoading(false);
    }
  }, [recordId]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleGradeObjective = async () => {
    if (!detail) return;
    try {
      await gradeObjective(detail.examId);
      setSnackbar({ open: true, message: '客观题批改完成', severity: 'success' });
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handleGradeSubjective = async () => {
    if (!detail) return;
    const items: GradeItem[] = [];
    detail.answers
      .filter((a) => a.questionType === 4 || a.questionType === 5)
      .forEach((a) => {
        if (scores[a.id] !== undefined) {
          items.push({
            answerId: a.id,
            score: scores[a.id],
            isCorrect: corrects[a.id] ?? 0,
          });
        }
      });

    if (items.length === 0) {
      setSnackbar({ open: true, message: '请至少批改一题', severity: 'warning' });
      return;
    }

    try {
      await gradeSubjective({ answers: items });
      setSnackbar({ open: true, message: '主观题批改成功', severity: 'success' });
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  if (loading) {
    return <Box sx={{ p: 4 }}><Typography>加载中...</Typography></Box>;
  }

  if (!detail) {
    return <Box sx={{ p: 4 }}><Typography>未找到记录</Typography></Box>;
  }

  const isObjective = (type: QuestionType) => type === 1 || type === 2 || type === 3;
  const isSubjective = (type: QuestionType) => type === 4 || type === 5;

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <IconButton onClick={() => navigate('/grading')}><ArrowBack /></IconButton>
        <Typography variant="h5" sx={{ ml: 1 }}>批改详情</Typography>
      </Box>

      {/* 学生信息 */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6">学生: {detail.realName} ({detail.username})</Typography>
        <Box sx={{ display: 'flex', gap: 3, mt: 1 }}>
          <Typography>总分: {detail.totalScore >= 0 ? detail.totalScore : '-'}</Typography>
          <Typography>客观题: {detail.objectiveScore >= 0 ? detail.objectiveScore : '-'}</Typography>
          <Typography>主观题: {detail.subjectiveScore >= 0 ? detail.subjectiveScore : '-'}</Typography>
          <Typography>状态: {detail.status}</Typography>
        </Box>
      </Paper>

      {/* 操作按钮 */}
      <Box sx={{ mb: 3, display: 'flex', gap: 2 }}>
        <Button variant="contained" onClick={handleGradeObjective}>
          自动批改客观题
        </Button>
        <Button variant="contained" color="success" onClick={handleGradeSubjective}>
          提交主观题批改
        </Button>
      </Box>

      {/* 答案列表 */}
      {detail.answers.map((answer, idx) => (
        <Paper key={answer.id} sx={{ p: 3, mb: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
            <Typography variant="subtitle1">
              第 {idx + 1} 题 · {QuestionTypeLabels[answer.questionType as QuestionType] || '未知'}
            </Typography>
            <Chip
              label={answer.score >= 0 ? `${answer.score}分` : '未批改'}
              color={answer.score >= 0 ? 'success' : 'default'}
              size="small"
            />
          </Box>
          <Divider sx={{ my: 1 }} />
          <Typography variant="body1" sx={{ mb: 2 }}>{answer.content}</Typography>

          <Box sx={{ bgcolor: '#f5f5f5', p: 2, borderRadius: 1, mb: 2 }}>
            <Typography variant="body2" color="text.secondary">正确答案:</Typography>
            <Typography variant="body1">{answer.correctAnswer}</Typography>
          </Box>
          <Box sx={{ bgcolor: '#e3f2fd', p: 2, borderRadius: 1, mb: 2 }}>
            <Typography variant="body2" color="text.secondary">学生答案:</Typography>
            <Typography variant="body1">{answer.studentAnswer || '（未作答）'}</Typography>
          </Box>
          {answer.analysis && (
            <Box sx={{ bgcolor: '#fff3e0', p: 2, borderRadius: 1, mb: 2 }}>
              <Typography variant="body2" color="text.secondary">解析:</Typography>
              <Typography variant="body2">{answer.analysis}</Typography>
            </Box>
          )}

          {/* 主观题批改 */}
          {isSubjective(answer.questionType as QuestionType) && (
            <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mt: 1 }}>
              <TextField
                type="number" size="small" label="得分"
                value={scores[answer.id] ?? ''}
                onChange={(e) => setScores({ ...scores, [answer.id]: Number(e.target.value) })}
                sx={{ width: 100 }}
              />
              <TextField
                select size="small" label="正误"
                value={corrects[answer.id] ?? ''}
                onChange={(e) => setCorrects({ ...corrects, [answer.id]: Number(e.target.value) })}
                sx={{ width: 100 }}
              >
                <MenuItem value={1}>正确</MenuItem>
                <MenuItem value={0}>错误</MenuItem>
              </TextField>
            </Box>
          )}
        </Paper>
      ))}

      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default GradingDetail;
