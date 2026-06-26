# 需求管理模块设计文档

## 1. 背景与目标

### 1.1 背景

当前科室软件需求开发工作由合作伙伴供应商负责。需求开发前后需要分别进行工作量预估与最终核定，当前流程依赖在线表格管理，存在流程分散、效率低、统计不便、状态不透明、历史追溯困难等问题。

### 1.2 目标

建设工作量核定管理系统，实现需求工作量的线上化管理：

- 集中管理需求信息
- 支持 Excel 批量导入
- 产品经理可填写最终核定工作量
- 开发管理员可跟踪状态、发起催办
- 系统自动计算核减工作量
- 支持按时间/产品经理/系统维度查询

## 2. 范围

本 spec 覆盖需求管理完整模块，包括：

- 用户/角色/权限体系（RBAC）
- 需求信息 CRUD
- Excel 模板下载与批量导入
- 需求状态流转
- 催办功能
- 前端页面与路由

## 3. 数据库设计

### 3.1 t_user（用户表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AI | 主键 |
| username | VARCHAR(50) | 登录账号，唯一 |
| real_name | VARCHAR(50) | 真实姓名 |
| password | VARCHAR(100) | 密码（BCrypt 加密） |
| role | VARCHAR(20) | 角色：PRODUCT_MANAGER / DEV_ADMIN / SYS_ADMIN |
| status | TINYINT | 0-禁用，1-启用 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除：0-未删除，1-已删除 |

### 3.2 t_requirement（需求表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AI | 主键 |
| name | VARCHAR(200) | 需求名称 |
| description | TEXT | 需求描述 |
| product_manager_id | BIGINT | 关联产品经理（t_user.id） |
| system_name | VARCHAR(100) | 归属系统 |
| initial_workload | DECIMAL(10,2) | 初核工作量（人天） |
| initial_amount | DECIMAL(12,2) | 初核金额（元） |
| final_workload | DECIMAL(10,2) | 最终核定工作量（人天），可为空 |
| reduced_workload | DECIMAL(10,2) | 核减工作量 = initial_workload - final_workload |
| status | VARCHAR(20) | 状态：PENDING / FILLED / APPROVED |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

### 3.3 t_urge_record（催办记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK AI | 主键 |
| requirement_id | BIGINT | 关联需求 |
| operator_id | BIGINT | 操作人 |
| create_time | DATETIME | 催办时间 |
| deleted | TINYINT | 逻辑删除 |

## 4. 后端设计

### 4.1 技术选型

- Spring Security + JWT 实现认证授权
- Alibaba EasyExcel 实现 Excel 导入
- MyBatis-Plus 实现数据访问
- 沿用现有 Result / PageResult 统一返回

### 4.2 新增 Controller

| Controller | 路径前缀 | 说明 |
|------------|----------|------|
| AuthController | /api/auth | 登录、获取当前用户信息 |
| UserController | /api/user | 用户管理（CRUD） |
| RequirementController | /api/requirement | 需求管理、导入、模板下载 |
| UrgeRecordController | /api/urge | 催办记录 |

### 4.3 新增 Service

| Service | 说明 |
|---------|------|
| UserService | 用户增删改查、角色校验 |
| RequirementService | 需求 CRUD、导入、状态流转、核减计算 |
| UrgeRecordService | 催办记录查询与写入 |

### 4.4 新增 Entity / DTO / VO

- entity/User、Requirement、UrgeRecord
- dto/UserDTO、UserQueryDTO、RequirementDTO、RequirementQueryDTO、RequirementExcelDTO
- vo/UserVO、RequirementVO、RequirementDetailVO、UrgeRecordVO

### 4.5 权限设计

JWT Token 结构：

```json
{
  "userId": 1,
  "username": "zhangsan",
  "role": "PRODUCT_MANAGER"
}
```

角色权限：

| 角色 | 权限 |
|------|------|
| PRODUCT_MANAGER | 查看/填写自己负责的需求 |
| DEV_ADMIN | 需求录入、导入、催办、统计、查看全部 |
| SYS_ADMIN | 用户管理、角色配置 |

### 4.6 关键接口

#### 认证

- POST /api/auth/login
- GET /api/auth/me

#### 用户管理（仅 SYS_ADMIN）

- GET /api/user/list
- POST /api/user
- PUT /api/user/{id}
- DELETE /api/user/{id}

#### 需求管理

- GET /api/requirement/list
- GET /api/requirement/detail/{id}
- POST /api/requirement
- PUT /api/requirement/{id}
- DELETE /api/requirement/{id}
- POST /api/requirement/import
- GET /api/requirement/template
- POST /api/requirement/{id}/fill（产品经理填写最终工作量）

