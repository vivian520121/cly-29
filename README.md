# 企业级项目协作管理平台

## 技术栈

### 后端
- Java 17 + Spring Boot 3.2.x
- MySQL 8.0 + MyBatis Plus 3.5.x
- Redis 7 + JWT 鉴权
- Knife4j (Swagger) 接口文档
- MinIO 对象存储

### 前端
- React 18 + Vite 5
- Zustand 状态管理
- Ant Design 5.x
- @dnd-kit 拖拽
- gantt-task-react 甘特图

## 快速启动

### Docker 一键启动
```bash
docker-compose up -d
```

访问地址：
- 前端: http://localhost
- 后端API: http://localhost:8080
- 接口文档: http://localhost:8080/doc.html

### 本地开发启动

#### 后端
```bash
cd backend
mvn spring-boot:run
```

#### 前端
```bash
cd frontend
npm install
npm run dev
```

## 默认账号
- 管理员: admin / admin123
- 普通用户: user / user123

## 项目结构

```
├── backend/          # Java后端
│   ├── src/main/java/com/cly/project/
│   │   ├── common/      # 通用组件
│   │   ├── config/      # 配置类
│   │   ├── controller/  # 控制器
│   │   ├── entity/      # 实体类
│   │   ├── enums/       # 枚举类
│   │   ├── mapper/      # 数据访问层
│   │   ├── service/     # 业务逻辑层
│   │   └── util/        # 工具类
│   └── src/main/resources/
├── frontend/         # React前端
│   ├── src/
│   │   ├── api/         # 接口请求
│   │   ├── components/  # 公共组件
│   │   ├── layouts/     # 布局组件
│   │   ├── pages/       # 页面组件
│   │   ├── router/      # 路由配置
│   │   ├── store/       # 状态管理
│   │   └── utils/       # 工具函数
├── sql/              # 数据库脚本
└── docker-compose.yml
```
