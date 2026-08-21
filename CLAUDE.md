# CLAUDE.md

本文件为 Claude Code 及协作者提供项目的总纲。修改代码或发布版本前请先通读。

## 项目概述

`jeesoul-ai-model` 是一个 **Spring Boot Starter**，将国内外主流大模型的 API 封装为统一调用方式。
使用者只需面对统一入参 `ModelRequestVO` 与出参 `ModelResponseVO`，切换模型仅需修改模型名字符串，业务代码无需改动。

- Maven 坐标：`com.jeesoul:jeesoul-ai-model`
- 已发布至 **Maven 中央仓库**（发布后版本不可变、不可删除）
- 性质：纯依赖库，非可运行应用
- 开源协议：MIT

## 技术栈

- Java 8（务必保持 Java 8 兼容，勿用更高版本语法）
- Spring Boot 2.7.17
- Spring WebFlux / Reactor（流式输出 `Flux`）
- Apache HttpClient 5.1.4 + HttpCore 5.1.5（同步 HTTP，内置封装于 `http` 包；声明版本对齐 Spring Boot 2.7 BOM，代码兼容 5.1.x ~ 5.6.x）
- Lombok、commons-lang3、slf4j

## 支持的模型

通义千问(qWen)、ChatGPT、讯飞星火(spark)、DeepSeek、通义千问视觉(qwenVL 多模态)、豆包(douBao)。
模型清单维护在 `constant/AiModel.java` 枚举中。

## 架构

调用链：

```
FactoryModelService.create(模型名)
        ↓ 先查 AiModel 枚举，未命中再查自定义注册表
AiStrategyContext（策略表：模型名 → Spring Bean 名）
        ↓ SpringContextHolder.getBean()（Spring 原生，SPI 注册）
XxxService extends AbstractAiService
        ↓ buildRequest → 调用
util/HttpUtils（同步，底层 Apache HttpClient 5.x） / StreamHttpUtils（流式/WebClient）
        ↓
各家大模型 HTTP 端点
```

设计模式：**工厂 + 策略 + 模板方法**。`AbstractAiService` 抽出参数校验、参数兜底、消息构建等公共逻辑，子类只实现差异部分。

四种调用模式（定义在 `AiService` 接口）：
- `httpChat` 同步、返回解析对象
- `streamChat` 流式、返回 `Flux<ModelResponseVO>`
- `httpChatRaw` / `streamChatRaw` 返回模型原始 JSON

## 本地开发环境（重要，构建/发布必读）

- **JDK**：Java 8，路径 `C:\Program Files\Java\jdk-1.8`（务必用此 JDK 构建，保持 Java 8 兼容）
- **另有 JDK 17**：路径 `D:\java\jdk\jdk-17`，仅用于兼容性验证，**不要用它构建发布**（javadoc 插件会失败，详见下「JDK 版本兼容性」）
- **Maven**：`D:\java\maven\apache-maven-3.8.8`
- **Maven settings**：发布与构建使用 `D:\java\maven\apache-maven-3.8.8\conf\settings-github.xml`（其中配置了中央仓库发布所需的 `mymaven` server 凭据，对应 pom 中 `central-publishing-maven-plugin` 的 `publishingServerId=mymaven`）
- **网络代理**：本地代理 `127.0.0.1:7897`。拉取中央仓库依赖受限时追加 `-DproxyHost=127.0.0.1 -DproxyPort=7897`

构建命令示例（bash，需先把 JAVA_HOME 指向 JDK8）：

```bash
export JAVA_HOME="/c/Program Files/Java/jdk-1.8"
"/d/java/maven/apache-maven-3.8.8/bin/mvn" \
  -s "/d/java/maven/apache-maven-3.8.8/conf/settings-github.xml" \
  clean compile -DskipTests
```

注意：pom 已显式配置 `maven-compiler-plugin` 锁定 source/target 为 Java 8；早期版本未配置时默认会按 source 1.5 编译导致 lambda 报错，勿回退此配置。

## JDK 版本兼容性（实测结论，勿凭猜测改动）

要分清两件事：**使用方用什么 JDK 跑**（无限制）和 **本库用什么 JDK 构建发布**（当前必须 JDK 8）。

### 使用方运行时：JDK 8 / 11 / 17 / 21 均可

