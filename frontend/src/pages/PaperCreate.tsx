import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Button, TextField, MenuItem, Typography, Grid, IconButton,
  Snackbar, Alert, Paper, Divider, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Select, FormControl, InputLabel,
} from '@mui/material';
import { Add as AddIcon, Delete as DeleteIcon, ArrowBack as BackIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { createPaper } from '../api/paper';
import { listBanks } from '../api/bank';
import { listQuestions } from '../api/question';
import type { QuestionType, Difficulty, QuestionVO, BankVO, PaperQuestionVO } from '../types';
import { QuestionTypes, Difficulties } from '../types';

interface QuestionSelectItem {
  questionId: number;
  score: number;
}

interface RuleItem {
  questionType: QuestionType;
  difficulty: Difficulty;
  questionCount: number;
  scorePerQuestion: number;
}

const PaperCreate: React.FC = () => {
  const navigate = useNavigate();
  const [paperName, setPaperName] = useState('');
  const [paperType, setPaperType] = useState(1);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });

  // 手工组卷
  const [selectedQuestions, setSelectedQuestions] = useState<QuestionSelectItem[]>([]);
  const [availableQuestions, setAvailableQuestions] = useState<QuestionVO[]>([]);
  const [bankFilter, setBankFilter] = useState(0);
  const [banks, setBanks] = useState<BankVO[]>([]);

  // 随机组卷
  const [rules, setRules] = useState<RuleItem[]>([]);

  const fetchBanks = useCallback(async () => {
    try {
      const data = await listBanks({ page: 1, size: 100 });
      setBanks(data.records);
    } catch { /* ignore */ }
  }, []);

  const fetchAvailableQuestions = useCallback(async () => {
    try {
      const data = await listQuestions({ bankId: bankFilter || undefined, page: 1, size: 200 });
      setAvailableQuestions(data.records);
    } catch { /* ignore */ }
  }, [bankFilter]);

  useEffect(() => { fetchBanks(); fetchAvailableQuestions(); }, [fetchBanks, fetchAvailableQuestions]);

  const addQuestion = (q: QuestionVO) => {
    if (selectedQuestions.find((s) => s.questionId === q.id)) return;
    setSelectedQuestions([...selectedQuestions, { questionId: q.id, score: q.score }]);
  };

  const removeQuestion = (questionId: number) => {
    setSelectedQuestions(selectedQuestions.filter((s) => s.questionId !== questionId));
  };

  const updateScore = (questionId: number, score: number) => {
    setSelectedQuestions(selectedQuestions.map((s) => s.questionId === questionId ? { ...s, score } : s));
  };

  const addRule = () => {
    setRules([...rules, { questionType: 1, difficulty: 1, questionCount: 5, scorePerQuestion: 2 }]);
  };

  const removeRule = (index: number) => {
    setRules(rules.filter((_, i) => i !== index));
  };

  const updateRule = (index: number, field: keyof RuleItem, value: number) => {
    setRules(rules.map((r, i) => i === index ? { ...r, [field]: value } : r));
  };

  const handleSubmit = async () => {
    if (!paperName) {
      setSnackbar({ open: true, message: '请填写试卷名称', severity: 'error' });
      return;
    }

    try {
      if (paperType === 1) {
        if (selectedQuestions.length === 0) {
          setSnackbar({ open: true, message: '请至少选择一道题目', severity: 'error' });
          return;
        }
        await createPaper({
          paperName,
          paperType: 1,
          questions: selectedQuestions.map((s) => ({ questionId: s.questionId, score: s.score })),
        });
      } else {
        if (rules.length === 0) {
          setSnackbar({ open: true, message: '请至少添加一条组卷规则', severity: 'error' });
          return;
        }
        await createPaper({
          paperName,
          paperType: 2,
          rules: rules.map((r) => ({
            questionType: r.questionType,
            difficulty: r.difficulty,
            questionCount: r.questionCount,
            scorePerQuestion: r.scorePerQuestion,
          })),
        });
      }
      setSnackbar({ open: true, message: '创建成功', severity: 'success' });
      setTimeout(() => navigate('/papers'), 1000);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const totalScore = paperType === 1
    ? selectedQuestions.reduce((sum, s) => sum + s.score, 0)
    : rules.reduce((sum, r) => sum + r.questionCount * r.scorePerQuestion, 0);

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <IconButton onClick={() => navigate('/papers')}><BackIcon /></IconButton>
        <Typography variant="h5" sx={{ ml: 1 }}>创建试卷</Typography>
      </Box>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Grid container spacing={2}>
          <Grid item xs={6}>
            <TextField fullWidth label="试卷名称" value={paperName} onChange={(e) => setPaperName(e.target.value)} />
          </Grid>
          <Grid item xs={6}>
            <TextField fullWidth select label="组卷方式" value={paperType} onChange={(e) => setPaperType(Number(e.target.value))}>
              <MenuItem value={1}>手工组卷</MenuItem>
              <MenuItem value={2}>随机组卷</MenuItem>
            </TextField>
          </Grid>
        </Grid>
      </Paper>

      {paperType === 1 ? (
        <>
          {/* 手工组卷 */}
          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" gutterBottom>选择题目</Typography>
            <Box sx={{ mb: 2 }}>
              <TextField select size="small" label="按题库筛选" value={bankFilter} onChange={(e) => setBankFilter(Number(e.target.value))} sx={{ width: 200 }}>
                <MenuItem value={0}>全部</MenuItem>
                {banks.map((b) => <MenuItem key={b.id} value={b.id}>{b.bankName}</MenuItem>)}
              </TextField>
            </Box>
            <TableContainer sx={{ maxHeight: 300 }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>ID</TableCell>
                    <TableCell>题型</TableCell>
                    <TableCell>内容</TableCell>
                    <TableCell>分值</TableCell>
                    <TableCell>操作</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {availableQuestions.map((q) => {
                    const selected = selectedQuestions.some((s) => s.questionId === q.id);
                    return (
                      <TableRow key={q.id}>
                        <TableCell>{q.id}</TableCell>
                        <TableCell>{QuestionTypes.find((t) => t.value === q.questionType)?.label}</TableCell>
                        <TableCell sx={{ maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{q.content}</TableCell>
                        <TableCell>{q.score}</TableCell>
                        <TableCell>
                          <Button size="small" variant={selected ? 'outlined' : 'contained'} disabled={selected} onClick={() => addQuestion(q)}>
                            {selected ? '已选' : '选择'}
                          </Button>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>

          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>已选题目 (总分: {totalScore})</Typography>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>题目ID</TableCell>
                    <TableCell>分值</TableCell>
                    <TableCell>操作</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {selectedQuestions.map((s) => (
                    <TableRow key={s.questionId}>
                      <TableCell>{s.questionId}</TableCell>
                      <TableCell>
                        <TextField type="number" size="small" value={s.score} onChange={(e) => updateScore(s.questionId, Number(e.target.value))} sx={{ width: 80 }} />
                      </TableCell>
                      <TableCell>
                        <IconButton size="small" color="error" onClick={() => removeQuestion(s.questionId)}><DeleteIcon /></IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </>
      ) : (
        /* 随机组卷 */
        <Paper sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
            <Typography variant="h6">组卷规则 (总分: {totalScore})</Typography>
            <Button variant="contained" startIcon={<AddIcon />} onClick={addRule}>添加规则</Button>
          </Box>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>题型</TableCell>
                  <TableCell>难度</TableCell>
                  <TableCell>题目数量</TableCell>
                  <TableCell>每题分值</TableCell>
                  <TableCell>小计</TableCell>
                  <TableCell>操作</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {rules.map((r, idx) => (
                  <TableRow key={idx}>
                    <TableCell>
                      <Select size="small" value={r.questionType} onChange={(e) => updateRule(idx, 'questionType', Number(e.target.value))}>
                        {QuestionTypes.map((t) => <MenuItem key={t.value} value={t.value}>{t.label}</MenuItem>)}
                      </Select>
                    </TableCell>
                    <TableCell>
                      <Select size="small" value={r.difficulty} onChange={(e) => updateRule(idx, 'difficulty', Number(e.target.value))}>
                        {Difficulties.map((d) => <MenuItem key={d.value} value={d.value}>{d.label}</MenuItem>)}
                      </Select>
                    </TableCell>
                    <TableCell>
                      <TextField type="number" size="small" value={r.questionCount} onChange={(e) => updateRule(idx, 'questionCount', Number(e.target.value))} sx={{ width: 80 }} />
                    </TableCell>
                    <TableCell>
                      <TextField type="number" size="small" value={r.scorePerQuestion} onChange={(e) => updateRule(idx, 'scorePerQuestion', Number(e.target.value))} sx={{ width: 80 }} />
                    </TableCell>
                    <TableCell>{r.questionCount * r.scorePerQuestion}</TableCell>
                    <TableCell>
                      <IconButton size="small" color="error" onClick={() => removeRule(idx)}><DeleteIcon /></IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}

      <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
        <Button variant="outlined" onClick={() => navigate('/papers')}>取消</Button>
        <Button variant="contained" onClick={handleSubmit}>创建试卷</Button>
      </Box>

      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default PaperCreate;
