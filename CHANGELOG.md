# 更新日志

本文档记录 jeesoul-ai-model 的所有版本更新历史。

## [1.1.0-beta] - 2025-01-XX

### 🔥 紧急修复

#### 修复 HttpClient5 依赖缺失问题
- **问题**：1.1.0 版本在使用方运行时报错 `ClassNotFoundException: org.apache.hc.client5.http.config.ConnectionConfig`
- **原因**：
  - `httpcore5` 和 `httpcore5-h2` 被错误地放在 `<dependencyManagement>` 中，而不是 `<dependencies>` 中
  - 导致依赖不会传递给使用方
  - `httpclient5:5.5.1` 需要 `httpcore5:5.3.x`，但大多数用户环境是 `httpcore5:5.1.x`，版本不兼容
- **修复**：
  - 在 `<dependencies>` 中显式声明所有依赖
  - 降级到兼容版本组合：`httpclient5:5.2.3` + `httpcore5:5.1.5` + `httpcore5-h2:5.1.5`
  - 作为通用组件，不依赖使用方的 Spring Boot BOM

### ⚠️ 重要提示
**请使用 1.1.0-beta 版本进行测试。** 如果已使用 1.1.0，请升级到 1.1.0-beta。稳定后将发布 1.1.1 正式版。

---

## [1.1.0] - 2025-01-XX ⚠️ 需打补丁

> ⚠️ **警告**：此版本 pom 依赖声明有缺陷，直接引入运行时会报 `ClassNotFoundException`。
>
> 已在使用的项目**无需更换版本**，按 [v1.1.0 补丁方案](docs/versions/v1.1.0-hotfix.md) 在自身 pom 中补三个依赖即可正常工作（已实测验证）。
> 新接入建议直接用 1.1.0-beta。

### 🎉 重大更新

#### 彻底移除 Hutool 依赖
- 自研 `SpringContextHolder` 替代 Hutool `SpringUtil`
- 基于 Spring 原生 `ApplicationContextAware` 实现
- 通过 Spring Boot 自动配置 SPI 注册，Bean 名为全限定类名，避免冲突
- **对外 API 零变化，完全向后兼容**

#### HTTP 客户端全面升级
- 移除 Hutool HTTP，自研基于 Apache HttpClient 5.x 的封装
- 新增 `http` 包：`HttpRequest`、`HttpResponse`、`Method`（仿 Hutool 链式 API）
- 新增 `HttpClientEngine` 连接池引擎（200 连接、20s KeepAlive、三级超时）
- **对外 API 完全一致，升级无需修改业务代码**

#### 新增 HTTP 客户端配置支持 ⭐
- 连接池配置：`max-total`、`max-per-route`、`evict-idle-seconds`、`time-to-live-seconds`
- 超时配置：`connect`、`socket`（响应超时）、`connection-request`
- 保活配置：`duration`、`enable-retry`
- 所有参数可通过 `application.yml` 的 `ai.http` 前缀配置
- 详见 [HTTP_CONFIG.md](HTTP_CONFIG.md)

### 🔧 其他改进
- 版本号规整为语义化版本 `1.1.0`
- pom 显式锁定 Java 8 编译配置（`maven-compiler-plugin` 3.8.1）
- `httpcore5` 前置覆盖尝试规避版本冲突（❌ 该做法写在 `dependencyManagement` 中未生效，即本版本依赖问题的成因）

### 📝 文档更新
- 新增 `HTTP_CONFIG.md` - HTTP 客户端配置详解
- 新增 `CHANGELOG.md` - 统一的版本更新日志
- 更新 `README.md` - 精简为最新版本说明
- 创建 `docs/` 目录结构，规范化文档组织

### ⚠️ 不兼容变更
无。**完全向后兼容历史版本。**

### 📦 升级说明
直接将依赖版本改为 `1.1.0` 即可。若你的工程此前依赖了本库**传递**的 Hutool（`cn.hutool.*`），由于本库已彻底移除该依赖，需在自己的工程中显式引入 `hutool-all`。

详细升级指南：[docs/migration/upgrade-to-1.1.0.md](docs/migration/upgrade-to-1.1.0.md)

---

## [1.0.9-GA] - 2024-XX-XX

### 新增特性
- ModelConfig 接口：解耦配置与实现，扩展自定义模型无需修改框架代码
- HTTP 工具类静态化：`HttpUtils` 和 `StreamHttpUtils` 改为静态方法，简化依赖注入
- 支持豆包（火山方舟）模型
- 新增 `@AiModelService` 注解，支持零侵入扩展自定义模型

### 改进
- 优化 Token 统计逻辑
- 增强参数校验
- 完善异常处理

详见：[docs/versions/v1.0.9.md](docs/versions/v1.0.9.md)

---

## [1.0.8] - 2024-XX-XX

### 新增特性
- 支持思考模式（Thinking Mode）
- 支持流式对话
- 新增 QWen-VL 多模态模型支持

---

## [1.0.7] - 2024-XX-XX

### 新增特性
- 支持 DeepSeek 模型
- 优化流式输出性能

---

## [1.0.6] - 2024-XX-XX

### 初始发布
- 支持通义千问、ChatGPT、讯飞星火
- 统一的 API 接口
- 同步/流式对话支持

---

## 版本规范

本项目采用[语义化版本](https://semver.org/lang/zh-CN/)规范：

- **主版本号（X）**：不兼容的 API 变更
- **次版本号（Y）**：向后兼容的功能新增
- **修订号（Z）**：向后兼容的问题修复

## 如何升级

查看对应版本的升级指南：
- [升级到 1.1.0](docs/migration/upgrade-to-1.1.0.md)

## 贡献指南

提交 Pull Request 前，请：
1. 阅读 [CLAUDE.md](CLAUDE.md) 了解项目规范
2. 确保代码通过 Java 8 编译
3. 保持向后兼容性
4. 更新相应文档