本库编译产物是 Java 8 字节码（`major version 52`），高版本 JDK 向下兼容，可直接运行。
已用探针实测：在 JDK 17 运行时下加载 `HttpClientEngine`、构建连接池、发起请求，全部正常。
HttpClient5 直到 5.6.x 仍以 Java 8 为基线，不会因使用方 JDK 升级而失效。
JDK 17/21 使用方通常搭配 Spring Boot 3.x，其 BOM 把 httpclient5 管在 5.2/5.3，已在兼容矩阵覆盖范围内。

### 本库构建发布：**当前只能用 JDK 8**

用 JDK 17 实测构建本项目的结果：

| 环节 | JDK 8 | JDK 17 | 说明 |
|------|-------|--------|------|
| maven-compiler-plugin 3.8.1（source/target 8） | 通过 | 通过 | JDK 17 下仍能编出 major version 52 |
| maven-source-plugin 2.2.1 | 通过 | 通过 | 正常产出 sources jar |
| maven-gpg-plugin 1.5 | 通过 | 通过 | 正常产出 .asc 签名（此前预判会炸，实测未炸） |
| **maven-javadoc-plugin 2.9.1** | 通过 | **失败** | `ExceptionInInitializerError`，插件太老不认 JDK 9+ |
| maven-javadoc-plugin 3.6.3 | 未测 | 通过 | 实测可正常产出 javadoc jar |

要点：
- **唯一的阻塞点是 `maven-javadoc-plugin:2.9.1`**，编译器和 GPG 都没问题
- `-Dmaven.javadoc.skip=true` 对 2.9.1 **无效**（该参数是 3.x 才支持），JDK 17 下绕不过去
- javadoc jar 是中央仓库发布的必需产物，所以 JDK 17 下**无法完成发布**
- 若将来要改用 JDK 17/21 构建，需先把 javadoc 插件升到 **3.5.0+**（实测 3.6.3 可用），
  同时建议 compiler 升 3.11.0+、gpg 升 3.1.0+
- **升级 javadoc 插件的风险**：3.x 的 doclint 校验比 2.9.1 严格得多，会暴露现有注释里的新问题。
  这类改动**不要在临近发布时做**，应单独一个版本处理，避免再次浪费中央仓库额度
- **JDK 21 未实测**。预期与 JDK 17 一致（同为 JDK 9+ 模块化体系），但若真要换 21 构建，须重跑上表验证
- 结论：1.1.0 系列继续用 JDK 8 构建发布；插件升级排到后续独立版本

## 关键目录

- `service/` 各模型实现 + `AbstractAiService` 基类 + `AiService` 接口
- `config/` 自动配置类（每个模型一个 `XxxAutoConfiguration`，按 `@ConditionalOnProperty` 条件装配）+ `AiProperties` 配置属性
- `factory/` `FactoryModelService` 工厂入口
- `strategy/` `AiStrategyContext` 策略表
- `request/` `response/` 各模型的请求/响应体
- `vo/` 统一出入参 `ModelRequestVO` / `ModelResponseVO`
- `annotation/` `@AiModelService` 自定义模型注册注解
- `constant/` `AiModel` 模型枚举等
- `util/` `HttpUtils`（同步，门面 + Apache HttpClient 5.x 引擎）`StreamHttpUtils`（流式/WebClient）`JsonUtils` `SpringContextHolder`
- `http/` 内置 HTTP 封装：`HttpRequest`/`HttpResponse`/`Method`（仿 Hutool 链式门面）+ `engine/HttpClientEngine`（连接池引擎）+ `exception/HttpException`
- 自动装配声明：`resources/META-INF/spring/...AutoConfiguration.imports`

## 扩展新模型（不改框架源码）

1. 新建 Service 继承 `AbstractAiService`，实现抽象方法
2. 类上加 `@AiModelService(modelName="xxx")`，启动时自动扫描注册
3. 通过 `FactoryModelService.create("xxx")` 调用

内置新模型则需：在 `AiModel` 枚举登记 + 新增 `XxxService` + 新增 `XxxAutoConfiguration` + 在 `AiProperties` 加配置段。

## 铁律：HttpClient5 只能用 5.1.x 就存在的 API

**本库是被别人引入的 pom 依赖，HttpClient5 的最终版本仲裁权在使用方手里，不在我们手里。**

Maven 规则：**使用方继承的 `dependencyManagement`（如 `spring-boot-starter-parent`）优先级高于传递依赖的版本。**
使用方多为 Spring Boot 2.7.x，其 BOM 把 `httpclient5` 管在 **5.1.4**、`httpcore5` 管在 **5.1.5**。
所以无论本库 pom 里写多高的版本，传过去都会被改写成 5.1.4。