#### 催办（仅 DEV_ADMIN）

- POST /api/urge/{requirementId}
- GET /api/urge/list

### 4.7 Excel 导入校验规则

1. 需求名称不能为空
2. 产品经理姓名必须存在于 t_user 且角色为 PRODUCT_MANAGER
3. 归属系统不能为空
4. 初核工作量必须为大于 0 的数字
5. 初核金额必须为大于等于 0 的数字
6. 状态如填写必须为 PENDING / FILLED / APPROVED 之一，未填写默认为 PENDING
7. 重复需求名称按行提示，但不阻断其他行

导入返回结构：

```json
{
  "code": 200,
  "message": "导入完成",
  "data": {
    "successCount": 10,
    "failCount": 2,
    "failDetails": [
      { "row": 3, "reason": "需求名称不能为空" },
      { "row": 5, "reason": "产品经理不存在" }
    ]
  }
}
```

## 5. 前端设计

### 5.1 新增页面

| 页面 | 路由 | 说明 |
|------|------|------|
| Login.vue | /login | 登录页 |
| Requirement.vue | /requirement | 需求列表（搜索、导入、催办、删除） |
| RequirementForm.vue | /requirement/form/:id? | 新增/编辑需求 |
| User.vue | /user | 用户管理（仅 SYS_ADMIN） |

### 5.2 路由与菜单

左侧菜单新增：

- 需求管理 /requirement
- 用户管理 /user（仅 SYS_ADMIN 可见）

未登录用户访问非登录页自动跳转到 /login。

### 5.3 状态管理

新增 store/modules/user.js：

- token
- userInfo（id、username、realName、role）
- login / logout / fetchUserInfo

request.js 中统一附加 Authorization header。

### 5.4 需求列表页功能

- 搜索：需求名称、产品经理、归属系统、状态
- 操作按钮：
  - 下载模板
  - 导入 Excel
  - 新增需求
  - 编辑
  - 删除
  - 催办（DEV_ADMIN 可见）
- 表格列：需求名称、产品经理、归属系统、初核工作量、最终核定工作量、核减工作量、状态、操作

## 6. 数据流

### 6.1 Excel 导入流程

1. 用户点击"下载模板"，后端生成带示例数据的 .xlsx
2. 用户填写后点击"导入"
3. 前端通过 el-upload 上传文件到 /api/requirement/import
4. 后端解析并校验，返回成功/失败明细
5. 前端弹窗展示结果，刷新列表

### 6.2 状态流转

```
PENDING（待填写）
  ↓ 产品经理填写最终工作量
FILLED（已填写）
  ↓ 开发管理员确认
APPROVED（已核定）
```

- 导入时默认 PENDING
- 产品经理仅可修改自己负责且状态为 PENDING 的需求
- 开发管理员可修改全部需求

### 6.3 催办流程

1. DEV_ADMIN 在列表页点击"催办"
2. 后端写入 t_urge_record
3. 实际通知（邮件/站内信）本次不实现，仅保留记录

## 7. 测试策略

### 7.1 后端测试

- RequirementService 单测：导入校验、状态流转、核减计算
- AuthController 单测：登录成功/失败、JWT 签发
- 权限拦截测试

### 7.2 前端测试

- 登录页表单校验
- 需求列表权限按钮显示控制
- 导入结果弹窗展示

## 8. 安全与性能

### 8.1 安全

- 密码 BCrypt 加密存储
- JWT Secret 配置于 application.yml，不提交到仓库
- SQL 注入通过 MyBatis-Plus 参数绑定防护
- 文件上传限制类型为 .xlsx / .xls，限制大小 10MB

### 8.2 性能

- 导入采用流式读取，单批次保存 100 条
- 列表查询分页，默认每页 10 条
- 核减工作量冗余存储，避免列表页实时计算

## 9. 风险与假设

### 9.1 假设

- 当前系统无现有用户体系，需从零构建
- 用户默认由 SYS_ADMIN 创建，不开放注册
- 通知功能本次仅做记录，不接入真实消息通道

### 9.2 风险

- 引入 Spring Security 可能影响现有接口，需仔细配置放行规则
- 前端菜单权限需要动态渲染，可能影响现有路由结构

## 10. 验收标准

- [ ] 可登录系统，不同角色看到不同菜单
- [ ] SYS_ADMIN 可创建/编辑/禁用用户
- [ ] DEV_ADMIN 可下载模板、导入 Excel、查看全部需求、发起催办
- [ ] 产品经理登录后只能看到自己负责的需求，可填写最终核定工作量
- [ ] 导入 Excel 时对不合规数据进行行级提示
- [ ] 系统自动计算核减工作量
- [ ] 需求状态正确流转
