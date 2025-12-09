import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/LoginView.vue'
import StudentView from '../views/StudentView.vue' // 假设你将 TodoView 改名为 StudentView

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/student', component: StudentView } // 更新路径
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
