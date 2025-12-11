<script setup>
import { ref, onMounted, reactive, watch } from 'vue' // 引入 watch
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const API_URL = 'http://localhost:8080/api/students'

const students = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const searchKeyword = ref('')

// 表单数据
const form = reactive({
  id: null,
  name: '',
  studentNumber: '',
  gender: '男',
  age: 18
})

// 登出
const logout = () => {
  localStorage.removeItem('token')
  router.push('/login')
  ElMessage.info('已退出登录')
}

// 获取列表核心逻辑
const fetchStudents = async () => {
  loading.value = true
  try {
    const res = await axios.get(API_URL, {
      params: { keyword: searchKeyword.value }
    })
    students.value = res.data
  } catch (e) {
    console.error(e)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// --- 热加载搜索 (防抖实现) ---
let timer = null
// 监听搜索框的变化
watch(searchKeyword, () => {
  // 如果之前有定时器，先清除（说明用户还在打字）
  if (timer) clearTimeout(timer)

  // 重新设置定时器，300ms 后执行搜索
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
  dialogVisible.value = true
}

// 打开编辑窗口
const openEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 保存数据
const handleSave = async () => {
  if (!form.name || !form.studentNumber) return ElMessage.warning('姓名和学号不能为空')

  try {
    if (isEdit.value) {
      // 修复：直接 await，不再定义未使用的 res 变量
      await axios.put(`${API_URL}/${form.id}`, form)
      fetchStudents()
      ElMessage.success('修改成功')
    } else {
      await axios.post(API_URL, form)
      fetchStudents()
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
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
      await axios.delete(`${API_URL}/${id}`)
      fetchStudents()
      ElMessage.success('删除成功')
    } catch (e) {
      ElMessage.error('删除失败：'+e)
    }
  })
}

onMounted(() => fetchStudents())
</script>

<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo">
        <el-icon style="margin-right: 8px; vertical-align: middle;"><Management /></el-icon>
        <span>学生管理系统</span>
      </div>
      <el-button type="danger" plain size="small" @click="logout">退出登录</el-button>
    </el-header>

    <el-main class="main-content">
      <el-card class="box-card" shadow="hover">
        <div class="operation-area">
          <el-row :gutter="20">
            <el-col :span="8">
              <!-- 搜索框：去掉了按钮触发，改为自动监听 -->
              <el-input
                v-model="searchKeyword"
                placeholder="输入姓名或学号，自动搜索..."
                clearable
                prefix-icon="Search"
              />
            </el-col>
            <el-col :span="16" style="text-align: right;">
              <el-button type="primary" icon="Plus" @click="openAddDialog">添加学生</el-button>
            </el-col>
          </el-row>
        </div>

        <el-table :data="students" v-loading="loading" style="width: 100%" border stripe>
          <el-table-column prop="id" label="ID" width="80" sortable />
          <el-table-column prop="studentNumber" label="学号" width="150" sortable />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="gender" label="性别" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.gender === '男' ? '' : 'danger'" effect="plain">{{ scope.row.gender }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="age" label="年龄" width="100" sortable />

          <el-table-column label="操作" min-width="150">
            <template #default="scope">
              <el-button size="small" type="primary" link @click="openEditDialog(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" link @click="handleDelete(scope.row.id)">删除</el-button>
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
.layout { height: 100vh; display: flex; flex-direction: column; }
.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}
.logo { font-size: 18px; font-weight: 600; color: #303133; display: flex; align-items: center; }
.main-content { background-color: #f5f7fa; padding: 20px; }
.box-card { margin: 0 auto; border-radius: 8px; }
.operation-area { margin-bottom: 20px; }
</style>
