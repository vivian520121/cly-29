import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Form, Input, Button, Checkbox, Card, message } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useAuthStore } from '@/store/authStore'
import styles from './Login.module.scss'

const Login = () => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [remember, setRemember] = useState(false)
  const [autoLogin, setAutoLogin] = useState(false)
  const navigate = useNavigate()
  const { login, token } = useAuthStore()

  useEffect(() => {
    if (token) {
      navigate('/dashboard')
    }
    const savedUsername = localStorage.getItem('rememberedUsername')
    const savedPassword = localStorage.getItem('rememberedPassword')
    const savedRemember = localStorage.getItem('remember')
    if (savedRemember === 'true' && savedUsername) {
      form.setFieldsValue({
        username: savedUsername,
        password: savedPassword || ''
      })
      setRemember(true)
    }
  }, [token, navigate, form])

  const handleSubmit = async (values) => {
    setLoading(true)
    try {
      if (remember) {
        localStorage.setItem('rememberedUsername', values.username)
        localStorage.setItem('rememberedPassword', values.password)
        localStorage.setItem('remember', 'true')
        if (autoLogin) {
          localStorage.setItem('autoLogin', 'true')
        }
      } else {
        localStorage.removeItem('rememberedUsername')
        localStorage.removeItem('rememberedPassword')
        localStorage.removeItem('remember')
        localStorage.removeItem('autoLogin')
      }

      await login(values)
      message.success('登录成功')
      navigate('/dashboard')
    } catch (error) {
      console.error('Login error:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card className={styles.loginCard}>
      <div className={styles.loginHeader}>
        <h1 className={styles.title}>项目协作平台</h1>
        <p className={styles.subtitle}>欢迎回来，请登录您的账号</p>
      </div>

      <Form
        form={form}
        name="login"
        onFinish={handleSubmit}
        autoComplete="off"
        size="large"
      >
        <Form.Item
          name="username"
          rules={[
            { required: true, message: '请输入用户名' },
            { min: 3, message: '用户名至少3个字符' }
          ]}
        >
          <Input
            prefix={<UserOutlined className={styles.inputIcon} />}
            placeholder="请输入用户名"
          />
        </Form.Item>

        <Form.Item
          name="password"
          rules={[
            { required: true, message: '请输入密码' },
            { min: 6, message: '密码至少6个字符' }
          ]}
        >
          <Input.Password
            prefix={<LockOutlined className={styles.inputIcon} />}
            placeholder="请输入密码"
          />
        </Form.Item>

        <Form.Item>
          <div className={styles.loginOptions}>
            <div className={styles.checkboxGroup}>
              <Checkbox
                checked={remember}
                onChange={(e) => setRemember(e.target.checked)}
              >
                记住密码
              </Checkbox>
              <Checkbox
                checked={autoLogin}
                onChange={(e) => setAutoLogin(e.target.checked)}
                disabled={!remember}
              >
                自动登录
              </Checkbox>
            </div>
            <a href="#" className={styles.forgotPassword}>
              忘记密码?
            </a>
          </div>
        </Form.Item>

        <Form.Item>
          <Button
            type="primary"
            htmlType="submit"
            loading={loading}
            block
            className={styles.loginBtn}
          >
            登录
          </Button>
        </Form.Item>
      </Form>

      <div className={styles.loginFooter}>
        <p>
          还没有账号? <a href="#">立即注册</a>
        </p>
      </div>
    </Card>
  )
}

export default Login
