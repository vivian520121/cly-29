import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import AuthLayout from '@/layouts/AuthLayout'
import MainLayout from '@/layouts/MainLayout'
import Login from '@/pages/auth/Login'
import Dashboard from '@/pages/dashboard/Dashboard'
import ProjectList from '@/pages/project/ProjectList'
import ProjectDetail from '@/pages/project/ProjectDetail'
import TaskKanban from '@/pages/task/TaskKanban'
import TaskList from '@/pages/task/TaskList'
import TaskDetail from '@/pages/task/TaskDetail'
import Organization from '@/pages/organization/Organization'
import FileManager from '@/pages/file/FileManager'
import Profile from '@/pages/profile/Profile'
import Search from '@/pages/search/Search'
import NotFound from '@/pages/error/NotFound'

const PrivateRoute = ({ children }) => {
  const token = useAuthStore(state => state.token)
  return token ? children : <Navigate to="/auth/login" replace />
}

const App = () => {
  return (
    <Routes>
      <Route path="/auth" element={<AuthLayout />}>
        <Route path="login" element={<Login />} />
      </Route>

      <Route path="/" element={<PrivateRoute><MainLayout /></PrivateRoute>}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="project" element={<ProjectList />} />
        <Route path="project/:id" element={<ProjectDetail />} />
        <Route path="task/kanban" element={<TaskKanban />} />
        <Route path="task/list" element={<TaskList />} />
        <Route path="task/:id" element={<TaskDetail />} />
        <Route path="organization" element={<Organization />} />
        <Route path="file" element={<FileManager />} />
        <Route path="profile" element={<Profile />} />
        <Route path="search" element={<Search />} />
      </Route>

      <Route path="/404" element={<NotFound />} />
      <Route path="*" element={<Navigate to="/404" replace />} />
    </Routes>
  )
}

export default App
