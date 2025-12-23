import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/LoginView.vue'
import StudentView from '../views/StudentView.vue'
import RegisterView from '../views/RegisterView.vue'
import ProfileView from '../views/ProfileView.vue'
import HomeView from '../views/HomeView.vue'

const routes = [
  { path: '/', component: HomeView },
  { path: '/home', component: HomeView },
  { path: '/login', component: Login },
  { path: '/register', component: RegisterView },
  { path: '/student', component: StudentView },
  { path: '/profile', component: ProfileView },
  // 课程相关路由
  { path: '/courses', component: () => import('../views/CourseListView.vue') },
  { path: '/courses/:id', component: () => import('../views/CourseDetailView.vue') },
  { path: '/my-courses', component: () => import('../views/MyCoursesView.vue') },
  { path: '/calendar', component: () => import('../views/CourseCalendarView.vue') },
  // 班级成员路由（普通用户）
  { path: '/classmates', component: () => import('../views/ClassmatesView.vue') },
  // 管理员路由
  { path: '/course-manage', component: () => import('../views/CourseManageView.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 登录页和注册页不需要token
  if (to.path === '/login' || to.path === '/register') {
    if (token) {
      next('/') // 已登录则跳转主页
    } else {
      next()
    }
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router
