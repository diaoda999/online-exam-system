# 在线考试系统 — 面试准备手册

> 针对求职 AI 开发岗位，从简历项目经历出发，梳理项目亮点、技术难点与面试话术。

---

## 一、项目一句话描述（电梯演讲）

> 我独立开发了一个高校在线考试系统，覆盖教师出题组卷、学生在线答题、客观题自动批改、主观题手动批改全流程。技术栈使用 Spring Boot 3 + MyBatis-Plus + MySQL + Redis + RabbitMQ，其中 Redis 实现了考试答题进度的实时缓存与考试 Token 双重验证，RabbitMQ 实现了考试到时后未交卷学生的异步自动提交，配合死信队列保证消息可靠性。

---

## 二、项目架构总览（30秒讲解版）

```
┌──────────┐   HTTP/JWT    ┌──────────────────────┐   MyBatis-Plus   ┌─────────┐
│  React   │──────────────▶│    Spring Boot 3     │────────────────▶│  MySQL  │
│  前端    │               │  (Controller/Service) │                  │  8.0    │
└──────────┘               └──────┬───────┬───────┘                  └─────────┘
                                  │       │
                          Cache   │       │  Async Message
                                  ▼       ▼
                           ┌─────────┐  ┌───────────┐     ┌──────────┐
                           │  Redis  │  │ RabbitMQ  │────▶│ Consumer │
                           │   7     │  │   3.13    │     │ (自动提交)│
                           └─────────┘  └─────┬─────┘     └──────────┘
                                              │
                                        ┌─────▼─────┐
                                        │  死信队列  │
                                        │   (DLQ)   │
                                        └───────────┘
```

**分层架构**：Controller → Service → Mapper(MyBatis-Plus) → MySQL  
**中间件角色**：Redis 做缓存层（答题进度+Token+倒计时），RabbitMQ 做异步消息（自动提交+阅卷）

---

## 三、核心亮点（面试官最关心的 5 个点）

### 亮点 1：考试 Token 双重验证机制 ⭐

**问题**：考试过程中，学生答题、提交需要验证"这个学生确实有权限参加这场考试"，仅靠 JWT 不够（JWT 只验证身份，不验证考试会话）。

**方案**：
- **第一层**：`AuthInterceptor` 校验 JWT（`Authorization: Bearer xxx`），确认用户身份
- **第二层**：`ExamTokenInterceptor` 校验考试 Token（`X-Exam-Token: xxx`），确认考试会话合法

**实现细节**：
1. 学生进入考试时，后端生成 UUID 作为 examToken
2. Redis 双写：`exam:token:{examId}:{userId} → "examId:userId"` + `exam:token:value:{token} → "examId:userId"`
3. 为什么双写？因为提交时请求体只带 token，不知道 examId，所以需要通过 token 反查 examId
4. 拦截器校验 token 存在性 + userId 一致性（防止 Token 被他人盗用）
5. Token TTL = 考试时长 + 5 分钟缓冲，自然过期

**面试话术**：
> "我设计了考试 Token 双重验证机制。JWT 只解决'你是谁'的问题，但不解决'你是否在这场考试中'。所以我在学生进入考试时生成一个 examToken 存 Redis，拦截器在 JWT 校验之后再加一层考试 Token 校验。提交和保存进度接口都必须携带 X-Exam-Token Header。Token 的过期时间和考试时长绑定，考试结束自动失效。"

---

### 亮点 2：Redis 答题进度实时缓存 ⭐

**问题**：考试过程中学生每答一题都存 MySQL 太频繁，不存又怕意外退出丢答案。

**方案**：
1. **保存进度**：每答一题 → `Redis Hash`（key=`exam:progress:{examId}:{userId}`，field=questionId，value=answer）
2. **提交考试**：先读 Redis 缓存答案，再与请求体答案合并（请求体优先），最后批量写入 MySQL
3. **异常恢复**：学生意外退出后重新进入，进度仍在 Redis 中（TTL=24h）
4. **清理策略**：提交成功后主动删除 Redis 数据；未提交则由 TTL 自然过期

**面试话术**：
> "答题进度我用了 Redis Hash 缓存，key 是 exam:progress:examId:userId，field 是题目ID，value 是答案。学生每答一题就实时写入 Redis，最终提交时合并 Redis 缓存和请求体答案写入 MySQL。这样既减少了数据库写入频率，又保证了断线重连不丢进度。提交后主动清理 Redis 数据，未提交则 TTL 自然过期。"

---

### 亮点 3：RabbitMQ 异步自动提交 + 死信队列 ⭐