因此：
- 写 HTTP 相关代码**只能用 httpclient5 5.1.x 就存在的 API**，禁止使用 5.2+ 新增的类
- 典型雷区：`ConnectionConfig`（5.2 才引入）。1.1.0 ~ 1.1.0-beta2 三个版本因此在使用方运行期炸
- 连接超时用 `RequestConfig.setConnectTimeout`（5.2 后标记废弃但始终保留，是全区间唯一共有入口），
  Socket 超时用 `SocketConfig.setSoTimeout`，存活时长用 `PoolingHttpClientConnectionManagerBuilder.setConnectionTimeToLive`
- pom 中 httpclient5 版本**故意锁在 5.1.4**（对齐 SB 2.7 BOM），目的是让误用新 API 在本项目编译期就报错，
  而不是等使用方在生产环境炸。**勿以「版本太旧」为理由上调**
- 版本号由 `jeesoul.httpclient5.version` / `jeesoul.httpcore5.version` 属性控制（带前缀，避免与 SB BOM 同名属性混淆）
- httpclient5 与 httpcore5 是**独立发布的两个子项目**，补丁号各自推进，必须按官方配套关系成对使用：
  5.1.4↔5.1.5、5.2.3↔5.2.4、5.5.1↔5.3.6、5.6.4↔5.4.3（配套版本查 httpclient5-parent 的 `httpcore.version` 属性）
- 改动 `http` 包后，必须跨版本回归验证（四组均须通过）：

```bash
export JAVA_HOME="/c/Program Files/Java/jdk-1.8"
for c in "5.1.4 5.1.5" "5.2.3 5.2.4" "5.5.1 5.3.6" "5.6.4 5.4.3"; do set -- $c
  "/d/java/maven/apache-maven-3.8.8/bin/mvn" -s "/d/java/maven/apache-maven-3.8.8/conf/settings-github.xml" \
    -q clean compile -DskipTests -Djeesoul.httpclient5.version=$1 -Djeesoul.httpcore5.version=$2 && echo "$1 OK"
done
```

光编译通过不够，还要在目标运行时下真跑一次（编译期能过、运行期缺类是本次事故的原型）。
安全提示：5.1.x 分支已不收安全补丁，CVE-2026-64607 需 5.6.3+ 才修复。
本库不上调声明版本（保编译底线），改为在 README 告知使用方可自行覆盖，配套表同上。

教训：遇到使用方 `NoClassDefFoundError`，先查**那个类是哪个版本引入的**，再查使用方实际解析到的版本
（`mvn dependency:tree -Dincludes=org.apache.httpcomponents.client5:*`），
不要一上来就怀疑发布环节丢了依赖——1.1.0-beta / beta2 两次无效发布就是这么来的。

## HTTP 工具

- **同步 HTTP**：基于内置 Apache HttpClient 5.x 封装，位于 `http` 包。`http/HttpRequest`、`http/HttpResponse`、`http/Method` 是仿 Hutool `cn.hutool.http.*` 的同名同签名链式门面，底层委托 `http/engine/HttpClientEngine`（连接池单例：maxConn 200、KeepAlive 20s、三级超时、不重试、空闲回收）。`util/HttpUtils` 通过这套门面发起请求，公共 API 与历史版本完全一致。
- **流式 HTTP**：`util/StreamHttpUtils` 基于 Spring WebFlux `WebClient`，与同步链路是不同技术栈，**未纳入 Apache 封装**，保持独立。
- **设计来源**：引擎设计参考用户的 `jeesoul-httpclient`（4.x），用 5.x API 重新实现，非直接复制。
- 已彻底移除 Hutool 依赖。

## 当前版本进行中：1.1.0（尚未结束）

1.1.0 仍在迭代，本次迭代已包含的改动：
1. `SpringUtil` → 自研 `SpringContextHolder`（Spring 原生 `ApplicationContextAware` + SPI 注册）
2. 版本号规整为语义化 `1.1.0`，pom 显式锁定 Java 8 编译
3. Hutool HTTP → 内置 Apache HttpClient 5.x 封装（仿 Hutool 门面，无缝替换）
4. 彻底移除 Hutool 依赖
5. **修复 HttpClient5 版本兼容事故（beta3）**：`HttpClientEngine` 移除 5.2 才引入的 `ConnectionConfig`，
   改用 5.1.x 就有的等价 API，使本库在 httpclient5 5.1.x ~ 5.6.x 全区间可用，
   覆盖 Spring Boot 2.7.x / 3.x 使用方。详见上「铁律：HttpClient5 只能用 5.1.x 就存在的 API」
