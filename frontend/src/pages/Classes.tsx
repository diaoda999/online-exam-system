import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, Typography, IconButton, Snackbar, Alert, Chip,
} from '@mui/material';
import { DataGrid, type GridColDef, type GridPaginationModel } from '@mui/x-data-grid';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, People as PeopleIcon } from '@mui/icons-material';
import { listClasses, createClass, updateClass, deleteClass, getClass } from '../api/class';
import { listCourses } from '../api/course';
import type { ClassVO, ClassDetailVO, ClassCreateRequest, CourseVO } from '../types';

const Classes: React.FC = () => {
  const [rows, setRows] = useState<ClassVO[]>([]);
  const [total, setTotal] = useState(0);
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<ClassVO | null>(null);
  const [form, setForm] = useState({ className: '', courseId: 0 });
  const [courses, setCourses] = useState<CourseVO[]>([]);

  const [detailOpen, setDetailOpen] = useState(false);
  const [detail, setDetail] = useState<ClassDetailVO | null>(null);

  const fetchData = useCallback(async () => {
    try {
      const data = await listClasses({ page: paginationModel.page + 1, size: paginationModel.pageSize });
      setRows(data.records);
      setTotal(data.total);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  }, [paginationModel]);

  const fetchCourses = useCallback(async () => {
    try {
      const data = await listCourses({ page: 1, size: 100 });
      setCourses(data.records);
    } catch { /* ignore */ }
  }, []);

  useEffect(() => { fetchData(); fetchCourses(); }, [fetchData, fetchCourses]);

  const openCreate = () => {
    setEditing(null);
    setForm({ className: '', courseId: 0 });
    setDialogOpen(true);
  };

  const openEdit = (cls: ClassVO) => {
    setEditing(cls);
    setForm({ className: cls.className, courseId: cls.courseId });
    setDialogOpen(true);
  };

  const handleSave = async () => {
    try {
      if (editing) {
        await updateClass(editing.id, { className: form.className, courseId: form.courseId });
        setSnackbar({ open: true, message: '更新成功', severity: 'success' });
      } else {
        const req: ClassCreateRequest = { className: form.className, courseId: form.courseId };
        await createClass(req);
        setSnackbar({ open: true, message: '创建成功', severity: 'success' });
      }
      setDialogOpen(false);
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('确定删除该班级？')) return;
    try {
      await deleteClass(id);
      setSnackbar({ open: true, message: '删除成功', severity: 'success' });
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handleViewDetail = async (id: number) => {
    try {
      const data = await getClass(id);
      setDetail(data);
      setDetailOpen(true);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 80 },
    { field: 'className', headerName: '班级名称', width: 180 },
    { field: 'courseName', headerName: '所属课程', width: 180 },
    { field: 'studentCount', headerName: '学生数', width: 100 },
    { field: 'createTime', headerName: '创建时间', width: 180 },
    {
      field: 'actions', headerName: '操作', width: 180, renderCell: (params) => (
        <Box>
          <IconButton size="small" onClick={() => handleViewDetail(params.row.id)} title="查看学生"><PeopleIcon /></IconButton>
          <IconButton size="small" onClick={() => openEdit(params.row)}><EditIcon /></IconButton>
          <IconButton size="small" color="error" onClick={() => handleDelete(params.row.id)}><DeleteIcon /></IconButton>
        </Box>
      ),
    },
  ];

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>创建班级</Button>
      </Box>

      <Box sx={{ height: 600 }}>
        <DataGrid rows={rows} columns={columns} rowCount={total} paginationModel={paginationModel} onPaginationModelChange={setPaginationModel} paginationMode="server" pageSizeOptions={[5, 10, 20]} />
      </Box>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? '编辑班级' : '创建班级'}</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <TextField fullWidth margin="dense" label="班级名称" value={form.className} onChange={(e) => setForm({ ...form, className: e.target.value })} />
          <TextField fullWidth margin="dense" label="所属课程" select value={form.courseId} onChange={(e) => setForm({ ...form, courseId: Number(e.target.value) })}>
            {courses.map((c) => <MenuItem key={c.id} value={c.id}>{c.courseName}</MenuItem>)}
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleSave}>保存</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={detailOpen} onClose={() => setDetailOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{detail?.className} - 学生列表</DialogTitle>
        <DialogContent>
          {detail?.students && detail.students.length > 0 ? (
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {detail.students.map((s) => (
                <Chip key={s.id} label={`${s.realName} (${s.username})`} />
              ))}
            </Box>
          ) : (
            <Typography>暂无学生</Typography>
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

export default Classes;
