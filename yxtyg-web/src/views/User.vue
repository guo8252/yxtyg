<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="page-card search-card">
      <el-form :inline="true" :model="queryParams" size="small">
        <el-form-item label="账号">
          <el-input v-model="queryParams.username" placeholder="请输入" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="queryParams.realName" placeholder="请输入" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryParams.role" clearable placeholder="请选择" style="width: 140px">
            <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="page-card table-card">
      <div class="page-card__header">
        <div class="page-card__title">用户列表</div>
        <div class="page-card__actions">
          <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAdd">新增用户</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe size="mini" v-loading="loading">
        <el-table-column prop="username" label="账号" min-width="140" show-overflow-tooltip />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="role" label="角色" width="140">
          <template slot-scope="scope">
            {{ formatRole(scope.row.role) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag size="small" :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" size="small" style="color: #f56c6c" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-size="queryParams.size"
        :current-page="queryParams.current"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 15px; text-align: right"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" size="small">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, createUser, updateUser, deleteUser } from '@/api/user'

export default {
  name: 'User',
  data() {
    return {
      roleOptions: [
        { value: 'SYS_ADMIN', label: '系统管理员' },
        { value: 'DEV_ADMIN', label: '开发管理员' },
        { value: 'PRODUCT_MANAGER', label: '产品经理' }
      ],
      loading: false,
      queryParams: {
        username: '',
        realName: '',
        role: '',
        current: 1,
        size: 10
      },
      tableData: [],
      total: 0,
      dialogVisible: false,
      dialogTitle: '',
      form: {
        id: null,
        username: '',
        realName: '',
        password: '',
        role: '',
        status: 1
      },
      rules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        password: [{
          validator: (rule, value, callback) => {
            if (!this.form.id && !value) {
              callback(new Error('请输入密码'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }]
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    formatRole(role) {
      const item = this.roleOptions.find(r => r.value === role)
      return item ? item.label : role || '-'
    },
    loadData() {
      this.loading = true
      getUserList(this.queryParams).then(res => {
        if (res.code === 200) {
          this.tableData = res.data.records
          this.total = res.data.total
        }
      }).finally(() => {
        this.loading = false
      })
    },
    handleSearch() {
      this.queryParams.current = 1
      this.loadData()
    },
    handleReset() {
      this.queryParams = {
        username: '',
        realName: '',
        role: '',
        current: 1,
        size: 10
      }
      this.loadData()
    },
    handleSizeChange(val) {
      this.queryParams.size = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.queryParams.current = val
      this.loadData()
    },
    handleAdd() {
      this.dialogTitle = '新增用户'
      this.form = {
        id: null,
        username: '',
        realName: '',
        password: '',
        role: '',
        status: 1
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.formRef.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogTitle = '编辑用户'
      this.form = {
        id: row.id,
        username: row.username,
        realName: row.realName,
        password: '',
        role: row.role,
        status: row.status
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.formRef.clearValidate()
      })
    },
    handleSubmit() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return
        const api = this.form.id ? updateUser : createUser
        const payload = { ...this.form }
        if (this.form.id && !payload.password) {
          delete payload.password
        }
        const promise = this.form.id ? api(this.form.id, payload) : api(payload)
        promise.then(res => {
          if (res.code === 200) {
            this.$message.success('操作成功')
            this.dialogVisible = false
            this.loadData()
          }
        })
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该用户吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteUser(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          }
        })
      })
    }
  }
}
</script>

<style scoped>
.search-card .el-form-item {
  margin-bottom: 0;
}

.table-card {
  background: #fff;
}
</style>