6. 已实测 JDK 8 与 JDK 17 运行时均正常；构建发布仍须用 JDK 8，详见上「JDK 版本兼容性」

发布过程记录（中央仓库额度已因误判浪费两次，务必引以为戒）：
- `1.1.0`、`1.1.0-beta`、`1.1.0-beta2` 均已发到中央仓库且不可撤回，三者在 Spring Boot 2.7.x 下都会报
  `NoClassDefFoundError: ConnectionConfig`，已在 README / CHANGELOG 标注废弃
- beta / beta2 是同一误判（以为发布遗漏依赖声明）下的两次无效发布，只改了版本号没改代码
- beta3 是第一个真正修掉根因的版本，验证充分后再发 1.1.1 正式版

以上全部要求向后兼容、对使用方零改动。版本未发布前如有后续改动，继续归入本次 1.1.0 迭代。

## 铁律：版本迭代必须兼容历史版本

**每次版本迭代升级，必须向后兼容历史版本。** 已有客户在生产环境使用历史版本，兼容才能让他们平滑升级。

- 不得删除或修改公共 API 的签名：`AiService` 接口方法、`FactoryModelService.create(...)`、`AiStrategyContext` 的 `getService`/`registerModel`/`isModelRegistered`、`ModelRequestVO`/`ModelResponseVO` 的已有字段、`@AiModelService` 注解属性等
- 重构只允许换内部实现，对外行为与签名保持不变（例：1.1.0 用 `SpringContextHolder` 替换 Hutool `SpringUtil`，对外零变化）
- 新增能力用「新增」而非「改动」的方式（加方法、加可选字段、加重载）
- 作为 Spring Boot Starter，注册的 Bean 不得与使用方 Bean 冲突，详见下「Bean 注入与冲突规避」
- **如果实在无法兼容**：必须在 `README.md` 中明确写出「升级方案 / 不兼容变更说明」，告知用户如何从旧版本迁移，并按 SemVer 升主版本号（X）

## Bean 注入与冲突规避（Starter 红线）

本项目是 pom 依赖组件，被引入使用方工程。Spring Boot 2.1+ 默认禁止 Bean 覆盖，一旦与使用方 Bean 撞名会导致使用方启动失败（`BeanDefinitionOverrideException`）。因此：

- 本库注册的 Bean 必须用**项目前缀命名**，避免通用名（如 `aiModelSpringContextHolder` 而非 `springContextHolder`）
- 对可能与使用方重复的 Bean 加 `@ConditionalOnMissingBean`，使用方已有同类型 Bean 时不重复注册
- 优先用自动配置 `@Bean` 显式注册，而非 `@Component` + 组件扫描（使用方扫描范围通常不含本库包 `com.jeesoul.ai.model`）
- 模型服务 Bean 受 `@ConditionalOnProperty` 控制，仅在使用方配置了对应 api-key 时才装配，天然降低冲突面

## 版本规范

采用**语义化版本 `vX.Y.Z`**（标准 SemVer）：
- X 主版本：不兼容的 API 变更
- Y 次版本：向后兼容的功能新增
- Z 修订号：向后兼容的问题修复

约定：
- **`pom.xml` 的 `<version>` 不带 `v` 前缀**（如 `1.1.0`），**Git tag 带 `v` 前缀**（如 `v1.1.0`）
- 历史版本（1.0.6 ~ 1.0.9-GA）保留原样，不补打 tag
- 历史遗留问题：旧提交信息里的 `1.1.0.9-GA` 四段式编号已废弃，从下个版本起统一切换到 SemVer，并确保 pom 版本与 tag 一致

## 分支规范（强制流程）

**每个新版本的迭代，必须从 `main` 分支切出一个以该版本号命名的分支进行开发**，禁止直接在 main 上迭代。

```bash
git checkout main
git pull origin main
git checkout -b 1.1.0        # 分支名 = 目标版本号（不带 v 前缀）
```

- 分支名与目标版本号一致（如 `1.1.0`），与历史分支（`1.0.6`~`1.0.9-GA`）的命名惯例保持一致
- 该版本的所有开发、提交都在此分支上进行
- 开发完成、测试通过后再合并回 `main`，然后在 main 上打 tag 并发布

