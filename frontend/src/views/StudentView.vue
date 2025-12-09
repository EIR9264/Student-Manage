<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const API_URL = 'http://localhost:8080/api/students'

const students = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

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
  router.push('/login')
  ElMessage.info('已退出登录')
}

// 获取列表
const fetchStudents = async () => {
  loading.value = true
  try {
    const res = await axios.get(API_URL)
    students.value = res.data
  } catch (e) {
    console.error(e)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

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
  Object.assign(form, row) // 回填数据
  dialogVisible.value = true
}

// 保存数据（新增或修改）
const handleSave = async () => {
  if (!form.name || !form.studentNumber) return ElMessage.warning('姓名和学号不能为空')

  try {
    if (isEdit.value) {
      // 修改
      const res = await axios.put(`${API_URL}/${form.id}`, form)
      // 更新本地列表中的数据
      const index = students.value.findIndex(s => s.id === form.id)
      students.value[index] = res.data
      ElMessage.success('修改成功')
    } else {
      // 新增
      const res = await axios.post(API_URL, form)
      students.value.push(res.data)
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
      students.value = students.value.filter(s => s.id !== id)
      ElMessage.success('删除成功')
    } catch (e) {
      ElMessage.error('删除失败'+e)
    }
  })
}

onMounted(() => fetchStudents())
</script>

<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo">学生管理系统</div>
      <el-button type="danger" plain size="small" @click="logout">退出登录</el-button>
    </el-header>

    <el-main>
      <el-card class="box-card">
        <div class="operation-area">
          <el-button type="primary" icon="Plus" @click="openAddDialog">添加学生</el-button>
        </div>

        <el-table :data="students" v-loading="loading" style="width: 100%" border stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="studentNumber" label="学号" width="150" />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="gender" label="性别" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.gender === '男' ? '' : 'danger'">{{ scope.row.gender }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="age" label="年龄" width="100" />

          <el-table-column label="操作" min-width="150">
            <template #default="scope">
              <el-button size="small" @click="openEditDialog(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑学生' : '添加学生'"
        width="500px"
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
.layout { height: 100vh; }
.header { background: #fff; border-bottom: 1px solid #dcdfe6; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.logo { font-size: 20px; font-weight: bold; color: #409EFF; }
.box-card { max-width: 1000px; margin: 20px auto; }
.operation-area { margin-bottom: 20px; }
</style>
