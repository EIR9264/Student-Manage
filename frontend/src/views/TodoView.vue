<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const API_URL = 'http://localhost:8080/api/todos'
const todos = ref([])
const newTodoTitle = ref('')
const loading = ref(false)

// 登出
const logout = () => {
  router.push('/login')
  ElMessage.info('已退出登录')
}

// 获取数据
const fetchTodos = async () => {
  loading.value = true
  try {
    const res = await axios.get(API_URL)
    todos.value = res.data
  } catch (e) {
    console.error(e)
    ElMessage.error('获取列表失败，请检查后端服务')
  } finally {
    loading.value = false
  }
}

// 添加
const handleAdd = async () => {
  if (!newTodoTitle.value.trim()) return ElMessage.warning('内容不能为空')

  try {
    const res = await axios.post(API_URL, { title: newTodoTitle.value, completed: false })
    todos.value.push(res.data)
    newTodoTitle.value = ''
    ElMessage.success('添加成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('添加失败')
  }
}

// 切换状态
const toggleStatus = async (todo) => {
  try {
    await axios.put(`${API_URL}/${todo.id}`, { ...todo, completed: todo.completed })
    // ElMessage.success(todo.completed ? '已完成' : '未完成') // 可选反馈
  } catch (e) {
    console.error(e)
    todo.completed = !todo.completed // 回滚
    ElMessage.error('状态更新失败')
  }
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这个任务吗?', '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await axios.delete(`${API_URL}/${id}`)
      todos.value = todos.value.filter(t => t.id !== id)
      ElMessage.success('删除成功')
    } catch (e) {
      console.error(e)
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => fetchTodos())
</script>

<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="logo">My Awesome Todo</div>
      <el-button type="danger" plain size="small" @click="logout">退出登录</el-button>
    </el-header>

    <el-main>
      <el-card class="box-card" v-loading="loading">
        <div class="input-area">
          <el-input
            v-model="newTodoTitle"
            placeholder="今天要做什么？"
            size="large"
            clearable
            @keyup.enter="handleAdd"
          >
            <template #append>
              <el-button type="primary" @click="handleAdd">添加任务</el-button>
            </template>
          </el-input>
        </div>

        <el-table :data="todos" style="width: 100%" :show-header="false">
          <el-table-column width="60">
            <template #default="scope">
              <el-checkbox v-model="scope.row.completed" @change="toggleStatus(scope.row)" size="large"/>
            </template>
          </el-table-column>

          <el-table-column>
            <template #default="scope">
              <span :class="{ 'done-text': scope.row.completed }">{{ scope.row.title }}</span>
            </template>
          </el-table-column>

          <el-table-column width="100" align="right">
            <template #default="scope">
              <el-button type="danger" icon="Delete" circle @click="handleDelete(scope.row.id)" />
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="todos.length === 0" description="暂无任务，快去添加吧！"></el-empty>
      </el-card>
    </el-main>
  </el-container>
</template>

<style scoped>
.layout { height: 100vh; }
.header { background: #fff; border-bottom: 1px solid #dcdfe6; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.logo { font-size: 20px; font-weight: bold; color: #409EFF; }
.box-card { max-width: 800px; margin: 20px auto; min-height: 500px; }
.input-area { margin-bottom: 20px; }
.done-text { text-decoration: line-through; color: #909399; }
</style>
