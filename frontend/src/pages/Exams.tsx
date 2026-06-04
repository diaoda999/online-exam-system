import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, IconButton, Snackbar, Alert, Chip,
} from '@mui/material';
import { DataGrid, type GridColDef, type GridPaginationModel } from '@mui/x-data-grid';
import { Add as AddIcon, Delete as DeleteIcon, PlayArrow as PublishIcon, Stop as StopIcon } from '@mui/icons-material';
import { listExams, createExam, deleteExam, publishExam, endExam } from '../api/exam';
import { listPapers } from '../api/paper';
import { listClasses } from '../api/class';
import type { ExamVO, ExamCreateRequest, PaperVO, ClassVO, ExamStatus } from '../types';
import { ExamStatusLabels } from '../types';
import { useAuth } from '../contexts/AuthContext';

const statusColors: Record<ExamStatus, 'default' | 'warning' | 'success' | 'error'> = {
  NOT_STARTED: 'default',
  IN_PROGRESS: 'warning',
  ENDED: 'success',
};

const Exams: React.FC = () => {
  const { user } = useAuth();
  const [rows, setRows] = useState<ExamVO[]>([]);
  const [total, setTotal] = useState(0);
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [statusFilter, setStatusFilter] = useState('');
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });

  const [dialogOpen, setDialogOpen] = useState(false);
  const [form, setForm] = useState<ExamCreateRequest>({
    examName: '', paperId: 0, classId: 0, startTime: '', endTime: '', duration: 120,
  });
  const [papers, setPapers] = useState<PaperVO[]>([]);
  const [classes, setClasses] = useState<ClassVO[]>([]);

  const fetchData = useCallback(async () => {
    try {
      const data = await listExams({
        creatorId: user?.roleCode === 'TEACHER' ? user.userId : undefined,
        status: statusFilter || undefined,
        page: paginationModel.page + 1,
        size: paginationModel.pageSize,
      });
      setRows(data.records);
      setTotal(data.total);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  }, [paginationModel, statusFilter, user]);

  const fetchOptions = useCallback(async () => {
    try {
      const [pData, cData] = await Promise.all([
        listPapers({ page: 1, size: 100 }),
        listClasses({ page: 1, size: 100 }),
      ]);
      setPapers(pData.records);
      setClasses(cData.records);
    } catch { /* ignore */ }
  }, []);

  useEffect(() => { fetchData(); fetchOptions(); }, [fetchData, fetchOptions]);

  const handleCreate = async () => {
    try {
      await createExam(form);
      setSnackbar({ open: true, message: '创建成功', severity: 'success' });
      setDialogOpen(false);
      setForm({ examName: '', paperId: 0, classId: 0, startTime: '', endTime: '', duration: 120 });
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('确定删除该考试？')) return;
    try {
      await deleteExam(id);
      setSnackbar({ open: true, message: '删除成功', severity: 'success' });
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handlePublish = async (id: number) => {
    if (!window.confirm('确定发布该考试？发布后学生可以参加。')) return;
    try {
      await publishExam(id);
      setSnackbar({ open: true, message: '发布成功', severity: 'success' });
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handleEnd = async (id: number) => {
    if (!window.confirm('确定结束该考试？未提交的学生将自动提交。')) return;
    try {
      await endExam(id);
      setSnackbar({ open: true, message: '考试已结束', severity: 'success' });
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 70 },
    { field: 'examName', headerName: '考试名称', width: 180 },
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
    { field: 'studentCount', headerName: '学生数', width: 80 },
    { field: 'submittedCount', headerName: '已提交', width: 80 },
    {
      field: 'actions', headerName: '操作', width: 220, renderCell: (params) => {
        const row = params.row as ExamVO;
        return (
          <Box>
            {row.status === 'NOT_STARTED' && (
              <Button size="small" variant="outlined" color="success" startIcon={<PublishIcon />} onClick={() => handlePublish(row.id)}>发布</Button>
            )}
            {row.status === 'IN_PROGRESS' && (
              <Button size="small" variant="outlined" color="error" startIcon={<StopIcon />} onClick={() => handleEnd(row.id)}>结束</Button>
            )}
            {row.status === 'NOT_STARTED' && (
              <IconButton size="small" color="error" onClick={() => handleDelete(row.id)}><DeleteIcon /></IconButton>
            )}
          </Box>
        );
      },
    },
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
        <TextField select size="small" label="状态" value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value); setPaginationModel({ ...paginationModel, page: 0 }); }} sx={{ width: 150 }}>
          <MenuItem value="">全部</MenuItem>
          <MenuItem value="NOT_STARTED">未开始</MenuItem>
          <MenuItem value="IN_PROGRESS">进行中</MenuItem>
          <MenuItem value="ENDED">已结束</MenuItem>
        </TextField>
        <Box sx={{ flex: 1 }} />
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>创建考试</Button>
      </Box>

      <Box sx={{ height: 600 }}>
        <DataGrid rows={rows} columns={columns} rowCount={total} paginationModel={paginationModel} onPaginationModelChange={setPaginationModel} paginationMode="server" pageSizeOptions={[5, 10, 20]} />
      </Box>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>创建考试</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <TextField fullWidth margin="dense" label="考试名称" value={form.examName} onChange={(e) => setForm({ ...form, examName: e.target.value })} />
          <TextField fullWidth margin="dense" select label="试卷" value={form.paperId} onChange={(e) => setForm({ ...form, paperId: Number(e.target.value) })}>
            {papers.map((p) => <MenuItem key={p.id} value={p.id}>{p.paperName}</MenuItem>)}
          </TextField>
          <TextField fullWidth margin="dense" select label="班级" value={form.classId} onChange={(e) => setForm({ ...form, classId: Number(e.target.value) })}>
            {classes.map((c) => <MenuItem key={c.id} value={c.id}>{c.className}</MenuItem>)}
          </TextField>
          <TextField fullWidth margin="dense" label="开始时间" type="datetime-local" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} InputLabelProps={{ shrink: true }} />
          <TextField fullWidth margin="dense" label="结束时间" type="datetime-local" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} InputLabelProps={{ shrink: true }} />
          <TextField fullWidth margin="dense" label="考试时长(分钟)" type="number" value={form.duration} onChange={(e) => setForm({ ...form, duration: Number(e.target.value) })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleCreate}>创建</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default Exams;