**问题**：考试到时后，部分学生可能未点提交，需要自动收卷。如果同步处理，在大量学生同时到时的情况下会阻塞主流程。

**方案**：
1. `ExamStatusTask` 定时任务每分钟扫描 → 发现考试到期 → 发 MQ 消息 `{examId, userId}`
2. `ExamSubmitConsumer` 消费消息 → 从 Redis 读缓存答案 → 写入 MySQL → 清理 Redis
3. **死信队列**：主队列配置 `x-dead-letter-exchange`，消费失败的消息自动进入 DLQ，不丢失
4. 消费者 throw exception 让消息重入队列或进 DLQ

**面试话术**：
> "考试到时自动收卷我用了 RabbitMQ 异步处理。定时任务每分钟扫描到期考试，对未提交的学生发 MQ 消息，消费者从 Redis 读缓存答案写入 MySQL。这样不阻塞主流程，而且支持批量并发收卷。我还配了死信队列，消费失败的消息不会丢失，可以后续人工处理。"

**可追问的方向**：
- 为什么不用延迟队列？→ 延迟队列适合固定延迟场景，考试结束时间可能被教师修改，定时扫描更灵活
- 消息幂等性？→ autoSubmitExam 方法内部校验 record 状态，已提交的会跳过
- **哪些异常重试、哪些直接死信？**
  - 不可重试直入 DLQ：消息格式错误→`IllegalArgumentException`；业务异常（考试不存在/已提交）→`BusinessException`
  - 可重试 3 次：数据库连接超时→`DataAccessException`；SQL超时→`SQLException`；网络超时→`TimeoutException`。指数退避 1s→2s→4s
- DLQ 消费者做什么？→ ExamDlqConsumer 监听 gradingDeadLetterQueue，记录失败消息的 examId/userId 并输出处理建议供运维跟进

---

### 亮点 4：随机组卷算法

**问题**：教师出题时，不同学生应拿到不同但难度相当的试卷。

**方案**：
1. 教师定义组卷规则（PaperRule）：题型 + 难度 + 抽数量 + 每题分值 + 可选限定题库
2. 考试发布时触发 `generateRandomPaperQuestions()`
3. 按规则查题 → `Collections.shuffle()` 随机打乱 → 取前 N 题
4. 用 `usedQuestionIds` Set 去重，避免不同规则抽到重复题目
5. 抽完自动更新试卷总分

**面试话术**：
> "随机组卷是考试发布时触发的。教师预设规则——题型、难度、抽题数量、每题分值，可以限定题库范围。发布时按规则查题，Collections.shuffle 打乱后取前 N 题，用 Set 去重避免不同规则间重复抽题。如果题目不足直接抛业务异常提示教师。"

---

### 亮点 5：客观题自动批改 + 主观题手动批改的混合批改流程 ⭐

**问题**：不同题型需要不同的批改方式，且需要交卷后立即出分让学生看到客观题成绩。

**方案**：
1. **交卷即批改**：`submitExam()` 保存答案后立即调用 `gradeSingleRecord()` 自动批改单选/多选/判断题
2. **完全匹配判分**：答案字符串 `trim()` 后完全匹配，区分大小写
3. **事务管理**：答案保存使用 `@Transactional` 包裹循环 insert，保证原子性
4. **主观题手动批改**：填空/简答 → 教师通过 `gradeSubjective` 接口逐题给分，自动累加总分
5. **状态自动流转**：全部批改完 → status 从 SUBMITTED 自动变为 GRADED

**面试话术**：
> "批改分两步走：学生交卷后立即触发客观题自动批改，单/多/判通过字符串完全匹配判分并更新分数；然后教师手动批改主观题。我在 ExamRecordServiceImpl 提取了 gradeSingleRecord 方法，交卷和自动提交都能复用。答案用 saveBatch 批量插入 MySQL，减少数据库往返次数。每次批改后自动检查是否所有题目都已批改，全部完成则状态变为 GRADED。"

---

### 亮点 6：成绩统计与 Redis 缓存 ⭐ (新增)

**问题**：每查一次统计都要遍历所有考试记录重新计算平均分/最高分/及格率，性能差且浪费。

**方案**：
1. 新增 `ExamStatisticsController` 提供统计 API：
   - `GET /api/exam-statistics/exam/{examId}` — 平均分/最高分/最低分/及格率/分数段分布
   - `GET /api/exam-statistics/class/{classId}/exam/{examId}` — 班级维度统计
2. 计算完成后写入 Redis（key=`exam:stats:{examId}`，TTL=10分钟）
3. 批改完成后调用 `clearCache` 使缓存失效
4. 返回结构含 `scoreDistribution`（分数段分布 Map）和 `studentScores`（学生成绩明细 List）

