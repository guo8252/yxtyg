# 修复审查 Critical / Important 问题报告

## 提交信息

- `bc7c293` fix: address critical and important review issues

## 状态

DONE

## 已修复问题

### Critical（全部修复）

1. **需求接口角色/数据权限控制**
   - `SecurityConfig.java` 已按角色细化 `/api/requirement/**` 授权：
     - POST /api/requirement、PUT /api/requirement/{id}、DELETE /api/requirement/{id} → DEV_ADMIN / SYS_ADMIN
     - POST /api/requirement/{id}/fill → PRODUCT_MANAGER
     - POST /api/requirement/{id}/approve → DEV_ADMIN / SYS_ADMIN
     - 列表/详情保持 authenticated，由 service 层按角色过滤数据
   - `RequirementServiceImpl.queryPage` 对 PRODUCT_MANAGER 已限制只能查看自己负责的需求
   - `RequirementServiceImpl.getDetail` 新增角色/数据权限校验

2. **Excel 导入重复需求名称校验**
   - `RequirementServiceImpl.importExcel` 收集文件内名称并检测重复；同时查询数据库已存在名称；重复行加入 failDetails，不阻断其他行

3. **Excel 导入初核金额 >= 0**
   - 新增 `parseNonNegativeDecimal`，初核金额校验改为 `>= 0`

4. **Excel 导入归属系统非空校验**
   - `RequirementExcelParser` 在解析时为 `systemName` 添加非空校验

5. **前端用户编辑表单密码不强制输入**
   - `User.vue` 编辑时 password 改为条件校验，提交时若密码为空则不更新

6. **Blob 下载错误提示**
   - `request.js` 响应拦截器在 `responseType === 'blob'` 时直接返回数据，不再校验 `res.code`

### Important（全部修复，7-13）

7. **审批流程**
   - 新增 `POST /api/requirement/{id}/approve`，仅限 DEV_ADMIN / SYS_ADMIN
   - `approve` 服务方法将状态从 FILLED 改为 APPROVED
   - `update` 方法通过 `BeanUtils.copyProperties(dto, req, "status")` 禁止随意修改 status

8. **fillFinalWorkload 权限收紧**
   - 仅限 PRODUCT_MANAGER，且只能填写自己负责的需求

9. **创建/更新需求时校验 productManagerId**
   - 新增 `validateProductManager` 方法，校验用户存在且角色为 PRODUCT_MANAGER

10. **N+1 查询优化**
    - `RequirementServiceImpl.queryPage` 改为批量查询产品经理用户信息
    - `UrgeRecordServiceImpl.queryPage` 改为批量查询需求和操作人信息

11. **JWT 默认密钥风险提醒**
    - `JwtTokenProvider.init` 在使用默认密钥时打印 WARN 日志

12. **导入分批保存**
    - `RequirementServiceImpl.importExcel` 改为 `saveBatch(saveList, 100)`

13. **补充权限/业务规则测试**
    - 新增 `RequirementControllerSecurityTest`：Spring Boot + MockMvc 集成测试，验证需求接口角色控制
    - 扩展 `RequirementServiceImplTest`：覆盖重复名称、数据库已存在、初核金额 >=0、权限错误、审批流程、产品经理校验
    - 新增 `RequirementExcelParserTest`：覆盖归属系统非空、需求名称非空、解析成功

## 测试结果

- `mvn clean test`：26 个测试全部通过
- `npm run build`：构建成功（仅有 asset size 警告，非错误）

## 文件变更

### 后端
- `yxtyg-admin/pom.xml`
- `yxtyg-admin/src/main/java/com/jscm/yxtyg/config/SecurityConfig.java`
- `yxtyg-admin/src/main/java/com/jscm/yxtyg/controller/RequirementController.java`
- `yxtyg-admin/src/main/java/com/jscm/yxtyg/dto/RequirementExcelDTO.java`
- `yxtyg-admin/src/main/java/com/jscm/yxtyg/security/JwtTokenProvider.java`
- `yxtyg-admin/src/main/java/com/jscm/yxtyg/service/RequirementService.java`
- `yxtyg-admin/src/main/java/com/jscm/yxtyg/service/impl/RequirementServiceImpl.java`
- `yxtyg-admin/src/main/java/com/jscm/yxtyg/service/impl/UrgeRecordServiceImpl.java`
- `yxtyg-admin/src/main/java/com/jscm/yxtyg/util/RequirementExcelParser.java`
- `yxtyg-admin/src/test/java/com/jscm/yxtyg/service/impl/RequirementServiceImplTest.java`
- `yxtyg-admin/src/test/java/com/jscm/yxtyg/controller/RequirementControllerSecurityTest.java`（新增）
- `yxtyg-admin/src/test/java/com/jscm/yxtyg/util/RequirementExcelParserTest.java`（新增）
- `yxtyg-admin/src/test/resources/application.yml`（新增，H2 测试数据源）

### 前端
- `yxtyg-web/src/utils/request.js`
- `yxtyg-web/src/views/User.vue`

## 自检

- 所有 Critical 和 Important 问题均已修复
- 后端单元测试和集成测试全部通过
- 前端生产构建成功
- 未引入额外生产依赖（H2 仅 test scope）
- 未修改现有接口返回结构
- 未泄露密钥或敏感信息

## 问题与说明

- 为支持 Spring Security 集成测试，新增 H2 内存数据库作为 test scope 依赖，并补充 `src/test/resources/application.yml`。
- `npm run build` 的警告为既有资源体积提示，不影响构建结果。
