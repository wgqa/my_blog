import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    requiresAdmin?: boolean
    guestOnly?: boolean
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/public/HomeView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: () => import('../views/AdminDashboardView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('../views/AdminUsersView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/posts',
      name: 'admin-posts',
      component: () => import('../views/AdminPostsView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/posts/:id/edit',
      name: 'admin-post-edit',
      component: () => import('../views/AdminPostFormView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/categories',
      name: 'admin-categories',
      component: () => import('../views/AdminCategoriesView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/tags',
      name: 'admin-tags',
      component: () => import('../views/AdminTagsView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/me',
      name: 'author-dashboard',
      component: () => import('../views/AuthorDashboardView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/me/posts',
      name: 'author-posts',
      component: () => import('../views/AuthorPostsView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/me/posts/new',
      name: 'author-post-create',
      component: () => import('../views/AuthorPostFormView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/me/posts/:id/edit',
      name: 'author-post-edit',
      component: () => import('../views/AuthorPostFormView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/categories/:slug',
      name: 'category',
      component: () => import('../views/public/CategoryView.vue'),
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('../views/public/SearchView.vue'),
    },
    {
      path: '/posts/:slug',
      name: 'post-detail',
      component: () => import('../views/public/PostDetailView.vue'),
    },
    {
      path: '/authors/:username',
      name: 'author',
      component: () => import('../views/public/AuthorView.vue'),
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : authStore.isAdmin ? '/admin' : '/me'
    return redirect
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return '/'
  }
})

export default router
