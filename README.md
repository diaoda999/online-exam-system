# Online Exam System (在线考试系统)

高校在线学习平台 - 在线考试子系统，支持教师出题组卷、学生在线答题、自动/手动批改等完整考试流程。

## 技术栈

### 后端
- **Java 21** + **Spring Boot 3.2.5**
- **MyBatis-Plus 3.5.6**（ORM + 分页 + 自动填充）
- **MySQL 8.0**（主数据库）
- **Redis 7**（考试状态缓存、Token 存储、答题进度同步）
- **RabbitMQ 3.13**（异步阅卷消息队列）
- **JWT (jjwt 0.12.5)**（用户认证）
- **BCrypt**（密码加密）

### 前端
- **React 18** + **Vite**
- **MUI (Material UI)** + **Tailwind CSS**
- **React Router v6** + **Axios**

## 核心功能

| 模块 | 功能 |
|------|------|
| 用户管理 | 注册、登录、角色权限（管理员/教师/学生） |
| 课程管理 | 课程CRUD、班级管理、学生关联 |
| 题库管理 | 单选题/多选题/判断题/填空题/简答题 |
| 试卷管理 | 手工组卷、随机组卷（按规则抽题） |
| 考试流程 | 创建→发布→进入→答题→提交→批改→查分 |
| 批改系统 | 客观题自动批改、主观题手动批改 |
| 实时缓存 | Redis 缓存答题进度、考试 Token 验证 |
| 异步阅卷 | RabbitMQ 消息队列异步触发客观题批改 |

## 快速开始

### 环境要求
- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 7+
- RabbitMQ 3.13+
- Node.js 18+

### 数据库初始化
```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS exam_system DEFAULT CHARSET utf8mb4;"

# 2. 执行建表脚本（按顺序）
mysql -u root -p exam_system < sql/init.sql
mysql -u root -p exam_system < sql/patch_t03_t04.sql
mysql -u root -p exam_system < sql/patch_api_fix.sql
```

### Docker 快速启动（MySQL + Redis + RabbitMQ）
```bash
# MySQL
docker run -d --name exam-mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=root123 mysql:8.0

# Redis
docker run -d --name exam-redis -p 6379:6379 redis:7 redis-server --requirepass redis123

# RabbitMQ
docker run -d --name exam-rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.13-management
```

### 后端启动
```bash
cd online_exam_system
mvn spring-boot:run
```

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

## API 接口概览

| 模块 | 接口 | 方法 |
|------|------|------|
| 用户 | `/api/user/register` | POST |
| 用户 | `/api/user/login` | POST |
| 课程 | `/api/course` | POST/GET |
| 班级 | `/api/class` | POST/GET |
| 题目 | `/api/question` | POST/GET |
| 试卷 | `/api/paper` | POST/GET |
| 考试 | `/api/exam` | POST |
| 考试 | `/api/exam/{id}/enter` | POST |
| 考试 | `/api/exam/submit` | POST |
| 批改 | `/api/exam-record/{examId}/grade-objective` | POST |
| 批改 | `/api/exam-record/grade-subjective` | POST |
| 成绩 | `/api/exam/{id}/record` | GET |

## 项目结构

```
online_exam_system/
├── src/main/java/com/exam/
│   ├── common/          # 通用工具（Result、常量、异常）
│   ├── config/          # 配置类（Redis、RabbitMQ、拦截器）
│   ├── controller/      # REST 控制器
│   ├── interceptor/     # JWT + 考试Token 拦截器
│   ├── model/
│   │   ├── dto/         # 请求 DTO
│   │   ├── entity/      # 数据库实体
│   │   └── vo/          # 响应 VO
│   ├── mapper/          # MyBatis Mapper 接口
│   └── service/         # 业务逻辑 + 实现
├── src/main/resources/
│   ├── mapper/          # MyBatis XML
│   └── application.yml  # 配置文件
├── frontend/            # React 前端
├── sql/                 # 数据库脚本
└── pom.xml
```

## License

MIT
