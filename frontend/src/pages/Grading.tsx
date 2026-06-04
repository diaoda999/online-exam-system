import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Button, Typography, IconButton, Snackbar, Alert, TextField, MenuItem, Chip,
} from '@mui/material';
import { DataGrid, type GridColDef, type GridPaginationModel } from '@mui/x-data-grid';
import { Visibility as ViewIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { listRecords } from '../api/examRecord';
import { listExams } from '../api/exam';
import type { ExamRecordVO, ExamVO, RecordStatus } from '../types';
import { RecordStatusLabels } from '../types';

const Grading: React.FC = () => {
  const navigate = useNavigate();
  const [exams, setExams] = useState<ExamVO[]>([]);
  const [selectedExamId, setSelectedExamId] = useState<number>(0);
  const [rows, setRows] = useState<ExamRecordVO[]>([]);
  const [total, setTotal] = useState(0);
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [statusFilter, setStatusFilter] = useState('');
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });

  useEffect(() => {
    const fetchExams = async () => {
      try {
        const data = await listExams({ page: 1, size: 100 });
        setExams(data.records);
      } catch { /* ignore */ }
    };
    fetchExams();
  }, []);

  const fetchData = useCallback(async () => {
    if (!selectedExamId) return;
    try {
      const data = await listRecords({
        examId: selectedExamId,
        status: statusFilter || undefined,
        page: paginationModel.page + 1,
        size: paginationModel.pageSize,
      });
      setRows(data.records);
      setTotal(data.total);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  }, [selectedExamId, paginationModel, statusFilter]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const statusColor = (status: RecordStatus) => {
    switch (status) {
      case 'STARTED': return 'warning';
      case 'SUBMITTED': return 'info';
      case 'GRADED': return 'success';
      default: return 'default';
    }
  };

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 80 },
    { field: 'username', headerName: '用户名', width: 120 },
    { field: 'realName', headerName: '姓名', width: 120 },
    {
      field: 'status', headerName: '状态', width: 100,
      renderCell: (params) => <Chip label={RecordStatusLabels[params.value as RecordStatus] || params.value} color={statusColor(params.value as RecordStatus) as any} size="small" />,
    },
    { field: 'totalScore', headerName: '总分', width: 80, renderCell: (params) => params.value >= 0 ? params.value : '-' },
    { field: 'objectiveScore', headerName: '客观题', width: 80, renderCell: (params) => params.value >= 0 ? params.value : '-' },
    { field: 'subjectiveScore', headerName: '主观题', width: 80, renderCell: (params) => params.value >= 0 ? params.value : '-' },
    { field: 'submitTime', headerName: '提交时间', width: 180 },
    {
      field: 'actions', headerName: '操作', width: 120, renderCell: (params) => (
        <IconButton size="small" onClick={() => navigate(`/grading/${params.row.id}`)} title="批改详情">
          <ViewIcon />
        </IconButton>
      ),
    },
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
        <TextField select size="small" label="选择考试" value={selectedExamId} onChange={(e) => { setSelectedExamId(Number(e.target.value)); setPaginationModel({ ...paginationModel, page: 0 }); }} sx={{ width: 250 }}>
          <MenuItem value={0}>请选择</MenuItem>
          {exams.map((e) => <MenuItem key={e.id} value={e.id}>{e.examName}</MenuItem>)}
        </TextField>
        <TextField select size="small" label="状态" value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value); setPaginationModel({ ...paginationModel, page: 0 }); }} sx={{ width: 150 }}>
          <MenuItem value="">全部</MenuItem>
          <MenuItem value="SUBMITTED">已提交</MenuItem>
          <MenuItem value="GRADED">已批改</MenuItem>
        </TextField>
      </Box>

      {selectedExamId ? (
        <Box sx={{ height: 600 }}>
          <DataGrid rows={rows} columns={columns} rowCount={total} paginationModel={paginationModel} onPaginationModelChange={setPaginationModel} paginationMode="server" pageSizeOptions={[5, 10, 20]} />
        </Box>
      ) : (
        <Box sx={{ p: 4, textAlign: 'center', color: 'text.secondary' }}>
          <Typography>请先选择一个考试</Typography>
        </Box>
      )}

      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default Grading;
