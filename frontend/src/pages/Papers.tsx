import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, IconButton, Snackbar, Alert, Typography, Chip,
} from '@mui/material';
import { DataGrid, type GridColDef, type GridPaginationModel } from '@mui/x-data-grid';
import { Add as AddIcon, Delete as DeleteIcon, Visibility as ViewIcon } from '@mui/icons-material';
import { listPapers, createPaper, deletePaper, getPaper } from '../api/paper';
import type { PaperVO, PaperDetailVO, PaperCreateRequest } from '../types';
import { useNavigate } from 'react-router-dom';

const Papers: React.FC = () => {
  const navigate = useNavigate();
  const [rows, setRows] = useState<PaperVO[]>([]);
  const [total, setTotal] = useState(0);
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });

  const [detailOpen, setDetailOpen] = useState(false);
  const [detail, setDetail] = useState<PaperDetailVO | null>(null);

  const fetchData = useCallback(async () => {
    try {
      const data = await listPapers({ page: paginationModel.page + 1, size: paginationModel.pageSize });
      setRows(data.records);
      setTotal(data.total);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  }, [paginationModel]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleDelete = async (id: number) => {
    if (!window.confirm('确定删除该试卷？')) return;
    try {
      await deletePaper(id);
      setSnackbar({ open: true, message: '删除成功', severity: 'success' });
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handleViewDetail = async (id: number) => {
    try {
      const data = await getPaper(id);
      setDetail(data);
      setDetailOpen(true);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 80 },
    { field: 'paperName', headerName: '试卷名称', width: 200 },
    { field: 'paperType', headerName: '组卷方式', width: 100, renderCell: (params) => params.value === 1 ? '手工组卷' : '随机组卷' },
    { field: 'totalScore', headerName: '总分', width: 80 },
    { field: 'questionCount', headerName: '题目数', width: 80 },
    { field: 'creatorName', headerName: '创建者', width: 120 },
    { field: 'createTime', headerName: '创建时间', width: 180 },
    {
      field: 'actions', headerName: '操作', width: 150, renderCell: (params) => (
        <Box>
          <IconButton size="small" onClick={() => handleViewDetail(params.row.id)} title="查看详情"><ViewIcon /></IconButton>
          <IconButton size="small" color="error" onClick={() => handleDelete(params.row.id)}><DeleteIcon /></IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate('/papers/create')}>
          创建试卷
        </Button>
      </Box>

      <Box sx={{ height: 600 }}>
        <DataGrid rows={rows} columns={columns} rowCount={total} paginationModel={paginationModel} onPaginationModelChange={setPaginationModel} paginationMode="server" pageSizeOptions={[5, 10, 20]} />
      </Box>

      <Dialog open={detailOpen} onClose={() => setDetailOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>{detail?.paperName} - 试卷详情</DialogTitle>
        <DialogContent dividers>
          <Typography variant="subtitle1" gutterBottom>
            组卷方式: {detail?.paperType === 1 ? '手工组卷' : '随机组卷'} | 总分: {detail?.totalScore}
          </Typography>

          {detail?.paperType === 2 && detail.rules && detail.rules.length > 0 && (
            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle2">组卷规则:</Typography>
              {detail.rules.map((r, idx) => (
                <Chip key={idx} label={`题型${r.questionType} 难度${r.difficulty} ${r.questionCount}题 × ${r.scorePerQuestion}分`} sx={{ m: 0.5 }} />
              ))}
            </Box>
          )}

          {detail?.questions && detail.questions.length > 0 && (
            <Box>
              <Typography variant="subtitle2" gutterBottom>题目列表:</Typography>
              {detail.questions.map((q, idx) => (
                <Box key={q.id} sx={{ mb: 1, p: 1.5, bgcolor: '#f9f9f9', borderRadius: 1 }}>
                  <Typography variant="body2">{idx + 1}. {q.content} <Chip label={`${q.score}分`} size="small" /></Typography>
                </Box>
              ))}
            </Box>
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

export default Papers;
