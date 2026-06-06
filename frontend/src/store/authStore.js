import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { login, getUserInfo } from '@/services/auth'

export const useAuthStore = create(
  persist(
    (set, get) => ({
      token: '',
      userInfo: null,
      login: async (loginData) => {
        const data = await login(loginData)
        set({ token: data.token })
        const userInfo = await getUserInfo()
        set({ userInfo })
        return data
      },
      logout: () => {
        set({ token: '', userInfo: null })
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
      },
      setUserInfo: (userInfo) => set({ userInfo })
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({ token: state.token, userInfo: state.userInfo })
    }
  )
)
