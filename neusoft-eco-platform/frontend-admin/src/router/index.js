import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/LoginView.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/feedback',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'feedback',
        name: 'FeedbackList',
        component: () => import('@/pages/FeedbackListView.vue'),
        meta: { title: '反馈列表', requiresAuth: true }
      },
      {
        path: 'feedback/:afId',
        name: 'FeedbackDetail',
        component: () => import('@/pages/FeedbackDetailView.vue'),
        meta: { title: '反馈详情', requiresAuth: true }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/pages/StatisticsView.vue'),
        meta: { title: 'AQI统计', requiresAuth: true }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/pages/DashboardView.vue'),
        meta: { title: '环境监测大屏', requiresAuth: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title
    ? `${to.meta.title} - 东软环保监督平台`
    : '东软环保公众监督平台'

  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isLogin) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && authStore.isLogin) {
    next({ name: 'FeedbackList' })
  } else {
    next()
  }
})

export default router
