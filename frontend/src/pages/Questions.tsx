import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, IconButton, Snackbar, Alert, FormControl,
  InputLabel, Select, Grid,
} from '@mui/material';
import { DataGrid, type GridColDef, type GridPaginationModel } from '@mui/x-data-grid';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { listQuestions, createQuestion, updateQuestion, deleteQuestion } from '../api/question';
import { listBanks } from '../api/bank';
import type { QuestionVO, QuestionType, Difficulty, QuestionCreateRequest, BankVO } from '../types';
import { QuestionTypeLabels, QuestionTypes, DifficultyLabels, Difficulties } from '../types';

const Questions: React.FC = () => {
  const [rows, setRows] = useState<QuestionVO[]>([]);
  const [total, setTotal] = useState(0);
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [filters, setFilters] = useState({ questionType: '' as string, difficulty: '' as string, keyword: '', bankId: '' as string });
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });
  const [banks, setBanks] = useState<BankVO[]>([]);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<QuestionVO | null>(null);
  const [form, setForm] = useState<QuestionCreateRequest>({
    questionType: 1, difficulty: 1, subject: '', content: '', options: '',
    correctAnswer: '', analysis: '', score: 5, bankIds: [],
  });

  const fetchData = useCallback(async () => {
    try {
      const data = await listQuestions({
        questionType: filters.questionType ? Number(filters.questionType) : undefined,
        difficulty: filters.difficulty ? Number(filters.difficulty) : undefined,
        keyword: filters.keyword || undefined,
        bankId: filters.bankId ? Number(filters.bankId) : undefined,
        page: paginationModel.page + 1,
        size: paginationModel.pageSize,
      });
      setRows(data.records);
      setTotal(data.total);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  }, [paginationModel, filters]);

  const fetchBanks = useCallback(async () => {
    try {
      const data = await listBanks({ page: 1, size: 100 });
      setBanks(data.records);
    } catch { /* ignore */ }
  }, []);

  useEffect(() => { fetchData(); fetchBanks(); }, [fetchData, fetchBanks]);

  const openCreate = () => {
    setEditing(null);
    setForm({ questionType: 1, difficulty: 1, subject: '', content: '', options: '', correctAnswer: '', analysis: '', score: 5, bankIds: [] });
    setDialogOpen(true);
  };

  const openEdit = (q: QuestionVO) => {
    setEditing(q);
    setForm({
      questionType: q.questionType, difficulty: q.difficulty, subject: q.subject,
      content: q.content, options: q.options || '', correctAnswer: q.correctAnswer,
      analysis: q.analysis || '', score: q.score, bankIds: [],
    });
    setDialogOpen(true);
  };

  const handleSave = async () => {
    try {
      if (editing) {
        await updateQuestion(editing.id, form);
        setSnackbar({ open: true, message: '更新成功', severity: 'success' });
      } else {
        await createQuestion(form);
        setSnackbar({ open: true, message: '创建成功', severity: 'success' });
      }
      setDialogOpen(false);
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('确定删除该题目？')) return;
    try {
      await deleteQuestion(id);
      setSnackbar({ open: true, message: '删除成功', severity: 'success' });
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 70 },
    { field: 'questionType', headerName: '题型', width: 80, renderCell: (params) => QuestionTypeLabels[params.value as QuestionType] || params.value },
    { field: 'difficulty', headerName: '难度', width: 70, renderCell: (params) => DifficultyLabels[params.value as Difficulty] || params.value },
    { field: 'subject', headerName: '科目', width: 100 },
    { field: 'content', headerName: '题目内容', width: 300, renderCell: (params) => <Box sx={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{params.value}</Box> },
    { field: 'score', headerName: '分值', width: 70 },
    { field: 'bankNames', headerName: '所属题库', width: 150, renderCell: (params) => (params.value || []).join(', ') || '-' },
    {
      field: 'actions', headerName: '操作', width: 120, renderCell: (params) => (
        <Box>
          <IconButton size="small" onClick={() => openEdit(params.row)}><EditIcon /></IconButton>
          <IconButton size="small" color="error" onClick={() => handleDelete(params.row.id)}><DeleteIcon /></IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', gap: 2, mb: 2, flexWrap: 'wrap' }}>
        <TextField select size="small" label="题型" value={filters.questionType} onChange={(e) => { setFilters({ ...filters, questionType: e.target.value }); setPaginationModel({ ...paginationModel, page: 0 }); }} sx={{ width: 120 }}>
          <MenuItem value="">全部</MenuItem>
          {QuestionTypes.map((t) => <MenuItem key={t.value} value={t.value}>{t.label}</MenuItem>)}
        </TextField>
        <TextField select size="small" label="难度" value={filters.difficulty} onChange={(e) => { setFilters({ ...filters, difficulty: e.target.value }); setPaginationModel({ ...paginationModel, page: 0 }); }} sx={{ width: 120 }}>
          <MenuItem value="">全部</MenuItem>
          {Difficulties.map((d) => <MenuItem key={d.value} value={d.value}>{d.label}</MenuItem>)}
        </TextField>
        <TextField size="small" label="关键词" value={filters.keyword} onChange={(e) => setFilters({ ...filters, keyword: e.target.value })} sx={{ width: 200 }} />
        <TextField select size="small" label="题库" value={filters.bankId} onChange={(e) => { setFilters({ ...filters, bankId: e.target.value }); setPaginationModel({ ...paginationModel, page: 0 }); }} sx={{ width: 160 }}>
          <MenuItem value="">全部</MenuItem>
          {banks.map((b) => <MenuItem key={b.id} value={b.id}>{b.bankName}</MenuItem>)}
        </TextField>
        <Button variant="contained" onClick={fetchData}>搜索</Button>
        <Box sx={{ flex: 1 }} />
        <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>创建题目</Button>
      </Box>

      <Box sx={{ height: 600 }}>
        <DataGrid rows={rows} columns={columns} rowCount={total} paginationModel={paginationModel} onPaginationModelChange={setPaginationModel} paginationMode="server" pageSizeOptions={[5, 10, 20]} />
      </Box>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>{editing ? '编辑题目' : '创建题目'}</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <Grid container spacing={2} sx={{ mt: 0 }}>
            <Grid item xs={6}>
              <TextField fullWidth select label="题型" value={form.questionType} onChange={(e) => setForm({ ...form, questionType: Number(e.target.value) as QuestionType })}>
                {QuestionTypes.map((t) => <MenuItem key={t.value} value={t.value}>{t.label}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6}>
              <TextField fullWidth select label="难度" value={form.difficulty} onChange={(e) => setForm({ ...form, difficulty: Number(e.target.value) as Difficulty })}>
                {Difficulties.map((d) => <MenuItem key={d.value} value={d.value}>{d.label}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={6}>
              <TextField fullWidth label="科目" value={form.subject} onChange={(e) => setForm({ ...form, subject: e.target.value })} />
            </Grid>
            <Grid item xs={6}>
              <TextField fullWidth label="分值" type="number" value={form.score} onChange={(e) => setForm({ ...form, score: Number(e.target.value) })} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label="题目内容" multiline rows={3} value={form.content} onChange={(e) => setForm({ ...form, content: e.target.value })} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label="选项（JSON格式，选择题必填）" multiline rows={2} value={form.options} onChange={(e) => setForm({ ...form, options: e.target.value })} placeholder='[{"label":"A","value":"选项A"},{"label":"B","value":"选项B"}]' />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label="正确答案" value={form.correctAnswer} onChange={(e) => setForm({ ...form, correctAnswer: e.target.value })} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label="解析" multiline rows={2} value={form.analysis} onChange={(e) => setForm({ ...form, analysis: e.target.value })} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleSave}>保存</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default Questions;
