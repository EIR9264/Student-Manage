<script setup>
import { ref, onMounted, reactive, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete } from '@element-plus/icons-vue'
import AppLayout from '@/components/AppLayout.vue'
import request from '../api/request'

const students = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const searchKeyword = ref('')
const selectedClass = ref('')
const classList = ref([])
const classManageVisible = ref(false)
const classForm = reactive({ id: null, name: '' })
const isClassEdit = ref(false)

const userInfo = ref({})
const isAdmin = computed(() => userInfo.value.role === 'ADMIN')

// 表单数据
const form = reactive({
  id: null,
  name: '',
  studentNumber: '',
  gender: '男',
  age: 18,
  className: '',
  password: '',
  role: 'USER',
  email: '',
  phone: ''
})

// 获取用户信息
const fetchUserInfo = () => {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo)
  }
}

// 获取班级列表
const fetchClasses = async () => {
  try {
    const res = await request.get('/classes')
    classList.value = res.data
  } catch (e) {
    // 静默处理
  }
}

// 获取学生列表
const fetchStudents = async () => {
  loading.value = true
  try {
    const res = await request.get('/students', {
      params: {
        keyword: searchKeyword.value,
        className: selectedClass.value
      }
    })
    students.value = res.data
  } catch (e) {
    // 静默处理
  } finally {
    loading.value = false
  }
}

// 热加载搜索 (防抖实现)
let timer = null
watch([searchKeyword, selectedClass], () => {
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    fetchStudents()
  }, 300)
})

// 打开新增窗口
const openAddDialog = () => {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.studentNumber = ''
  form.gender = '男'
  form.age = 18
  form.className = ''
  form.password = '123'
  form.role = 'USER'
  form.email = ''
  form.phone = ''
  dialogVisible.value = true
}

// 打开编辑窗口
const openEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  form.password = '' // 编辑时密码为空，如果不修改就不传
  form.role = row.role || 'USER' // 确保角色有值
  form.email = row.email || ''
  form.phone = row.phone || ''
  dialogVisible.value = true
}

// 保存数据
const handleSave = async () => {
  if (!form.name || !form.studentNumber) {
    ElMessage.warning('姓名和学号不能为空')
    return
  }

  try {
    if (isEdit.value) {
      const res = await request.put(`/students/${form.id}`, form)
      if (res.data.success) {
        fetchStudents()
        ElMessage.success('修改成功')
        dialogVisible.value = false
      }
    } else {
      const res = await request.post('/students', form)
      if (res.data.success) {
        fetchStudents()
        fetchClasses() // 刷新班级列表
        ElMessage.success('添加成功')
        dialogVisible.value = false
      }
    }
  } catch (e) {
    // 静默处理
  }
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该学生吗?', '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      const res = await request.delete(`/students/${id}`)
      if (res.data.success) {
        fetchStudents()
        ElMessage.success('删除成功')
      }
    } catch (e) {
      // 静默处理
    }
  })
}

// 班级管理相关方法
const openClassManage = () => {
  classManageVisible.value = true
}

const openAddClass = () => {
  isClassEdit.value = false
  classForm.id = null
  classForm.name = ''
}

const openEditClass = (cls) => {
  isClassEdit.value = true
  classForm.id = cls.id
  classForm.name = cls.name
}

const getStudentCount = (className) => {
  return students.value.filter(s => s.className === className).length
}

