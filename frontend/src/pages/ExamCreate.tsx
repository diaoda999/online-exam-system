import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, Snackbar, Alert,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { listExams, createExam } from '../api/exam';
import { listPapers } from '../api/paper';
import { listClasses } from '../api/class';
import type { ExamCreateRequest, PaperVO, ClassVO } from '../types';

const ExamCreate: React.FC = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState<ExamCreateRequest>({
    examName: '', paperId: 0, classId: 0, startTime: '', endTime: '', duration: 120,
  });
  const [papers, setPapers] = useState<PaperVO[]>([]);
  const [classes, setClasses] = useState<ClassVO[]>([]);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });

  useEffect(() => {
    const fetchOptions = async () => {
      try {
        const [pData, cData] = await Promise.all([
          listPapers({ page: 1, size: 100 }),
          listClasses({ page: 1, size: 100 }),
        ]);
        setPapers(pData.records);
        setClasses(cData.records);
      } catch { /* ignore */ }
    };
    fetchOptions();
  }, []);

  const handleSubmit = async () => {
    try {
      await createExam(form);
      setSnackbar({ open: true, message: '创建成功', severity: 'success' });
      setTimeout(() => navigate('/exams'), 1000);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  return (
    <Box sx={{ maxWidth: 600, mx: 'auto', mt: 3 }}>
      <Box sx={{ mb: 3 }}>
        <Button onClick={() => navigate('/exams')}>← 返回考试列表</Button>
      </Box>
      <TextField fullWidth margin="normal" label="考试名称" value={form.examName} onChange={(e) => setForm({ ...form, examName: e.target.value })} />
      <TextField fullWidth margin="normal" select label="试卷" value={form.paperId} onChange={(e) => setForm({ ...form, paperId: Number(e.target.value) })}>
        {papers.map((p) => <MenuItem key={p.id} value={p.id}>{p.paperName}</MenuItem>)}
      </TextField>
      <TextField fullWidth margin="normal" select label="班级" value={form.classId} onChange={(e) => setForm({ ...form, classId: Number(e.target.value) })}>
        {classes.map((c) => <MenuItem key={c.id} value={c.id}>{c.className}</MenuItem>)}
      </TextField>
      <TextField fullWidth margin="normal" label="开始时间" type="datetime-local" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} InputLabelProps={{ shrink: true }} />
      <TextField fullWidth margin="normal" label="结束时间" type="datetime-local" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} InputLabelProps={{ shrink: true }} />
      <TextField fullWidth margin="normal" label="考试时长(分钟)" type="number" value={form.duration} onChange={(e) => setForm({ ...form, duration: Number(e.target.value) })} />
      <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
        <Button variant="outlined" onClick={() => navigate('/exams')}>取消</Button>
        <Button variant="contained" onClick={handleSubmit}>创建考试</Button>
      </Box>
      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default ExamCreate;
