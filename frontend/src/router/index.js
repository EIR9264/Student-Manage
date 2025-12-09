import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/LoginView.vue'
import TodoList from '../views/TodoView.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/todo', component: TodoList }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
