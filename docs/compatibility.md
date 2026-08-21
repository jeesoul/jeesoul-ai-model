# 运行环境与依赖兼容性

本文说明 jeesoul-ai-model 在不同 JDK、Spring Boot、Apache HttpClient5 版本下的兼容情况，
以及有安全合规要求时如何自行升级 HttpClient5。

只是想快速接入的，看 [README](../README.md) 就够了。本文面向需要做版本仲裁、安全扫描、
合规评估的场景。

## 一、JDK 与 Spring Boot

本库编译产物为 **Java 8 字节码**（class major version 52），高版本 JDK 向下兼容，
可直接运行，无需任何额外配置。

| 使用方环境 | 是否支持 | 说明 |
|-----------|---------|------|
| JDK 8 | 支持 | 已实测 |
| JDK 11 | 支持 | Java 8 字节码向下兼容 |
| JDK 17 | 支持 | 已实测，连接池构建与请求发起均正常 |
| JDK 21 | 支持 | Java 8 字节码向下兼容 |
| Spring Boot 2.7.x | 支持 | 已实测，其 BOM 将 httpclient5 管在 5.1.4 |
| Spring Boot 3.x | 支持 | 其 BOM 将 httpclient5 抬到 5.2 / 5.3，均在兼容区间内 |

上表说的是「使用方运行本库」的兼容性。本库自身构建发布仍必须使用 JDK 8
（受 maven-javadoc-plugin 版本限制），这与使用方无关。

## 二、HttpClient5 版本兼容

本库同步 HTTP 基于 Apache HttpClient5，pom 中声明 `httpclient5:5.1.4` + `httpcore5:5.1.5`，
与 Spring Boot 2.7.x 的 BOM 完全一致，这是 Apache 官方配套的版本组合。

### 版本的最终决定权在使用方手里

若你的项目继承 `spring-boot-starter-parent`，Maven 的规则是**继承的 `dependencyManagement`
优先级高于传递依赖**，所以无论本库声明什么版本，实际生效的都是你的 BOM 管理的那个版本。

本库声明 5.1.4 有两个目的：与主流使用环境对齐，以及作为**编译底线**——
按最低支持版本编译，任何误用高版本 API 的代码都会在本库编译期直接报错，
而不是等使用方在生产环境炸。

自 1.1.0-beta3 起，本库代码只使用 5.1.x ~ 5.6.x **全区间共有**的 API，
无论最终仲裁到哪个版本都能正常工作。

### 官方配套关系

`httpclient5` 与 `httpcore5` 是 Apache HttpComponents 下**独立发布**的两个子项目，
补丁号各自推进，**必须成对匹配，混搭会导致运行期找不到类**。

| httpclient5 | 配套 httpcore5 | 说明 |
|-------------|----------------|------|
| 5.1.4 | 5.1.5 | 本库默认，对齐 Spring Boot 2.7.x BOM |
| 5.2.3 | 5.2.4 | 已实测 |
| 5.5.1 | 5.3.6 | 已实测 |
| 5.6.4 | 5.4.3 | 已实测，含 CVE-2026-64607 修复，仍兼容 Java 8 |

配套版本可查 httpclient5-parent 的 `httpcore.version` 属性确认。
本库已实测上表全部四组运行时组合，编译与运行均正常，升级不影响功能。

## 三、已知 CVE 与自行升级

**CVE-2026-64607**：响应带非法 `Content-Encoding` 时连接不归还连接池，
可导致连接耗尽的拒绝服务。影响 5.0-alpha1 ~ 5.6.2 全部版本，修复版本为 **5.6.3**。

5.1.x 分支已不再收到安全补丁。有安全扫描或合规要求的项目，
可在自己项目的 `<dependencies>` 中**直接声明**更高版本覆盖：

```xml
<!-- 按需升级 HttpClient5，本库在 5.1.x ~ 5.6.x 全区间均可正常运行 -->
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>5.6.4</version>
</dependency>
<dependency>
    <groupId>org.apache.httpcomponents.core5</groupId>
    <artifactId>httpcore5</artifactId>
    <version>5.4.3</version>
</dependency>
<dependency>
    <groupId>org.apache.httpcomponents.core5</groupId>
    <artifactId>httpcore5-h2</artifactId>
    <version>5.4.3</version>
</dependency>
```

注意必须写在 `<dependencies>` 里。写进 `<dependencyManagement>` 对传递依赖无效——
只有直接声明的依赖优先级才高于继承的 BOM。

改完执行下面命令，确认最终仲裁结果是你想要的版本：

```bash
mvn dependency:tree -Dincludes=org.apache.httpcomponents.client5:*,org.apache.httpcomponents.core5:*
```

## 四、旧版本补丁依赖必须删除

1.1.0 / 1.1.0-beta / 1.1.0-beta2 在 Spring Boot 2.7.x 下运行期会报
`NoClassDefFoundError: ConnectionConfig`（根因：代码用了 5.2 才引入的 `ConnectionConfig`，
而使用方 BOM 把版本仲裁成 5.1.4）。当时的应急方案是让使用方手工补三个依赖。

**升级到 1.1.0-beta3 后，这三个依赖必须删掉**，不是「留着也行」：

1. **会把你锁死在有漏洞的版本上** —— `5.2.3` 落在 CVE-2026-64607 的影响区间内
2. **会屏蔽 BOM 的后续安全更新** —— 直接声明的版本优先级最高，
   以后你升级 Spring Boot 时，BOM 抬高的 httpclient5 版本会被这三行**静默顶掉**，
   你以为升级了，实际还停在 5.2.3
3. **留着不会立刻报错，所以很容易被忘记**，这正是它危险的地方

要删除的内容与验证方式见 [v1.1.0-beta3 版本说明](versions/v1.1.0-beta3.md)。

## 五、排查思路

遇到 `NoClassDefFoundError` / `ClassNotFoundException`，按这个顺序查：

1. 先查**那个类是哪个版本引入的**（例：`ConnectionConfig` 是 5.2 引入的）
2. 再查使用方**实际解析到的版本**：`mvn dependency:tree -Dincludes=org.apache.httpcomponents.client5:*`
3. 对照上面的配套关系表，确认 httpclient5 与 httpcore5 没有混搭

不要一上来就怀疑发布环节丢了依赖。1.1.0-beta / beta2 两次无效发布就是这么来的——
发布的 pom 里依赖一直都在，真正原因是 Maven 的版本仲裁规则。

## 相关文档

- [README](../README.md) - 快速开始与配置
- [HTTP 客户端配置详解](../HTTP_CONFIG.md) - 连接池、超时参数
- [v1.1.0-beta3 版本说明](versions/v1.1.0-beta3.md) - 本次修复详情
- [升级到 1.1.0-beta3 指南](migration/upgrade-to-1.1.0.md) - 从旧版本迁移

