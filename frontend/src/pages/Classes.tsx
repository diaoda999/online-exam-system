import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, Typography, IconButton, Snackbar, Alert, Chip,
  Checkbox, List, ListItem, ListItemText, ListItemIcon, Tabs, Tab,
} from '@mui/material';
import { DataGrid, type GridColDef, type GridPaginationModel } from '@mui/x-data-grid';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, People as PeopleIcon, MenuBook as MenuBookIcon, Close as CloseIcon } from '@mui/icons-material';
import { listClasses, createClass, updateClass, deleteClass, getClass } from '../api/class';
import { listCourses } from '../api/course';
import { getClassStudents, inviteStudents, removeClassStudent, getClassCourses, addCourseToClass, removeCourseFromClass } from '../api/classStudent';
import { listUsers } from '../api/user';
import type { ClassVO, ClassDetailVO, ClassCreateRequest, CourseVO, ClassStudentVO, ClassCourseVO, UserVO } from '../types';
import { useAuth } from '../contexts/AuthContext';

const Classes: React.FC = () => {
  const { user } = useAuth();
  const [rows, setRows] = useState<ClassVO[]>([]);
  const [total, setTotal] = useState(0);
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<ClassVO | null>(null);
  const [form, setForm] = useState({ className: '', courseId: 0 });
  const [courses, setCourses] = useState<CourseVO[]>([]);

  // 详情对话框
  const [detailOpen, setDetailOpen] = useState(false);
  const [detail, setDetail] = useState<ClassDetailVO | null>(null);

  // 学生管理对话框
  const [studentDialogOpen, setStudentDialogOpen] = useState(false);
  const [selectedClass, setSelectedClass] = useState<ClassVO | null>(null);
  const [classStudents, setClassStudents] = useState<ClassStudentVO[]>([]);
  const [tabValue, setTabValue] = useState(0);

  // 邀请学生对话框
  const [inviteDialogOpen, setInviteDialogOpen] = useState(false);
  const [availableStudents, setAvailableStudents] = useState<UserVO[]>([]);
  const [selectedStudentIds, setSelectedStudentIds] = useState<number[]>([]);

  // 需修读课程对话框
  const [courseDialogOpen, setCourseDialogOpen] = useState(false);
  const [classCourses, setClassCourses] = useState<ClassCourseVO[]>([]);
  const [addCourseDialogOpen, setAddCourseDialogOpen] = useState(false);
  const [selectedCourseId, setSelectedCourseId] = useState<number>(0);

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

  // 学生管理
  const openStudentManage = async (cls: ClassVO) => {
    setSelectedClass(cls);
    setTabValue(0);
    try {
      const students = await getClassStudents(cls.id);
      setClassStudents(students);
    } catch { setClassStudents([]); }
    setStudentDialogOpen(true);
  };

  const openInviteDialog = async () => {
    try {
      const data = await listUsers({ roleCode: 'STUDENT', page: 1, size: 200 });
      const existingIds = classStudents.map((cs) => cs.studentId);
      setAvailableStudents(data.records.filter((s) => !existingIds.includes(s.id)));
    } catch { setAvailableStudents([]); }
    setSelectedStudentIds([]);
    setInviteDialogOpen(true);
  };

  const handleInvite = async () => {
    if (!selectedClass || selectedStudentIds.length === 0) return;
    try {
      await inviteStudents(selectedClass.id, selectedStudentIds);
      setSnackbar({ open: true, message: `已邀请 ${selectedStudentIds.length} 名学生`, severity: 'success' });
      setInviteDialogOpen(false);
      const students = await getClassStudents(selectedClass.id);
      setClassStudents(students);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handleRemoveStudent = async (cs: ClassStudentVO) => {
    if (!selectedClass || !window.confirm(`确定移除学生 ${cs.studentName}？`)) return;
    try {
      await removeClassStudent(selectedClass.id, cs.studentId);
      setSnackbar({ open: true, message: '移除成功', severity: 'success' });
      const students = await getClassStudents(selectedClass.id);
      setClassStudents(students);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const toggleStudent = (id: number) => {
    setSelectedStudentIds((prev) => prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]);
  };

  // 需修读课程管理
  const openCourseManage = async (cls: ClassVO) => {
    setSelectedClass(cls);
    try {
      const ccs = await getClassCourses(cls.id);
      setClassCourses(ccs);
    } catch { setClassCourses([]); }
    setCourseDialogOpen(true);
  };

  const openAddCourseDialog = () => {
    const existingCourseIds = classCourses.map((cc) => cc.courseId);
    const available = courses.filter((c) => !existingCourseIds.includes(c.id));
    setCourses(available);
    setSelectedCourseId(available.length > 0 ? available[0].id : 0);
    setAddCourseDialogOpen(true);
  };

  const handleAddCourse = async () => {
    if (!selectedClass || !selectedCourseId) return;
    try {
      await addCourseToClass(selectedClass.id, selectedCourseId);
      setSnackbar({ open: true, message: '添加成功', severity: 'success' });
      setAddCourseDialogOpen(false);
      const ccs = await getClassCourses(selectedClass.id);
      setClassCourses(ccs);
      fetchCourses(); // 刷新课程列表
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handleRemoveCourse = async (cc: ClassCourseVO) => {
    if (!selectedClass || !window.confirm(`确定移除课程 ${cc.courseName}？`)) return;
    try {
      await removeCourseFromClass(selectedClass.id, cc.courseId);
      setSnackbar({ open: true, message: '移除成功', severity: 'success' });
      const ccs = await getClassCourses(selectedClass.id);
      setClassCourses(ccs);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const pendingStudents = classStudents.filter((cs) => cs.status === 'PENDING');
  const acceptedStudents = classStudents.filter((cs) => cs.status === 'ACCEPTED');

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 80 },
    { field: 'className', headerName: '班级名称', width: 180 },
    { field: 'courseName', headerName: '所属课程', width: 180 },
    { field: 'studentCount', headerName: '学生数', width: 100 },
    { field: 'createTime', headerName: '创建时间', width: 180 },
    {
      field: 'actions', headerName: '操作', width: 240, renderCell: (params) => (
        <Box>
          <IconButton size="small" onClick={() => openStudentManage(params.row)} title="学生管理"><PeopleIcon /></IconButton>
          <IconButton size="small" onClick={() => openCourseManage(params.row)} title="需修读课程"><MenuBookIcon /></IconButton>
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

      {/* 创建/编辑班级 */}
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

      {/* 学生管理对话框 */}
      <Dialog open={studentDialogOpen} onClose={() => setStudentDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>学生管理 - {selectedClass?.className}</DialogTitle>
        <DialogContent>
          <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
            <Tabs value={tabValue} onChange={(_, v) => setTabValue(v)}>
              <Tab label={`已加入 (${acceptedStudents.length})`} />
              <Tab label={`待确认 (${pendingStudents.length})`} />
            </Tabs>
          </Box>

          {tabValue === 0 && (
            <>
              <Box sx={{ mb: 2 }}>
                <Button variant="contained" startIcon={<AddIcon />} onClick={openInviteDialog}>邀请学生</Button>
              </Box>
              {acceptedStudents.length === 0 ? (
                <Typography color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>暂无已加入的学生</Typography>
              ) : (
                <List dense>
                  {acceptedStudents.map((cs) => (
                    <ListItem key={cs.id} secondaryAction={
                      <IconButton edge="end" size="small" color="error" onClick={() => handleRemoveStudent(cs)}><CloseIcon /></IconButton>
                    }>
                      <ListItemText primary={cs.studentName} secondary={`${cs.studentUsername} | 邀请人: ${cs.inviterName || '-'}`} />
                      <Chip label="已加入" color="success" size="small" sx={{ mr: 2 }} />
                    </ListItem>
                  ))}
                </List>
              )}
            </>
          )}

          {tabValue === 1 && (
            pendingStudents.length === 0 ? (
              <Typography color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>暂无待确认的邀请</Typography>
            ) : (
              <List dense>
                {pendingStudents.map((cs) => (
                  <ListItem key={cs.id} secondaryAction={
                    <IconButton edge="end" size="small" color="error" onClick={() => handleRemoveStudent(cs)}><CloseIcon /></IconButton>
                  }>
                    <ListItemText primary={cs.studentName} secondary={`${cs.studentUsername} | 邀请人: ${cs.inviterName || '-'}`} />
                    <Chip label="待确认" color="warning" size="small" sx={{ mr: 2 }} />
                  </ListItem>
                ))}
              </List>
            )
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setStudentDialogOpen(false)}>关闭</Button>
        </DialogActions>
      </Dialog>

      {/* 邀请学生对话框 */}
      <Dialog open={inviteDialogOpen} onClose={() => setInviteDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>邀请学生加入班级</DialogTitle>
        <DialogContent>
          {availableStudents.length === 0 ? (
            <Typography color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>没有可邀请的学生</Typography>
          ) : (
            <List dense sx={{ maxHeight: 400, overflow: 'auto' }}>
              {availableStudents.map((s) => (
                <ListItem key={s.id} onClick={() => toggleStudent(s.id)} sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}>
                  <ListItemIcon sx={{ minWidth: 36 }}>
                    <Checkbox checked={selectedStudentIds.includes(s.id)} size="small" />
                  </ListItemIcon>
                  <ListItemText primary={s.realName || s.username} secondary={s.username} />
                </ListItem>
              ))}
            </List>
          )}
          {selectedStudentIds.length > 0 && (
            <Typography variant="body2" color="primary" sx={{ mt: 1 }}>已选 {selectedStudentIds.length} 名学生</Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setInviteDialogOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleInvite} disabled={selectedStudentIds.length === 0}>确认邀请</Button>
        </DialogActions>
      </Dialog>

      {/* 需修读课程管理对话框 */}
      <Dialog open={courseDialogOpen} onClose={() => setCourseDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>需修读课程 - {selectedClass?.className}</DialogTitle>
        <DialogContent>
          <Box sx={{ mb: 2 }}>
            <Button variant="contained" startIcon={<AddIcon />} onClick={openAddCourseDialog}>添加课程</Button>
          </Box>
          {classCourses.length === 0 ? (
            <Typography color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>暂无修读课程</Typography>
          ) : (
            <List dense>
              {classCourses.map((cc) => (
                <ListItem key={cc.id} secondaryAction={
                  <IconButton edge="end" size="small" color="error" onClick={() => handleRemoveCourse(cc)}><CloseIcon /></IconButton>
                }>
                  <ListItemText primary={cc.courseName} secondary={`课程编码: ${cc.courseCode || '-'} | 添加人: ${cc.adderName || '-'}`} />
                  <Chip label="修读课程" color="primary" size="small" sx={{ mr: 2 }} />
                </ListItem>
              ))}
            </List>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCourseDialogOpen(false)}>关闭</Button>
        </DialogActions>
      </Dialog>

      {/* 添加课程对话框 */}
      <Dialog open={addCourseDialogOpen} onClose={() => setAddCourseDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>添加修读课程</DialogTitle>
        <DialogContent>
          {courses.length === 0 ? (
            <Typography color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>所有课程都已在班级修读列表中</Typography>
          ) : (
            <TextField fullWidth margin="dense" label="选择课程" select value={selectedCourseId} onChange={(e) => setSelectedCourseId(Number(e.target.value))}>
              {courses.map((c) => <MenuItem key={c.id} value={c.id}>{c.courseName} ({c.courseCode})</MenuItem>)}
            </TextField>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAddCourseDialogOpen(false)}>取消</Button>
          <Button variant="contained" onClick={handleAddCourse} disabled={!selectedCourseId}>确认添加</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default Classes;
