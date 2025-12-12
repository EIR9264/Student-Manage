<script setup>
import { ref, onMounted, reactive, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '../api/request'

const router = useRouter()

const students = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const searchKeyword = ref('')
const selectedClass = ref('')
const classList = ref([])

// 获取用户信息
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
  password: ''
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
    const res = await request.get('/students/classes')
    classList.value = res.data
  } catch (e) {
    console.error(e)
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
    console.error(e)
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
  dialogVisible.value = true
}

// 打开编辑窗口
const openEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  form.password = '' // 编辑时密码为空，如果不修改就不传
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
    console.error(e)
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
      console.error(e)
    }
  })
}

// 去个人中心
const goToProfile = () => {
  router.push('/profile')
}

// 登出
const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
  ElMessage.info('已退出登录')
}

onMounted(() => {
  fetchUserInfo()
  fetchStudents()
  fetchClasses()
})
</script>

<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo">
        <el-icon style="margin-right: 8px; vertical-align: middle;"><Management /></el-icon>
        <span>学生管理系统</span>
      </div>
      <div class="header-right">
        <el-tag :type="isAdmin ? 'danger' : ''" style="margin-right: 10px;">
          {{ isAdmin ? '管理员' : '普通用户' }}
        </el-tag>
        <el-button type="primary" plain size="small" @click="goToProfile" style="margin-right: 10px;">
          个人中心
        </el-button>
        <el-button type="danger" plain size="small" @click="logout">退出登录</el-button>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-card class="box-card" shadow="hover">
        <div class="operation-area">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-select
                v-model="selectedClass"
                placeholder="选择班级"
                clearable
                style="width: 100%;"
              >
                <el-option label="全部班级" value="" />
                <el-option
                  v-for="className in classList"
                  :key="className"
                  :label="className"
                  :value="className"
                />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-input
                v-model="searchKeyword"
                placeholder="输入姓名或学号搜索..."
                clearable
                prefix-icon="Search"
              />
            </el-col>
            <el-col :span="12" style="text-align: right;">
              <el-button
                v-if="isAdmin"
                type="primary"
                icon="Plus"
                @click="openAddDialog"
              >
                添加学生
              </el-button>
            </el-col>
          </el-row>
        </div>

        <el-table :data="students" v-loading="loading" style="width: 100%" border stripe>
          <el-table-column prop="id" label="ID" width="80" sortable />
          <el-table-column prop="studentNumber" label="学号" width="150" sortable />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="gender" label="性别" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.gender === '男' ? '' : 'danger'" effect="plain">
                {{ scope.row.gender }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="age" label="年龄" width="100" sortable />
          <el-table-column prop="className" label="班级" width="150" />

          <el-table-column v-if="isAdmin" label="操作" min-width="150">
            <template #default="scope">
              <el-button size="small" type="primary" link @click="openEditDialog(scope.row)">
                编辑
              </el-button>
              <el-button size="small" type="danger" link @click="handleDelete(scope.row.id)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

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
            <el-input v-model="form.name" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="form.gender">
              <el-radio label="男" value="男">男</el-radio>
              <el-radio label="女" value="女">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="年龄">
            <el-input-number v-model="form.age" :min="1" :max="100" />
          </el-form-item>
          <el-form-item label="班级">
            <el-input v-model="form.className" placeholder="请输入班级名称" />
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
    </el-main>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.logo {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.main-content {
  background-color: #f5f7fa;
  padding: 20px;
}

.box-card {
  margin: 0 auto;
  border-radius: 8px;
}

.operation-area {
  margin-bottom: 20px;
}
</style>
