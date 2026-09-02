import { createRouter, createWebHistory } from 'vue-router'
import { pinia } from '@/app/pinia'
import { useAuthStore } from '@/features/auth/stores/auth-store'
import type { UserRole } from '@/features/users/types/user'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    guestOnly?: boolean
    roles?: UserRole[]
  }
}

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/app/layouts/AppLayout.vue'),
      children: [
        { path: '', redirect: { name: 'login' } },
        {
          path: 'services',
          name: 'services',
          component: () => import('@/features/services/views/ServiceListView.vue'),
        },
        {
          path: 'providers',
          name: 'providers',
          component: () => import('@/app/views/ModuleView.vue'),
          props: { title: 'Prestadores', description: 'Descoberta e perfis públicos de prestadores.' },
        },
        { path: 'dashboard', name: 'dashboard', component: () => import('@/app/views/DashboardView.vue'), meta: { requiresAuth: true } },
        {
          path: 'profile', name: 'profile', component: () => import('@/app/views/ModuleView.vue'),
          props: { title: 'Minha conta', description: 'Dados pessoais e preferências da conta.' }, meta: { requiresAuth: true },
        },
        {
          path: 'provider/profile', name: 'provider-profile', component: () => import('@/app/views/ModuleView.vue'),
          props: { title: 'Perfil profissional', description: 'Onboarding e gestão do perfil do prestador.' },
          meta: { requiresAuth: true, roles: ['PROVIDER'] },
        },
        {
          path: 'bookings', name: 'bookings', component: () => import('@/app/views/ModuleView.vue'),
          props: { title: 'Pedidos', description: 'Histórico e gestão de pedidos de serviço.' }, meta: { requiresAuth: true },
        },
        {
          path: 'notifications', name: 'notifications', component: () => import('@/app/views/ModuleView.vue'),
          props: { title: 'Notificações', description: 'Atualizações dos pedidos e da conta.' }, meta: { requiresAuth: true },
        },
        {
          path: 'admin', name: 'admin', component: () => import('@/app/views/ModuleView.vue'),
          props: { title: 'Administração', description: 'Moderação de utilizadores, prestadores, serviços e categorias.' },
          meta: { requiresAuth: true, roles: ['ADMIN'] },
        },
      ],
    },
    { path: '/login', name: 'login', component: () => import('@/features/auth/views/LoginView.vue'), meta: { guestOnly: true } },
    { path: '/register', name: 'register', component: () => import('@/features/auth/views/RegisterView.vue'), meta: { guestOnly: true } },
    { path: '/forgot-password', name: 'forgot-password', component: () => import('@/features/auth/views/ForgotPasswordView.vue'), meta: { guestOnly: true } },
    { path: '/reset-password', name: 'reset-password', component: () => import('@/features/auth/views/ResetPasswordView.vue'), meta: { guestOnly: true } },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/app/views/NotFoundView.vue') },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach(async (to) => {
  const auth = useAuthStore(pinia)
  await auth.initialize()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && auth.isAuthenticated) return { name: 'dashboard' }
  if (to.meta.roles?.length && (!auth.role || !to.meta.roles.includes(auth.role))) {
    return { name: 'dashboard' }
  }
})
