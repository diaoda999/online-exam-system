import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Typography, List, ListItem, ListItemText, Chip, IconButton,
  Button, Snackbar, Alert, Card, CardContent, CardActions,
} from '@mui/material';
import { Check as CheckIcon, Close as CloseIcon } from '@mui/icons-material';
import { getMyInvitations, acceptInvitation, rejectInvitation, getMyClasses, getAllClassCourses } from '../api/classStudent';
import type { ClassStudentVO, ClassCourseVO } from '../types';

const MyInvitations: React.FC = () => {
  const [invitations, setInvitations] = useState<ClassStudentVO[]>([]);
  const [myClasses, setMyClasses] = useState<ClassStudentVO[]>([]);
  const [allClassCourses, setAllClassCourses] = useState<ClassCourseVO[]>([]);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });

  const fetchData = useCallback(async () => {
    try {
      const [invData, classData, courseData] = await Promise.all([
        getMyInvitations(),
        getMyClasses(),
        getAllClassCourses(),
      ]);
      setInvitations(invData);
      setMyClasses(classData);
      setAllClassCourses(courseData);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleAccept = async (id: number) => {
    try {
      await acceptInvitation(id);
      setSnackbar({ open: true, message: '已加入班级', severity: 'success' });
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const handleReject = async (id: number) => {
    try {
      await rejectInvitation(id);
      setSnackbar({ open: true, message: '已拒绝邀请', severity: 'info' });
      fetchData();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  };

  const pending = invitations.filter((i) => i.status === 'PENDING');
  const accepted = invitations.filter((i) => i.status === 'ACCEPTED');

  // 筛选我已加入班级的需修读课程
  const myClassIds = myClasses.map((c) => c.classId);
  const myClassCourses = allClassCourses.filter((cc) => myClassIds.includes(cc.classId));

  return (
    <Box>
      {/* 待确认邀请 */}
      <Typography variant="h6" sx={{ mb: 2 }}>
        班级邀请 {pending.length > 0 && `(${pending.length} 条待确认)`}
      </Typography>

      {pending.length === 0 ? (
        <Card sx={{ mb: 3 }}>
          <CardContent><Typography color="text.secondary">暂无待确认的班级邀请</Typography></CardContent>
        </Card>
      ) : (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mb: 4 }}>
          {pending.map((inv) => (
            <Card key={inv.id} variant="outlined">
              <CardContent sx={{ pb: 1 }}>
                <Typography variant="subtitle1">{inv.className}</Typography>
                <Typography variant="body2" color="text.secondary">邀请人：{inv.inviterName || '系统'}</Typography>
                <Typography variant="body2" color="text.secondary">邀请时间：{inv.createTime}</Typography>
              </CardContent>
              <CardActions>
                <Button size="small" variant="contained" startIcon={<CheckIcon />} onClick={() => handleAccept(inv.id)}>同意加入</Button>
                <Button size="small" variant="outlined" color="error" startIcon={<CloseIcon />} onClick={() => handleReject(inv.id)}>拒绝</Button>
              </CardActions>
            </Card>
          ))}
        </Box>
      )}

      {/* 已加入班级 */}
      <Typography variant="h6" sx={{ mb: 2 }}>已加入班级 ({myClasses.length})</Typography>
      {myClasses.length === 0 ? (
        <Card sx={{ mb: 3 }}>
          <CardContent><Typography color="text.secondary">暂无已加入的班级</Typography></CardContent>
        </Card>
      ) : (
        <List sx={{ mb: 4 }}>
          {myClasses.map((inv) => (
            <ListItem key={inv.id}>
              <ListItemText primary={inv.className} secondary={`邀请人：${inv.inviterName || '-'}`} />
              <Chip label="已加入" color="success" size="small" />
            </ListItem>
          ))}
        </List>
      )}

      {/* 班级需修读课程 */}
      <Typography variant="h6" sx={{ mb: 2 }}>我的修读课程 ({myClassCourses.length})</Typography>
      {myClassCourses.length === 0 ? (
        <Card>
          <CardContent><Typography color="text.secondary">暂无修读课程</Typography></CardContent>
        </Card>
      ) : (
        <List>
          {myClassCourses.map((cc) => (
            <ListItem key={cc.id}>
              <ListItemText
                primary={`${cc.courseName} (${cc.courseCode || '-'})`}
                secondary={`班级：${cc.className}`}
              />
              <Chip label="修读课程" color="primary" size="small" />
            </ListItem>
          ))}
        </List>
      )}

      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default MyInvitations;
