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

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 获取 token (假设后端返回的 token 存放在 localStorage 中)
  const token = localStorage.getItem('token')

  // 如果去的不是登录页，且没有 token，则强制跳转登录页
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
