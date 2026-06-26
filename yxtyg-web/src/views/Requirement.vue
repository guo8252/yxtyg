<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card class="page-card search-card">
      <el-form :inline="true" :model="queryParams" size="small">
        <el-form-item label="需求名称">
          <el-input v-model="queryParams.name" placeholder="请输入" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="归属系统">
          <el-input v-model="queryParams.systemName" placeholder="请输入" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="产品经理">
          <el-select v-model="queryParams.productManagerId" clearable placeholder="请选择" style="width: 140px">
            <el-option v-for="pm in productManagers" :key="pm.id" :label="pm.realName || pm.username" :value="pm.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" clearable placeholder="请选择" style="width: 110px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
        <div class="page-card__title">需求列表</div>
        <div class="page-card__actions">
          <el-button type="success" size="small" icon="el-icon-download" @click="handleDownloadTemplate">下载模板</el-button>
          <el-upload
            class="upload-inline"
            action=""
            accept=".xlsx,.xls"
            :show-file-list="false"
            :http-request="handleImportRequest"
            :before-upload="beforeUpload">
            <el-button type="primary" size="small" icon="el-icon-upload2">导入 Excel</el-button>
          </el-upload>
          <el-button v-if="isAdminOrDev" type="primary" size="small" icon="el-icon-plus" @click="handleAdd">新增需求</el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe size="mini" v-loading="loading">
        <el-table-column prop="name" label="需求名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="productManagerName" label="产品经理" width="120" />
        <el-table-column prop="systemName" label="归属系统" width="150" />
        <el-table-column prop="initialWorkload" label="初核工作量" width="110" align="right" />
        <el-table-column prop="finalWorkload" label="最终核定工作量" width="130" align="right" />
        <el-table-column prop="reducedWorkload" label="核减工作量" width="110" align="right" />
        <el-table-column prop="statusLabel" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag size="small" :type="getStatusType(scope.row.status)">{{ scope.row.statusLabel || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="canEdit(scope.row)" type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button v-if="canDelete(scope.row)" type="text" size="small" style="color: #f56c6c" @click="handleDelete(scope.row)">删除</el-button>
            <el-button
              v-if="canFill(scope.row)"
              type="text"
              size="small"
              style="color: #67c23a"
              @click="handleFill(scope.row)">
              填写
            </el-button>
            <el-button
              v-if="canApprove(scope.row)"
              type="text"
              size="small"
              style="color: #409eff"
              @click="handleApprove(scope.row)">
              核定
            </el-button>
            <el-button
              v-if="isAdminOrDev"
              type="text"
              size="small"
              style="color: #e6a23c"
              @click="handleUrge(scope.row)">
              催办
            </el-button>
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

    <!-- 填写最终工作量弹窗 -->
    <el-dialog title="填写最终工作量" :visible.sync="fillVisible" width="500px">
      <el-form :model="fillForm" :rules="fillRules" ref="fillFormRef" label-width="120px" size="small">
        <el-form-item label="需求名称">
          <span>{{ fillForm.name }}</span>
        </el-form-item>
        <el-form-item label="初核工作量">
          <span>{{ fillForm.initialWorkload }}</span>
        </el-form-item>
        <el-form-item label="最终核定工作量" prop="finalWorkload">
          <el-input-number v-model="fillForm.finalWorkload" :min="0" :precision="2" :controls="false" placeholder="请输入" style="width: 100%" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="fillVisible = false">取消</el-button>
        <el-button type="primary" @click="handleFillSubmit">确定</el-button>
      </span>
    </el-dialog>

    <!-- 导入结果弹窗 -->
    <el-dialog title="导入结果" :visible.sync="importVisible" width="600px">
      <div v-if="importResult">
        <el-alert
          :title="importResult.message || '导入完成'"
          :type="importResult.failCount > 0 ? 'warning' : 'success'"
          :closable="false"
          show-icon
        />
        <el-table v-if="importResult.failDetails && importResult.failDetails.length > 0" :data="importResult.failDetails" border size="mini" style="margin-top: 15px">
          <el-table-column prop="row" label="行号" width="80" />
          <el-table-column prop="reason" label="失败原因" />
        </el-table>
      </div>
      <span slot="footer">
        <el-button @click="importVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getRequirementList,
  deleteRequirement,
  importRequirement,
  downloadTemplate,
  fillFinalWorkload,
  approveRequirement
} from '@/api/requirement'
import { urgeRequirement } from '@/api/urge'
import { getProductManagers } from '@/api/user'

export default {
  name: 'Requirement',
  data() {
    return {
      statusOptions: [
        { value: 'PENDING', label: '待填写' },
        { value: 'FILLED', label: '已填写' },
        { value: 'APPROVED', label: '已核定' }
      ],
      loading: false,
      queryParams: {
        name: '',
        systemName: '',
        status: '',
        productManagerId: null,
        current: 1,
        size: 10
      },
      tableData: [],
      total: 0,
      productManagers: [],
      fillVisible: false,
      fillForm: {
        id: null,
        name: '',
        initialWorkload: null,
        finalWorkload: undefined
      },
      fillRules: {
        finalWorkload: [{ required: true, message: '请输入最终核定工作量', trigger: 'blur' }]
      },
      importVisible: false,
      importResult: null
    }
  },
  computed: {
    currentUser() {
      return this.$store.state.user.userInfo
    },
    isAdminOrDev() {
      const role = this.currentUser.role
      return role === 'DEV_ADMIN' || role === 'SYS_ADMIN'
    },
    isProductManager() {
      return this.currentUser.role === 'PRODUCT_MANAGER'
    }
  },
  created() {
    this.loadProductManagers()
    this.loadData()
  },
  methods: {
    loadProductManagers() {
      getProductManagers({ current: 1, size: 1000 }).then(res => {
        if (res.code === 200) {
          this.productManagers = res.data.records || []
        }
      })
    },
    getStatusType(status) {
      if (status === 'PENDING') return 'warning'
      if (status === 'FILLED') return 'success'
      if (status === 'APPROVED') return 'info'
      return ''
    },
    canEdit(row) {
      if (this.isAdminOrDev) return true
      return this.isProductManager && row.status === 'PENDING' && row.productManagerId === this.currentUser.id
    },
    canDelete(row) {
      return this.isAdminOrDev
    },
    canFill(row) {
      return this.isProductManager && row.status === 'PENDING' && row.productManagerId === this.currentUser.id
    },
    canApprove(row) {
      return this.isAdminOrDev && row.status === 'FILLED'
    },
    loadData() {
      this.loading = true
      getRequirementList(this.queryParams).then(res => {
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
        name: '',
        systemName: '',
        status: '',
        productManagerId: null,
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
      this.$router.push('/requirement/form')
    },
    handleEdit(row) {
      this.$router.push('/requirement/form/' + row.id)
    },
    handleDelete(row) {
      this.$confirm('确定要删除该需求吗？', '提示', {
        type: 'warning'
      }).then(() => {
        deleteRequirement(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          }
        })
      })
    },
    handleFill(row) {
      this.fillForm = {
        id: row.id,
        name: row.name,
        initialWorkload: row.initialWorkload,
        finalWorkload: undefined
      }
      this.fillVisible = true
      this.$nextTick(() => {
        this.$refs.fillFormRef.clearValidate()
      })
    },
    handleFillSubmit() {
      this.$refs.fillFormRef.validate(valid => {
        if (!valid) return
        fillFinalWorkload(this.fillForm.id, this.fillForm.finalWorkload).then(res => {
          if (res.code === 200) {
            this.$message.success('填写成功')
            this.fillVisible = false
            this.loadData()
          }
        })
      })
    },
    handleApprove(row) {
      this.$confirm(`确定要核定需求"${row.name}"吗？核定后状态将变为已核定。`, '提示', {
        type: 'warning'
      }).then(() => {
        approveRequirement(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('核定成功')
            this.loadData()
          }
        })
      })
    },
    handleUrge(row) {
      this.$confirm(`确定要催办需求"${row.name}"吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        urgeRequirement(row.id).then(res => {
          if (res.code === 200) {
            this.$message.success('催办成功')
            this.loadData()
          }
        })
      })
    },
    beforeUpload(file) {
      const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls')
      if (!isExcel) {
        this.$message.error('只能上传Excel文件')
        return false
      }
      return true
    },
    handleImportRequest(options) {
      importRequirement(options.file).then(res => {
        if (res.code === 200) {
          this.importResult = res.data
          this.importVisible = true
          this.loadData()
        }
      })
    },
    handleDownloadTemplate() {
      downloadTemplate().then(res => {
        const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = 'requirement_template.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(link.href)
      }).catch(() => {
        this.$message.error('下载模板失败')
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

.upload-inline {
  display: inline-block;
  margin: 0 8px;
}
</style>