**面试话术**：
> "成绩统计做了 Redis 缓存。统计接口先检查缓存，命中直接返回。如果未命中则实时计算平均分/最高分/最低分/及格率/分数段分布和学生明细，写入 Redis 并设 10 分钟 TTL。批改完成后主动清理缓存，保证数据一致性。这样高频查询直接走 Redis，避免重复全表扫描。"


---

## 四、技术难点与踩坑记录

### 难点 1：数据库字段与 Java 实体不一致

**问题**：MyBatis-Plus 的 `@TableField(fill = FieldFill.INSERT)` 自动填充 `create_time`/`update_time`，但部分建表 SQL 遗漏了这些字段，导致 INSERT 报 `Unknown column 'create_time'`。

**解决**：
- 统一用存储过程条件判断补充缺失字段（`patch_api_fix.sql`）
- NOT NULL 无默认值但 Java 实体无对应字段的，改为 `NULL DEFAULT NULL`

**面试话术**：
> "开发中踩了一个 MyBatis-Plus 和建表 SQL 不一致的坑。实体类有 createTime/updateTime 且配了自动填充，但建表脚本部分表遗漏了这两个字段，INSERT 时报 Unknown column。我写了一套幂等的补丁脚本，用存储过程判断列是否存在再 ADD COLUMN，确保脚本可重复执行。"

---

### 难点 2：ExamToken 反查问题

**问题**：考试提交时，请求体只有 examToken，不知道 examId。但 Redis 的 token key 是 `exam:token:{examId}:{userId}`，无法直接反查。

**解决**：双写策略——同时存储 `exam:token:value:{token} → "examId:userId"`，提交时通过 token 反查。

**面试话术**：
> "考试 Token 的设计遇到了一个反查问题。进入考试时 Redis key 包含 examId，但提交时请求体只有 token 不知道 examId。我采用了双写策略，存了两份映射：一份按 examId+userId 做 key，一份按 token 字符串做 key。这样提交时能通过 token 反查到 examId 和 userId。"

---

### 难点 3：考试记录创建时机

**问题**：原设计中 ExamRecord 在 publishExam 时批量创建，但如果直接创建考试不经过发布步骤，enterExam 时 existingRecord 为 null 会报错。

**解决**：enterExam 中增加 `existingRecord == null` 判断，自动创建已开始状态的记录。

**面试话术**：
> "考试记录的创建时机我做了一个兜底处理。正常流程是发布考试时批量给学生创建记录，但如果跳过发布直接让学生进入考试，记录不存在就会报错。我在 enterExam 中加了判断，记录不存在时自动创建一个已开始状态的记录，保证流程的健壮性。"

---

## 五、高频面试题 + 标准回答

### Q1：为什么选 Redis 做答题缓存而不是直接写 MySQL？

> 考试场景下答题频率很高，每题都写 MySQL 会产生大量小事务，高并发时可能造成锁竞争和性能瓶颈。Redis 是内存数据库，单线程无锁竞争，Hash 结构天然适合存 questionId→answer 的映射，读写都是 O(1)。最终提交时再批量写入 MySQL，既减少了数据库压力，又保证了断线重连的进度恢复。

### Q2：RabbitMQ 消息丢失怎么处理？

> 我做了三层保障：
> 1. 队列和交换机都设为 durable=true，Broker 重启不丢
> 2. 配置了死信队列（DLQ），消费者异常时消息进入 DLQ 而非直接丢弃
> 3. 消费者逻辑本身做了幂等——autoSubmitExam 方法内校验 record 状态，已提交的会跳过，重复消费不会出问题

### Q3：JWT 和 Session 的区别？为什么选 JWT？

> JWT 是无状态的，服务端不保存会话信息，适合前后端分离架构和分布式部署。Session 需要服务端存储，多实例部署要共享 Session（比如用 Redis Session）。我选 JWT 是因为考试系统是单体应用，JWT 的无状态特性简化了认证逻辑，Token 自带 userId/username/roleCode，不用每次查库。缺点是 Token 一旦签发无法主动撤销，不过考试 Token（examToken）存在 Redis 里可以主动删除，弥补了这个不足。

### Q4：如果 1000 个学生同时考试，系统会有什么瓶颈？

