import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, IconButton, Snackbar, Alert, FormControl,
  InputLabel, Select, Grid, Tabs, Tab, Radio, Checkbox, Typography, Tooltip,
} from '@mui/material';
import { DataGrid, type GridColDef, type GridPaginationModel } from '@mui/x-data-grid';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, Close as CloseIcon } from '@mui/icons-material';
import { listQuestions, createQuestion, updateQuestion, deleteQuestion } from '../api/question';
import { listBanks } from '../api/bank';
import type { QuestionVO, QuestionType, Difficulty, BankVO } from '../types';
import { QuestionTypeLabels, QuestionTypes, DifficultyLabels, Difficulties } from '../types';

const OPTION_KEYS = ['optionA', 'optionB', 'optionC', 'optionD', 'optionE', 'optionF', 'optionG', 'optionH'] as const;
const OPTION_LABELS = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];

interface QuestionFormData {
  questionType: QuestionType;
  difficulty: Difficulty;
  subject: string;
  content: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  optionE: string;
  optionF: string;
  optionG: string;
  optionH: string;
  answer: string;
  analysis: string;
  score: number;
  bankIds: number[];
}

const emptyForm = (): QuestionFormData => ({
  questionType: 1, difficulty: 1, subject: '', content: '',
  optionA: '', optionB: '', optionC: '', optionD: '',
  optionE: '', optionF: '', optionG: '', optionH: '',
  answer: '', analysis: '', score: 5, bankIds: [],
});

const getOptionValue = (form: QuestionFormData, index: number): string => {
  return form[OPTION_KEYS[index]];
};

const setOptionValue = (form: QuestionFormData, index: number, value: string): QuestionFormData => {
  const key = OPTION_KEYS[index];
  return { ...form, [key]: value };
};