## 发布流程（每次迭代版本发布必须执行）

每发布一个版本到 Maven 中央仓库，**必须打对应 Git tag**。分支 + tag 双保险：分支用于维护和 hotfix，tag 精确标记发到中央仓库的那一版快照。

步骤：

1. 从 `main` 切出版本分支（见上「分支规范」），在该分支完成开发
2. 确认 `pom.xml` 的 `<version>` 已更新为目标版本（如 `1.1.0`，不带 `v`）
3. 将版本分支合并回 `main`
4. 在 `main` 对应发布的 commit 上打带注释的 tag：
   ```bash
   git tag -a v1.1.0 -m "Release v1.1.0"
   git push origin v1.1.0
   ```
5. 执行 Maven 发布（中央仓库发布插件 + GPG 签名，配置见 pom.xml 的 `central-publishing-maven-plugin`）
6. 在 GitHub Releases 基于该 tag 创建 Release 说明

要点：
- 新版本一律先从 main 切版本分支开发，不在 main 直接改
- **tag 命名固定为 `v` + pom 版本号**，如 pom 是 `1.1.0` 则 tag 为 `v1.1.0`
- **分支名 = 版本号（不带 v）**，**tag 名 = v + 版本号**
- 中央仓库版本不可撤回，发布前务必确认版本号正确、pom 与 tag 一致
- 不要删除任何历史版本分支或 tag

## 已知待办 / 注意事项

- `resources/META-INF/spring.factories` 与 `AutoConfiguration.imports` 并存；`spring.factories` 在 Spring Boot 2.7 已废弃，可择机只保留后者
- `src/test` 下暂无测试代码，对外发布库建议补充
- 保持 Java 8 兼容，勿引入高版本语法或 API
- **构建插件版本偏旧，待独立版本升级**：`maven-javadoc-plugin` 2.9.1（2013）、`maven-gpg-plugin` 1.5（2014）、`maven-compiler-plugin` 3.8.1（2019）。其中 javadoc 2.9.1 在 JDK 17 下直接失败，是本库无法用 JDK 17 发布的唯一原因。升级时注意 javadoc 3.x 的 doclint 更严格，需同步清理注释，务必单独一个版本做，勿与功能改动混在一起发布

## Javadoc 注释规范（发布中央仓库强制）

发布到 Maven 中央仓库会执行 `maven-javadoc-plugin` 生成 javadoc 并校验，**注释中出现不规范的 HTML 标签或标签类会导致校验失败、发布被拒**。因此注释必须遵守：

- **禁止使用 HTML 标签**：如 `<p>`、`<pre>`、`<ul>`、`<li>`、`<code>`、`<br>` 等，一律用纯文本和换行表达
- **新增代码禁止引入 javadoc 内联标签**：如 `{@link}`、`{@code}`（历史代码中零星存在，不强制清理，但新代码不再引入，避免链接失效导致校验报错）
- 注释风格与项目既有代码保持一致：类/方法用标准块注释，方法注明 `@param`/`@return`/`@throws`，作者与日期用 `@author`/`@date`
- 描述用简洁中文纯文本，多个要点用多行普通文字罗列，不要用列表标签

## Bean 注册方式约定

- 需要随 Starter 自动加载的配置类、上下文持有类等，**通过 Spring Boot 自动配置 SPI 注册**：写入 `resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（旧版兼容则同步写 `spring.factories`）
- 不要为了注册一个工具类去污染 `AiAutoConfiguration` 等已有配置类（不随意往里塞 `@Bean`）
- 通过 SPI imports 注册的类，Bean 名为全限定类名，天然不与使用方 Bean 冲突，无需额外起前缀名或加 `@ConditionalOnMissingBean`
- 参考：1.1.0 的 `SpringContextHolder` 即为此模式（类上 `@Configuration` + 实现 `ApplicationContextAware`，由 imports 文件注册），与 Hutool SpringUtil 的注册机制一致

## 给 AI 协作者的约束

- 修改公共 API（`AiService` 接口、`ModelRequestVO`/`ModelResponseVO` 字段）属于破坏性变更，需提示并按 SemVer 升主/次版本
- 涉及发布、打 tag、推远程等对外可见操作前，先与用户确认
- 提交信息使用中文，风格与项目历史一致
- **提交边界**：新增且需纳入版本管理的文件，只做 `git add`，**不做 `git commit`**——提交由用户本人执行。改动完成后把该 add 的文件 add 进暂存区即可，不要自行创建提交。