> **瓶颈分析**：
> 1. Redis：单节点 QPS 10w+，1000 学生每 30 秒保存一次进度 ≈ 33 QPS，完全没压力
> 2. MySQL：最终提交时 1000 个事务并发写入，可以通过批量 INSERT 优化（当前是逐条 insert）
> 3. RabbitMQ：考试结束时 1000 条消息瞬间涌入，消费者需要控制消费速率
>
> **优化方向**：
> - 答案写入已改为批量 INSERT（MyBatis-Plus saveBatch），而非逐条 insert
> - MQ 消费者配置 prefetchCount 控制消费速率
> - Redis 可以做集群化，MySQL 读写分离

### Q5：随机组卷的随机性如何保证？会不会出现相邻学生试卷一样？

> 当前方案是同一份试卷所有学生拿到相同题目（手工组卷），随机组卷的随机性体现在不同次发布时会重新抽题。如果要实现**每人不同卷**，可以在 enterExam 时为每个学生动态生成一套题目，题目的抽取基于规则 + 随机种子（userId + examId），既保证随机性又可复现。

### Q6：数据库设计有哪些考虑？

> 1. **分表策略**：exam_record 和 exam_answer 分开存储，因为一条考试记录对应多条答案，分开后查询效率更高
> 2. **冗余字段**：exam_record 中存了 objectiveScore 和 subjectiveScore，虽然可以实时计算，但查分场景频繁，冗余避免每次 JOIN 聚合
> 3. **软删除 vs 硬删除**：当前考试删除是硬删除+级联清答案，生产环境建议软删除（加 is_deleted 字段）
> 4. **索引设计**：exam_answer 表的 idx_record_id 加速按记录查答案，exam_record 的 examId+userId 联合查询

### Q7：MyBatis-Plus 在项目中怎么用的？

> 1. **BaseMapper**：所有 Mapper 继承 BaseMapper<Entity>，自动拥有 CRUD 方法
> 2. **LambdaQueryWrapper**：所有查询都用 Lambda 方式，类型安全不怕字段名写错
> 3. **分页插件**：MyBatisPlusInterceptor 配置 PaginationInnerInterceptor，接口层传 page/size 自动分页
> 4. **自动填充**：MetaObjectHandler 处理 createTime/updateTime，INSERT/UPDATE 时自动填充
> 5. **自定义 SQL**：复杂查询（如 selectExamList、selectAnswerListByRecordId）写在 Mapper XML 中

### Q8：Redis 的数据结构选择？

| 业务 | 数据结构 | Key 格式 | TTL |
|------|---------|----------|-----|
| 答题进度 | Hash | `exam:progress:{examId}:{userId}` | 24h |
| 考试Token(正查) | String | `exam:token:{examId}:{userId}` | 考试时长+5min |
| 考试Token(反查) | String | `exam:token:value:{token}` | 考试时长+5min |
| 考试倒计时 | String | `exam:timer:{examId}:{userId}` | 剩余秒数 |
| 考试状态 | String | `exam:status:{examId}:0` | 24h |

> Hash 存进度是因为天然适合 field=questionId, value=answer 的映射；
> String 存 Token/状态是因为值简单，不需要多字段。

---

## 六、项目数据

| 指标 | 数值 |
|------|------|
| Java 源文件 | 75 个 |
| Mapper XML | 9 个 |
| React 组件/页面 | 30+ 个 |
| 数据库表 | 16 张 |
| REST 接口 | 20+ 个 |
| 代码总行数 | 17,187 行 |

---

## 七、可扩展方向（展示你的技术视野）

1. **防作弊**：IP 限制、切屏检测、题目乱序
2. **AI 批改**：主观题接入大模型（如文心/通义）做语义相似度评分
3. **数据分析**：考试结果统计、错题分析、知识点掌握度雷达图
4. **分布式部署**：Spring Cloud 微服务化，网关统一鉴权
5. **WebSocket**：实时倒计时推送、教师端实时监控学生在线状态

---

## 八、简历项目描述（可直接用）

### 在线考试系统 | Spring Boot 3 + MyBatis-Plus + Redis + RabbitMQ

- 独立开发高校在线考试系统，覆盖教师出题组卷、学生在线答题、自动/手动批改全流程
- 设计**考试 Token 双重验证机制**（JWT 身份认证 + Redis examToken 会话校验），保障考试安全
- 基于 **Redis Hash** 实现答题进度实时缓存，支持断线重连进度恢复，提交时合并缓存与请求答案批量写入 MySQL
- 使用 **RabbitMQ + 死信队列**实现考试到时异步自动收卷，保证消息可靠投递与消费幂等
- 实现随机组卷算法，支持按题型/难度/题库规则抽题，Collections.shuffle 保证随机性
- 客观题自动批改（字符串匹配）+ 主观题手动批改的混合批改流程，自动计算总分并流转批改状态
