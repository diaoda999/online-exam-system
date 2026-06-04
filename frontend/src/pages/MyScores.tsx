import React, { useEffect, useState, useCallback } from 'react';
import {
  Box, Typography, Paper, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Chip, Snackbar, Alert,
} from '@mui/material';
import { DataGrid, type GridColDef, type GridPaginationModel } from '@mui/x-data-grid';
import { listExams, getMyExamRecord } from '../api/exam';
import type { ExamVO, ExamRecordVO, RecordStatus } from '../types';
import { RecordStatusLabels } from '../types';

const MyScores: React.FC = () => {
  const [exams, setExams] = useState<ExamVO[]>([]);
  const [records, setRecords] = useState<Record<number, ExamRecordVO | null>>({});
  const [paginationModel, setPaginationModel] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' | 'warning' | 'info' });

  const fetchData = useCallback(async () => {
    try {
      const data = await listExams({ status: 'ENDED', page: paginationModel.page + 1, size: paginationModel.pageSize });
      setExams(data.records);
      // Fetch records for each exam
      const recordMap: Record<number, ExamRecordVO | null> = {};
      await Promise.all(
        data.records.map(async (exam) => {
          try {
            const record = await getMyExamRecord(exam.id);
            recordMap[exam.id] = record;
          } catch {
            recordMap[exam.id] = null;
          }
        })
      );
      setRecords(recordMap);
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: 'error' });
    }
  }, [paginationModel]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const rows = exams.map((exam) => ({
    id: exam.id,
    examName: exam.examName,
    paperName: exam.paperName,
    duration: exam.duration,
    record: records[exam.id],
  }));

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 70 },
    { field: 'examName', headerName: '考试名称', width: 200 },
    { field: 'paperName', headerName: '试卷', width: 150 },
    { field: 'duration', headerName: '时长(分)', width: 90 },
    {
      field: 'status', headerName: '状态', width: 100,
      renderCell: (params) => {
        const record = params.row.record as ExamRecordVO | null;
        if (!record) return <Chip label="未参加" color="default" size="small" />;
        return <Chip label={RecordStatusLabels[record.status as RecordStatus] || record.status} color={record.status === 'GRADED' ? 'success' : 'info'} size="small" />;
      },
    },
    {
      field: 'totalScore', headerName: '总分', width: 80,
      renderCell: (params) => {
        const record = params.row.record as ExamRecordVO | null;
        return record && record.totalScore >= 0 ? record.totalScore : '-';
      },
    },
    {
      field: 'objectiveScore', headerName: '客观题', width: 80,
      renderCell: (params) => {
        const record = params.row.record as ExamRecordVO | null;
        return record && record.objectiveScore >= 0 ? record.objectiveScore : '-';
      },
    },
    {
      field: 'subjectiveScore', headerName: '主观题', width: 80,
      renderCell: (params) => {
        const record = params.row.record as ExamRecordVO | null;
        return record && record.subjectiveScore >= 0 ? record.subjectiveScore : '-';
      },
    },
  ];

  return (
    <Box>
      <Typography variant="h5" gutterBottom>我的成绩</Typography>
      <Box sx={{ height: 600 }}>
        <DataGrid rows={rows} columns={columns} paginationModel={paginationModel} onPaginationModelChange={setPaginationModel} pageSizeOptions={[5, 10, 20]} />
      </Box>
      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar({ ...snackbar, open: false })}>
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default MyScores;