const Questions: React.FC = () => {
  const [rows, setRows] = useState<QuestionVO[]>([]);
  const [total, setTotal] = useState(0);
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [filters, setFilters] = useState({ questionType: '' as string, difficulty: '' as string, keyword: '', bankId: '' as string });
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });
  const [banks, setBanks] = useState<BankVO[]>([]);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<QuestionVO | null>(null);
  const [form, setForm] = useState<QuestionFormData>(emptyForm());
  const [optionCount, setOptionCount] = useState(4);

  const currentType = form.questionType;

  const handleTypeChange = (newType: QuestionType) => {
    setForm(prev => {
      const updated = { ...prev, questionType: newType, answer: '' };
      if (newType === 3) {
        updated.answer = 'true';
      }
      return updated;
    });
  };

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
    setForm(emptyForm());
    setOptionCount(4);
    setDialogOpen(true);
  };

  const openEdit = (q: any) => {
    setEditing(q);
    const f: QuestionFormData = {
      questionType: q.questionType, difficulty: q.difficulty, subject: q.subject || '',
      content: q.content, optionA: q.optionA || '', optionB: q.optionB || '',
      optionC: q.optionC || '', optionD: q.optionD || '',
      optionE: q.optionE || '', optionF: q.optionF || '',
      optionG: q.optionG || '', optionH: q.optionH || '',
      answer: q.answer || '', analysis: q.analysis || '', score: q.score || 5, bankIds: [],
    };
    if ((q.questionType === 1 || q.questionType === 2)) {
      let count = 0;
      for (let i = 0; i < 8; i++) {
        if (f[OPTION_KEYS[i]]) count = i + 1;
      }
      setOptionCount(Math.max(count, 2));
    }
    setForm(f);
    setDialogOpen(true);
  };

  const handleSave = async () => {
    try {
      if (editing) {
        await updateQuestion(editing.id, form);
        setSnackbar({ open: true, message: '更新成功', severity: 'success' });
      } else {
        await createQuestion(form as any);
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

  const handleSingleCorrectChange = (label: string) => {
    setForm(prev => ({ ...prev, answer: label }));
  };

  const handleMultiCorrectChange = (label: string) => {
    const selected = form.answer.split(',').filter(Boolean);
    let updated: string[];
    if (selected.includes(label)) {
      updated = selected.filter(s => s !== label);
    } else {
      updated = [...selected, label].sort();
    }
    setForm(prev => ({ ...prev, answer: updated.join(',') }));
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

  const renderTypeSpecificForm = () => {
    if (currentType === 1 || currentType === 2) {
      const isMulti = currentType === 2;
      const selectedAnswers = isMulti ? form.answer.split(',').filter(Boolean) : [form.answer];
      return (
        <>
          <Grid item xs={12}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
              <Typography variant="body2" color="text.secondary">选项设置</Typography>
              <FormControl size="small" sx={{ minWidth: 80 }}>
                <InputLabel>选项数</InputLabel>
                <Select value={optionCount} label="选项数" onChange={(e) => setOptionCount(Number(e.target.value))}>
                  {[2, 3, 4, 5, 6, 7, 8].map(n => <MenuItem key={n} value={n}>{n}个</MenuItem>)}
                </Select>
              </FormControl>
            </Box>
          </Grid>
          {Array.from({ length: optionCount }, (_, i) => {
            const label = OPTION_LABELS[i];
            const isSelected = selectedAnswers.includes(label);
            return (
              <Grid item xs={12} key={label}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, p: 1.5, borderRadius: 2, border: '1px solid', borderColor: isSelected ? '#534AB7' : '#e0e0e0', bgcolor: isSelected ? '#EEEDFE' : 'transparent', transition: 'all 0.2s' }}>
                  <Box sx={{ width: 32, height: 32, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: isSelected ? '#534AB7' : '#AFA9EC', color: 'white', fontWeight: 600, fontSize: 14, flexShrink: 0 }}>
                    {label}
                  </Box>
                  <TextField fullWidth size="small" placeholder={`输入选项${label}的内容`} value={getOptionValue(form, i)} onChange={(e) => setForm(setOptionValue(form, i, e.target.value))} sx={{ flex: 1 }} />
                  <Tooltip title="设为正确答案">
                    {isMulti ? (
                      <Checkbox checked={isSelected} onChange={() => handleMultiCorrectChange(label)} sx={{ color: '#534AB7', '&.Mui-checked': { color: '#534AB7' } }} />
                    ) : (
                      <Radio checked={form.answer === label} onChange={() => handleSingleCorrectChange(label)} sx={{ color: '#534AB7', '&.Mui-checked': { color: '#534AB7' } }} />
                    )}
                  </Tooltip>
                </Box>
              </Grid>
            );
          })}
          {isMulti && (
            <Grid item xs={12}>
              <Typography variant="caption" color="text.secondary">
                已选正确答案：{form.answer || '未选择'}（可多选）
              </Typography>
            </Grid>
          )}
        </>
      );
    }

    if (currentType === 3) {
      return (
        <Grid item xs={12}>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>选择正确答案</Typography>
          <Box sx={{ display: 'flex', gap: 2 }}>
            <Box onClick={() => setForm(prev => ({ ...prev, answer: 'true' }))} sx={{ flex: 1, p: 3, borderRadius: 2, border: '2px solid', borderColor: form.answer === 'true' ? '#1D9E75' : '#e0e0e0', bgcolor: form.answer === 'true' ? '#E1F5EE' : 'transparent', cursor: 'pointer', textAlign: 'center', transition: 'all 0.2s', '&:hover': { borderColor: '#1D9E75' } }}>
              <Typography variant="h4" sx={{ color: form.answer === 'true' ? '#1D9E75' : '#888' }}>&#10003;</Typography>
              <Typography sx={{ mt: 1, color: form.answer === 'true' ? '#1D9E75' : '#888', fontWeight: 500 }}>正确</Typography>
            </Box>
            <Box onClick={() => setForm(prev => ({ ...prev, answer: 'false' }))} sx={{ flex: 1, p: 3, borderRadius: 2, border: '2px solid', borderColor: form.answer === 'false' ? '#D85A30' : '#e0e0e0', bgcolor: form.answer === 'false' ? '#FAECE7' : 'transparent', cursor: 'pointer', textAlign: 'center', transition: 'all 0.2s', '&:hover': { borderColor: '#D85A30' } }}>
              <Typography variant="h4" sx={{ color: form.answer === 'false' ? '#D85A30' : '#888' }}>&#10007;</Typography>
              <Typography sx={{ mt: 1, color: form.answer === 'false' ? '#D85A30' : '#888', fontWeight: 500 }}>错误</Typography>
            </Box>
          </Box>
        </Grid>
      );
    }

    if (currentType === 4) {
      return (
        <Grid item xs={12}>
          <TextField fullWidth label="参考答案（多个空用分号分隔）" multiline rows={2} value={form.answer} onChange={(e) => setForm(prev => ({ ...prev, answer: e.target.value }))} placeholder="答案1;答案2;答案3" helperText="在题目内容中用 ___ 标记空白位置，此处按顺序填写答案" />
        </Grid>
      );
    }

    if (currentType === 5) {
      return (
        <Grid item xs={12}>
          <TextField fullWidth label="参考答案" multiline rows={4} value={form.answer} onChange={(e) => setForm(prev => ({ ...prev, answer: e.target.value }))} placeholder="输入参考答案..." />
        </Grid>
      );
    }

    return null;
  };

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

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="md" fullWidth PaperProps={{ sx: { maxHeight: '90vh' } }}>
        <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', pb: 0 }}>
          {editing ? '编辑题目' : '创建题目'}
          <IconButton size="small" onClick={() => setDialogOpen(false)}><CloseIcon /></IconButton>
        </DialogTitle>
        <DialogContent sx={{ pt: 1 }}>
          <Tabs
            value={currentType - 1}
            onChange={(_, v) => handleTypeChange((v + 1) as QuestionType)}
            variant="scrollable"
            scrollButtons="auto"
            sx={{
              mb: 2, mt: 1,
              '& .MuiTab-root': { minWidth: 80, fontWeight: 500 },
              '& .Mui-selected': { color: '#534AB7' },
              '& .MuiTabs-indicator': { backgroundColor: '#534AB7' },
            }}
          >
            {QuestionTypes.map((t) => <Tab key={t.value} label={t.label} />)}
          </Tabs>

          <Grid container spacing={2}>
            <Grid item xs={4}>
              <TextField fullWidth select size="small" label="难度" value={form.difficulty} onChange={(e) => setForm({ ...form, difficulty: Number(e.target.value) as Difficulty })}>
                {Difficulties.map((d) => <MenuItem key={d.value} value={d.value}>{d.label}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={4}>
              <TextField fullWidth size="small" label="科目" value={form.subject} onChange={(e) => setForm({ ...form, subject: e.target.value })} placeholder="如：Java基础" />
            </Grid>
            <Grid item xs={4}>
              <TextField fullWidth size="small" label="分值" type="number" value={form.score} onChange={(e) => setForm({ ...form, score: Number(e.target.value) })} />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label="题目内容" multiline rows={3} value={form.content} onChange={(e) => setForm({ ...form, content: e.target.value })} placeholder={currentType === 4 ? '用 ___ 标记空白位置，如：Java是一种___语言' : '请输入题目内容'} />
            </Grid>
            {renderTypeSpecificForm()}
            <Grid item xs={12}>
              <TextField fullWidth label="解析（选填）" multiline rows={2} value={form.analysis} onChange={(e) => setForm({ ...form, analysis: e.target.value })} placeholder="输入题目解析..." />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setDialogOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleSave} sx={{ bgcolor: '#534AB7', '&:hover': { bgcolor: '#3C3489' } }}>保存</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default Questions;
