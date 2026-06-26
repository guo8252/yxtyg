import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { title: '月度看板' }
  },
  {
    path: '/review',
    name: 'Review',
    component: () => import('../views/Review.vue'),
    meta: { title: '评审统计' }
  },
  {
    path: '/training',
    name: 'Training',
    component: () => import('../views/Training.vue'),
    meta: { title: '转培上报' }
  },
  {
    path: '/experimenter',
    name: 'Experimenter',
    component: () => import('../views/Experimenter.vue'),
    meta: { title: '体验官管理' }
  },
  {
    path: '/workorder',
    name: 'WorkOrder',
    component: () => import('../views/WorkOrder.vue'),
    meta: { title: '需求提单' }
  },
  {
    path: '/requirement',
    name: 'Requirement',
    component: () => import('../views/Requirement.vue'),
    meta: { title: '需求管理' }
  },
  {
    path: '/requirement/form/:id?',
    name: 'RequirementForm',
    component: () => import('../views/RequirementForm.vue'),
    meta: { title: '需求编辑' }
  },
  {
    path: '/model-config',
    name: 'ModelConfig',
    component: () => import('../views/ModelConfig.vue'),
    meta: { title: '大模型设置' }
  },
  {
    path: '/embedding-config',
    name: 'EmbeddingConfig',
    component: () => import('../views/EmbeddingConfig.vue'),
    meta: { title: '向量化模型配置' }
  },
  {
    path: '/agent-config',
    name: 'AgentConfig',
    component: () => import('../views/AgentConfig.vue'),
    meta: { title: '智能体参数' }
  },
  {
    path: '/smart-recommend',
    name: 'SmartRecommend',
    component: () => import('../views/SmartRecommend.vue'),
    meta: { title: '智能推荐' }
  },
  {
    path: '/vector-search',
    name: 'VectorSearch',
    component: () => import('../views/VectorSearch.vue'),
    meta: { title: '向量搜索' }
  }
]

const router = new VueRouter({
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title + ' - 一线体验官专项数据分析系统'
  }
  const token = localStorage.getItem('yxtyg_token')
  if (!to.meta.public && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
