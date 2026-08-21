# 更新日志

本文档记录 jeesoul-ai-model 的所有版本更新历史。

## [1.1.0-beta3] - 2026-08-20

### 🔥 真正修复 HttpClient5 兼容性问题

前两次 beta 修错了方向：只改 pom 依赖版本、没改代码，因此问题依旧。本次从代码层面修掉根因。

#### 根因（推翻此前判断）
- **现象**：Spring Boot 2.7.x 使用方运行期报 `NoClassDefFoundError: org/apache/hc/client5/http/config/ConnectionConfig`
- **此前误判**：以为是「发布时遗漏依赖声明」。经核查，发布到中央仓库的 pom 里
  `httpclient5` / `httpcore5` / `httpcore5-h2` 三个依赖一直都在，发布环节没有问题
- **真正原因**：
  - `ConnectionConfig` 是 httpclient5 **5.2 才引入**的类，本库 `HttpClientEngine.buildClient()` 用了它
  - 使用方继承 `spring-boot-starter-parent:2.7.17`，其 BOM 把 `httpclient5` 管在 **5.1.4**
  - Maven 规则：**使用方继承的 dependencyManagement 优先级高于传递依赖的版本**，
    本库声明的 5.2.3 传过去后被强制改写为 5.1.4
  - 5.1.4 中没有 `ConnectionConfig`，于是运行期找不到类
- **结论**：版本的最终仲裁权在使用方 BOM 手里，只要库代码依赖 5.2+ 的 API，改版本号无法解决

#### 修复内容
- **代码**：`HttpClientEngine` 移除 `ConnectionConfig`，改用 5.1.x 就存在的等价 API
  - `ConnectionConfig.setSocketTimeout` → `SocketConfig.setSoTimeout`
  - `ConnectionConfig.setConnectTimeout` → `RequestConfig.setConnectTimeout`
  - `ConnectionConfig.setTimeToLive` → `PoolingHttpClientConnectionManagerBuilder.setConnectionTimeToLive`
- **pom**：httpclient5 声明版本对齐 Spring Boot 2.7.x BOM（`5.1.4` / `httpcore5 5.1.5`），
  按最低支持版本编译，后续误用 5.2+ 新 API 会在编译期报错而非使用方运行期报错
- **pom**：版本号抽为 `jeesoul.httpclient5.version` / `jeesoul.httpcore5.version` 属性，
  便于跨版本回归验证（带 `jeesoul` 前缀，避免与 Spring Boot BOM 的同名属性混淆）

#### 兼容性验证
已实测编译 + 运行双重验证，四组运行时组合均正常：

| httpclient5 | httpcore5 | 编译 | 运行 |
|-------------|-----------|------|------|
| 5.1.4       | 5.1.5     | 通过 | 通过 |
| 5.2.3       | 5.2.4     | 通过 | 通过 |
| 5.5.1       | 5.3.6     | 通过 | 通过 |
| 5.6.4       | 5.4.3     | 通过 | 通过 |

并确认编译产物字节码中已无 `ConnectionConfig` 引用（`target/classes` 与打包后的 jar 内均已核查）。
另外搭建了一个继承 `spring-boot-starter-parent:2.7.17` 的临时使用方工程，
不加任何补丁依赖直接引入本版本，`dependency:tree` 显示 httpclient5 被 BOM 仲裁为 5.1.4，
连接池仍构建成功——即复现了使用方原始报错场景并确认已修复。

#### JDK 版本兼容性（实测）
- **使用方运行时**：编译产物为 Java 8 字节码（`major version 52`），
  已实测 JDK 8 与 **JDK 17** 下加载引擎、构建连接池、发起请求全部正常；JDK 11/21 向下兼容同样可用
- **本库构建发布**：仍须使用 JDK 8。JDK 17 下 `maven-javadoc-plugin:2.9.1` 抛
  `ExceptionInInitializerError` 导致构建失败（该插件不认 JDK 9+），而 javadoc jar 是中央仓库必需产物。
  实测 compiler 3.8.1、source 2.2.1、gpg 1.5 在 JDK 17 下均正常，javadoc 插件是唯一阻塞点；
  升到 3.6.3 可解决，但 3.x 的 doclint 更严格，留待后续独立版本处理

### ⚠️ 重要提示
使用方**不再需要**手工补 `httpclient5` / `httpcore5` 依赖，之前加的补丁依赖可以删除。
业务代码零改动。稳定后将发布 1.1.1 正式版。

---

## [1.1.0-beta2] - 2026-08-20 ⚠️ 已废弃

> ⚠️ **警告**：此版本只改了版本号、未改代码，运行期仍报 `NoClassDefFoundError: ConnectionConfig`。
>
> 当时误判为「发布遗漏依赖」，实际根因是代码使用了 httpclient5 5.2+ 才有的 API。
>
> **请使用 1.1.0-beta3。**

---

## [1.1.0-beta] - 2026-08-20 ⚠️ 已废弃

> ⚠️ **警告**：此版本运行期报 `NoClassDefFoundError: ConnectionConfig`，与 1.1.0 同因。
>
> **请使用 1.1.0-beta3。**

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
