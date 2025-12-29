import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', component: () => import('../views/HomeView.vue') },
  { path: '/home', component: () => import('../views/HomeView.vue') },
  { path: '/login', component: () => import('../views/LoginView.vue') },
  { path: '/register', component: () => import('../views/RegisterView.vue') },
  { path: '/student', component: () => import('../views/StudentView.vue') },
  { path: '/profile', component: () => import('../views/ProfileView.vue') },
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
