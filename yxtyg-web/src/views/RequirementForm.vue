<template>
  <div class="page-container">
    <el-card class="page-card">
      <div class="page-card__header">
        <div class="page-card__title">{{ isEdit ? '编辑需求' : '新增需求' }}</div>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        size="small"
        style="max-width: 600px; margin-top: 20px"
      >
        <el-form-item label="需求名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入需求名称" />
        </el-form-item>

        <el-form-item label="需求描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入需求描述" />
        </el-form-item>

        <el-form-item label="产品经理" prop="productManagerId">
          <el-select v-model="form.productManagerId" filterable placeholder="请选择产品经理" style="width: 100%">
            <el-option
              v-for="pm in productManagers"
              :key="pm.id"
              :label="pm.realName || pm.username"
              :value="pm.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="归属系统" prop="systemName">
          <el-input v-model="form.systemName" placeholder="请输入归属系统" />
        </el-form-item>

        <el-form-item label="初核工作量" prop="initialWorkload">
          <el-input-number
            v-model="form.initialWorkload"
            :min="0"
            :precision="2"
            :controls="false"
            placeholder="请输入初核工作量"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="初核金额" prop="initialAmount">
          <el-input-number
            v-model="form.initialAmount"
            :min="0"
            :precision="2"
            :controls="false"
            placeholder="请输入初核金额"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item v-if="isEdit" label="最终核定工作量" prop="finalWorkload">
          <el-input-number
            v-model="form.finalWorkload"
            :min="0"
            :precision="2"
            :controls="false"
            placeholder="请输入最终核定工作量"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item v-if="isEdit" label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { createRequirement, updateRequirement, getRequirementDetail } from '@/api/requirement'
import { getUserList } from '@/api/user'

export default {
  name: 'RequirementForm',
  data() {
    return {
      statusOptions: [
        { value: 'PENDING', label: '待填写' },
        { value: 'FILLED', label: '已填写' },
        { value: 'APPROVED', label: '已核定' }
      ],
      productManagers: [],
      form: {
        id: null,
        name: '',
        description: '',
        productManagerId: null,
        systemName: '',
        initialWorkload: undefined,
        initialAmount: undefined,
        finalWorkload: undefined,
        status: 'PENDING'
      },
      rules: {
        name: [{ required: true, message: '请输入需求名称', trigger: 'blur' }],
        productManagerId: [{ required: true, message: '请选择产品经理', trigger: 'change' }],
        systemName: [{ required: true, message: '请输入归属系统', trigger: 'blur' }],
        initialWorkload: [{ required: true, message: '请输入初核工作量', trigger: 'blur' }],
        initialAmount: [{ required: true, message: '请输入初核金额', trigger: 'blur' }]
      }
    }
  },
  computed: {
    isEdit() {
      return !!this.$route.params.id
    }
  },
  created() {
    this.loadProductManagers()
    if (this.isEdit) {
      this.loadDetail()
    }
  },
  methods: {
    loadProductManagers() {
      getUserList({ role: 'PRODUCT_MANAGER', current: 1, size: 1000 }).then(res => {
        if (res.code === 200) {
          this.productManagers = res.data.records || []
        }
      })
    },
    loadDetail() {
      getRequirementDetail(this.$route.params.id).then(res => {
        if (res.code === 200) {
          const data = res.data
          this.form = {
            id: data.id,
            name: data.name,
            description: data.description,
            productManagerId: data.productManagerId,
            systemName: data.systemName,
            initialWorkload: data.initialWorkload,
            initialAmount: data.initialAmount,
            finalWorkload: data.finalWorkload,
            status: data.status || 'PENDING'
          }
        }
      })
    },
    handleSubmit() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return
        const api = this.isEdit ? updateRequirement : createRequirement
        const promise = this.isEdit ? api(this.form.id, this.form) : api(this.form)
        promise.then(res => {
          if (res.code === 200) {
            this.$message.success('保存成功')
            this.$router.push('/requirement')
          }
        })
      })
    },
    handleCancel() {
      this.$router.push('/requirement')
    }
  }
}
</script>

<style scoped>
.page-card__header {
  margin-bottom: 10px;
}
</style>
