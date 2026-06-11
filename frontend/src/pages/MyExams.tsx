import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Button, Typography, Chip, Paper, Snackbar, Alert,
} from '@mui/material';
import { DataGrid, type GridColDef, type GridPaginationModel } from '@mui/x-data-grid';
import { useNavigate } from 'react-router-dom';
import { listExams } from '../api/exam';
import type { ExamVO, ExamStatus } from '../types';
import { ExamStatusLabels } from '../types';

const statusColors: Record<ExamStatus, 'default' | 'warning' | 'success' | 'error' | 'info'> = {
  NOT_STARTED: 'default',
  IN_PROGRESS: 'warning',
  ENDED: 'info',
};

const MyExams: React.FC = () => {
  const navigate = useNavigate();
  const [rows, setRows] = useState<ExamVO[]>([]);
  const [total, setTotal] = useState(0);
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });

  const fetchData = useCallback(async () => {
    try {
      // 优化：只发一次请求，不带 status 过滤，获取所有状态的考试
      const data = await listExams({ page: paginationModel.page + 1, size: paginationModel.pageSize });
      setRows(data.records);
      setTotal(data.total);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  }, [paginationModel]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleEnter = async (examId: number) => {
    if (!window.confirm('确定要进入考试吗？进入后请认真作答。')) return;
    try {
      navigate(`/exam/${examId}`);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handleViewScore = async (examId: number) => {
    navigate(`/my-scores?examId=${examId}`);
  };

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 70 },
    { field: 'examName', headerName: '考试名称', width: 200 },
    { field: 'paperName', headerName: '试卷', width: 150 },
    { field: 'className', headerName: '班级', width: 120 },
    { field: 'startTime', headerName: '开始时间', width: 160 },
    { field: 'duration', headerName: '时长(分)', width: 90 },
    {
      field: 'status', headerName: '状态', width: 100,
      renderCell: (params) => (
        <Chip label={ExamStatusLabels[params.value as ExamStatus] || params.value} color={statusColors[params.value as ExamStatus] || 'default'} size="small" />
      ),
    },
    {
      field: 'actions', headerName: '操作', width: 200, renderCell: (params) => {
        const row = params.row as ExamVO;
        return (
          <Box sx={{ display: 'flex', gap: 1 }}>
            {row.status === 'IN_PROGRESS' && (
              <Button size="small" variant="contained" onClick={() => handleEnter(row.id)}>进入考试</Button>
            )}
            {row.status === 'ENDED' && (
              <Button size="small" variant="outlined" onClick={() => handleViewScore(row.id)}>查看成绩</Button>
            )}
            {row.status === 'NOT_STARTED' && (
              <Typography variant="body2" color="text.secondary">未开始</Typography>
            )}
          </Box>
        );
      },
    },
  ];

  return (
    <Box>
      <Typography variant="h5" gutterBottom>我的考试</Typography>
      <Box sx={{ height: 600 }}>
        <DataGrid rows={rows} columns={columns} rowCount={total} paginationModel={paginationModel} onPaginationModelChange={setPaginationModel} paginationMode="server" pageSizeOptions={[5, 10, 20]} />
      </Box>
      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default MyExams;