const handleSaveClass = async () => {
  if (!classForm.name.trim()) {
    ElMessage.warning('班级名称不能为空')
    return
  }
  try {
    if (isClassEdit.value) {
      const res = await request.put(`/classes/${classForm.id}`, { name: classForm.name })
      if (res.data.success) {
        ElMessage.success('修改成功')
        fetchClasses()
        fetchStudents()
      } else {
        ElMessage.error(res.data.message)
      }
    } else {
      const res = await request.post('/classes', { name: classForm.name })
      if (res.data.success) {
        ElMessage.success('添加成功')
        fetchClasses()
      } else {
        ElMessage.error(res.data.message)
      }
    }
    classForm.id = null
    classForm.name = ''
    isClassEdit.value = false
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const handleDeleteClass = (cls) => {
  ElMessageBox.confirm(`确定要删除班级"${cls.name}"吗?`, '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete(`/classes/${cls.id}`)
      if (res.data.success) {
        ElMessage.success('删除成功')
        fetchClasses()
      } else {
        ElMessage.error(res.data.message)
      }
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  })
}

onMounted(() => {
  fetchUserInfo()
  fetchStudents()
  fetchClasses()
})
</script>

<template>
  <AppLayout>
    <div class="student-container">
      <!-- 操作区 -->
      <div class="filter-card">
        <el-select
          v-model="selectedClass"
          placeholder="选择班级"
          clearable
          class="filter-select"
        >
          <el-option label="全部班级" value="" />
          <el-option
            v-for="cls in classList"
            :key="cls.id"
            :label="cls.name"
            :value="cls.name"
          />
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索姓名或学号..."
          clearable
          prefix-icon="Search"
          class="filter-input"
        />
        <el-button
          v-if="isAdmin"
          type="info"
          icon="Setting"
          class="ios-pressable"
          @click="openClassManage"
        >
          班级管理
        </el-button>
        <el-button
          v-if="isAdmin"
          type="primary"
          icon="Plus"
          class="add-btn ios-pressable"
          @click="openAddDialog"
        >
          添加学生
        </el-button>
      </div>

      <!-- 表格卡片 -->
      <div class="table-card">
        <el-table
          :data="students"
          v-loading="loading"
          stripe
          class="ios-table"
        >
          <el-table-column label="头像" width="70">
            <template #default="scope">
              <el-avatar
                :size="40"
                :src="scope.row.avatarUrl"
                class="student-avatar"
              >
                {{ scope.row.name?.charAt(0) || '?' }}
              </el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="studentNumber" label="学号" width="120" />
          <el-table-column prop="name" label="姓名" width="120">
            <template #default="scope">
              <span :class="{
                'name-super-admin': scope.row.username === 'admin',
                'name-admin': scope.row.role === 'ADMIN' && scope.row.username !== 'admin'
              }">
                {{ scope.row.name }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="gender" label="性别" width="80">
            <template #default="scope">
              <span class="gender-tag" :class="scope.row.gender === '男' ? 'male' : 'female'">
                {{ scope.row.gender }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="age" label="年龄" width="80" />
          <el-table-column prop="className" label="班级" min-width="120" />
          <el-table-column v-if="isAdmin" label="操作" width="100" fixed="right">
            <template #default="scope">
              <div class="action-btns">
                <el-button
                  :icon="Edit"
                  circle
                  size="small"
                  @click="openEditDialog(scope.row)"
                />
                <el-button
                  v-if="scope.row.username !== 'admin' && scope.row.username !== userInfo.username"
                  :icon="Delete"
                  circle
                  size="small"
                  type="danger"
                  @click="handleDelete(scope.row.id)"
                />
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 弹窗 -->
      <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑学生' : '添加学生'"
        width="500px"
        align-center
      >
        <el-form :model="form" label-width="80px">
          <el-form-item label="学号">
            <el-input v-model="form.studentNumber" placeholder="请输入学号" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="form.name" placeholder="请输入姓名" :disabled="isEdit && form.username === 'admin'" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="form.gender">
              <el-radio label="男" value="男">男</el-radio>
              <el-radio label="女" value="女">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="userInfo.username === 'admin' && form.username !== 'admin'" label="角色">
            <el-radio-group v-model="form.role">
              <el-radio label="USER" value="USER">普通用户</el-radio>
              <el-radio label="ADMIN" value="ADMIN">管理员</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="年龄">
            <el-input-number v-model="form.age" :min="1" :max="100" />
          </el-form-item>
          <el-form-item label="班级">
            <el-select v-model="form.className" placeholder="请选择班级" style="width: 100%">
              <el-option
                v-for="cls in classList"
                :key="cls.id"
                :label="cls.name"
                :value="cls.name"
              />
            </el-select>
          </el-form-item>
          <el-form-item v-if="isEdit" label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item v-if="isEdit" label="手机号">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item v-if="isAdmin && isEdit" label="密码">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="不修改请留空"
              show-password
            />
            <span style="font-size: 12px; color: #909399;">留空则不修改密码</span>
          </el-form-item>
          <el-form-item v-if="!isEdit">
            <el-alert
              title="提示：新增学生将自动创建登录账号，默认密码为123"
              type="info"
              :closable="false"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSave">确定</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 班级管理弹窗 -->
      <el-dialog
        v-model="classManageVisible"
        title="班级管理"
        width="500px"
        align-center
      >
        <div class="class-form">
          <el-input
            v-model="classForm.name"
            :placeholder="isClassEdit ? '修改班级名称' : '输入新班级名称'"
            style="flex: 1"
          />
          <el-button type="primary" @click="handleSaveClass">
            {{ isClassEdit ? '保存' : '添加' }}
          </el-button>
          <el-button v-if="isClassEdit" @click="openAddClass">取消</el-button>
        </div>
        <el-table :data="classList" style="margin-top: 16px">
          <el-table-column prop="name" label="班级名称" />
          <el-table-column label="学生数" width="80">
            <template #default="scope">
              {{ getStudentCount(scope.row.name) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button
                size="small"
                :disabled="getStudentCount(scope.row.name) > 0"
                @click="openEditClass(scope.row)"
              >编辑</el-button>
              <el-button
                size="small"
                type="danger"
                :disabled="getStudentCount(scope.row.name) > 0"
                @click="handleDeleteClass(scope.row)"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="classList.length === 0" class="empty-tip">
          暂无班级，请先添加
        </div>
      </el-dialog>
    </div>
  </AppLayout>
</template>

<style scoped>
.student-container {
  max-width: 1200px;
  margin: 0 auto;
}

.filter-card {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-select {
  width: 160px;
}

.filter-input {
  width: 240px;
}

.add-btn {
  margin-left: auto;
}

.table-card {
  background: var(--ios-card-solid);
  border-radius: var(--ios-radius-lg);
  box-shadow: var(--ios-shadow);
  overflow: hidden;
}

.ios-table {
  --el-table-border-color: var(--ios-separator);
  --el-table-row-hover-bg-color: var(--ios-blue-light);
}

.gender-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
}

.gender-tag.male {
  background: rgba(0, 122, 255, 0.1);
  color: var(--ios-blue);
}

.gender-tag.female {
  background: rgba(255, 45, 85, 0.1);
  color: #ff2d55;
}

.action-btns {
  display: flex;
  gap: 8px;
}

.name-super-admin {
  color: var(--ios-orange) !important;
  font-weight: 600 !important;
}

.name-admin {
  color: var(--ios-green) !important;
  font-weight: 600 !important;
}

.class-form {
  display: flex;
  gap: 12px;
}

.empty-tip {
  text-align: center;
  color: var(--ios-text-secondary);
  padding: 20px;
}

.student-avatar {
  background: linear-gradient(135deg, var(--ios-blue), var(--ios-purple));
  color: white;
  font-weight: 600;
}
</style>
