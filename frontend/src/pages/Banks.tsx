import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, IconButton, Snackbar, Alert, Typography, Chip,
} from '@mui/material';
import { DataGrid, type GridColDef, type GridPaginationModel } from '@mui/x-data-grid';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, Visibility as ViewIcon } from '@mui/icons-material';
import { listBanks, createBank, updateBank, deleteBank, getBank } from '../api/bank';
import type { BankVO, BankDetailVO, BankCreateRequest } from '../types';

const Banks: React.FC = () => {
  const [rows, setRows] = useState<BankVO[]>([]);
  const [total, setTotal] = useState(0);
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [keyword, setKeyword] = useState('');
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<BankVO | null>(null);
  const [form, setForm] = useState<BankCreateRequest>({ bankName: '', description: '' });

  const [detailOpen, setDetailOpen] = useState(false);
  const [detail, setDetail] = useState<BankDetailVO | null>(null);

  const fetchData = useCallback(async () => {
    try {
      const data = await listBanks({ keyword: keyword || undefined, page: paginationModel.page + 1, size: paginationModel.pageSize });
      setRows(data.records);
      setTotal(data.total);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  }, [paginationModel, keyword]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const openCreate = () => {
    setEditing(null);
    setForm({ bankName: '', description: '' });
    setDialogOpen(true);
  };

  const openEdit = (bank: BankVO) => {
    setEditing(bank);
    setForm({ bankName: bank.bankName, description: bank.description });
    setDialogOpen(true);
  };

  const handleSave = async () => {
    try {
      if (editing) {
        await updateBank(editing.id, form);
        setSnackbar({ open: true, message: '更新成功', severity: 'success' });
      } else {
        await createBank(form);
        setSnackbar({ open: true, message: '创建成功', severity: 'success' });
      }
      setDialogOpen(false);
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('确定删除该题库？')) return;
    try {
      await deleteBank(id);
      setSnackbar({ open: true, message: '删除成功', severity: 'success' });
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handleViewDetail = async (id: number) => {
    try {
      const data = await getBank(id);
      setDetail(data);
      setDetailOpen(true);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 80 },
    { field: 'bankName', headerName: '题库名称', width: 200 },
    { field: 'description', headerName: '描述', width: 250, renderCell: (params) => params.value || '-' },
    { field: 'questionCount', headerName: '题目数', width: 100 },
    { field: 'creatorName', headerName: '创建者', width: 120 },
    { field: 'createTime', headerName: '创建时间', width: 180 },
    {
      field: 'actions', headerName: '操作', width: 180, renderCell: (params) => (
        <Box>
          <IconButton size="small" onClick={() => handleViewDetail(params.row.id)} title="查看题目"><ViewIcon /></IconButton>
          <IconButton size="small" onClick={() => openEdit(params.row)}><EditIcon /></IconButton>
          <IconButton size="small" color="error" onClick={() => handleDelete(params.row.id)}><DeleteIcon /></IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
        <TextField size="small" label="搜索" value={keyword} onChange={(e) => setKeyword(e.target.value)} sx={{ width: 250 }} />
        <Button variant="contained" onClick={fetchData}>搜索</Button>
        <Box sx={{ flex: 1 }} />
        <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>创建题库</Button>
      </Box>

      <Box sx={{ height: 600 }}>
        <DataGrid rows={rows} columns={columns} rowCount={total} paginationModel={paginationModel} onPaginationModelChange={setPaginationModel} paginationMode="server" pageSizeOptions={[5, 10, 20]} />
      </Box>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? '编辑题库' : '创建题库'}</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <TextField fullWidth margin="dense" label="题库名称" value={form.bankName} onChange={(e) => setForm({ ...form, bankName: e.target.value })} />
          <TextField fullWidth margin="dense" label="描述" multiline rows={2} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleSave}>保存</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={detailOpen} onClose={() => setDetailOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>{detail?.bankName} - 题目列表 ({detail?.questionCount || 0}题)</DialogTitle>
        <DialogContent dividers>
          {detail?.questions && detail.questions.length > 0 ? (
            detail.questions.map((q, idx) => (
              <Box key={q.id} sx={{ mb: 2, p: 2, bgcolor: '#f9f9f9', borderRadius: 1 }}>
                <Typography variant="body2" color="text.secondary">#{idx + 1} · {q.subject}</Typography>
                <Typography variant="body1">{q.content}</Typography>
                <Box sx={{ mt: 1, display: 'flex', gap: 1 }}>
                  <Chip label={q.questionType === 1 ? '单选' : q.questionType === 2 ? '多选' : q.questionType === 3 ? '判断' : q.questionType === 4 ? '填空' : '简答'} size="small" />
                  <Chip label={q.difficulty === 1 ? '简单' : q.difficulty === 2 ? '中等' : '困难'} size="small" color={q.difficulty === 3 ? 'error' : q.difficulty === 2 ? 'warning' : 'success'} />
                </Box>
              </Box>
            ))
          ) : (
            <Typography>暂无题目</Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailOpen(false)}>关闭</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default Banks;
