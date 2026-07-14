import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
  },
  {
    path: '/list/:type?',
    name: 'List',
    component: () => import('../views/List.vue'),
  },
  {
    path: '/item/:slug',
    name: 'Detail',
    component: () => import('../views/Detail.vue'),
  },
  {
    path: '/scene/:slug',
    name: 'Scene',
    component: () => import('../views/Scene.vue'),
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('../views/Search.vue'),
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach(async (to, from, next) => {
  const auth = useAuthStore()

  if (!localStorage.getItem('token') && auth.isLoggedIn) {
    auth.logout()
  }

  // Ensure user data is loaded for authenticated routes
  if (auth.isLoggedIn && !auth.user) {
    try {
      await auth.fetchUser()
    } catch (e) {
      // fetchUser already calls logout on failure
    }
  }

  if (to.meta.requiresAuth && !localStorage.getItem('token')) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.meta.requiresAdmin && !auth.isAdmin) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
