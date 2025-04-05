# SuperHero API gRPC Proxy (Kotlin)

## 项目简介
本项目实现了一个 **gRPC** 服务，作为 [SuperHero API](https://superheroapi.com/) 的代理层。它支持：
1. **searchHero** —— 将 `GET /api/{token}/search/{name}` 转为 gRPC 调用。
2. **本地缓存** —— 提升重复查询的响应速度，可配置过期时间，暴露 *cache hit/miss* 指标。
3. **异步刷新** —— 通过后台轮询监听 SuperHero API 更新，自动刷新缓存并通过 *Server Streaming* 主动推送关键词变更给客户端。



## 目录结构 / Project Structure
```
├── src/main/kotlin
│   ├── cache            # 本地缓存实现
│   ├── client           # 调用 SuperHero REST API
│   ├── daemon           # 轮询刷新与推送
│   ├── service          # gRPC 服务实现
│   └── utils            # 工具类
├── src/test/kotlin      # 单元 / 集成测试
├── proto                # gRPC 协议
├── build.gradle.kts     # 构建脚本
└── application.conf     # 配置文件
```

---

## 快速开始 / Quick Start
### 1 . 准备配置 (application.conf)
```hocon
server {
  grpc_port       = 50051
  grpc_host       = "0.0.0.0"
  secret          = "<aes‑cipher‑text>"   # 由 getAccessToken() 解密
  expire_time     = 300                       # 秒
  interval_millis = 60000                     # 轮询间隔,单位毫秒
  api_website     = "https://superheroapi.com/api"
  prometheus_port = 9090
}
```

### 2 . 构建并运行
```bash
./gradlew clean build            # 编译 & 运行全部测试
./gradlew run                    # 启动 gRPC 服务
```

---

## gRPC API
| Method | Type | Request | Response |
|--------|------|---------|----------|
| `searchHero` | Unary | `SearchHeroRequest { string name }` | `SearchHeroResponse` |
| `subscribeUpdates` | Server Streaming | `SubscribeRequest {}` | `SubscribeResponse { string keyword }` |

---

## 缓存策略 / Cache Policy
* 键：搜索关键词 (`name`)
* 值：`SearchHeroResponse` 的 JSON 序列化 ( 即 superheroapi 返回的结果)
* 失效：`expire_time` 秒后自动删除
* 指标：
    * `cache_hits_total`
    * `cache_misses_total`

Prometheus metrics 暴露在 `http://<host>:<prometheus_port>/metrics`。

---

## 异步更新 / Async Refresh
`UpdatePoller` 使用协程在后台定时拉取最新数据：
1. 计算新旧结果的 MD5；若发生变化则更新缓存。
2. 通过 `ChannelBasedFlowManager.notifyAll()` 将变更关键词推送给所有 `subscribeUpdates` 订阅者。

该流程完全非阻塞，不会影响前台查询延迟。

---

## 测试 / Testing

### 1. 手动功能验证 (Manual Functional Verification)

#### 1.1 验证 gRPC 接口与缓存
1. 在 `resources/application.conf` 中引用 `application-superhero.conf`。
2. 启动服务后，使用 **BloomRPC** 或 `grpcurl` 调用 `searchHero`，确认返回正常且日志显示已调用外部 API。
3. 以相同参数再次调用 `searchHero`，响应应显著加快，日志中出现 *cache hit* 记录，证明缓存生效。

#### 1.2 验证异步缓存刷新
1. 将 `resources/application.conf` 切换为 `application-local.conf`。
2. 启动 `test/mock_web/mock_super_hero.kt`，该模拟服务在请求次数为偶数时返回新数据，奇数时返回旧数据。
3. 启动主服务并首次调用 `searchHero` 写入缓存。
4. 观察日志：`UpdatePoller` 周期性拉取远端数据，计算 MD5 并与缓存比对；当发现差异时会刷新缓存并通过 `ChannelBasedFlowManager.notifyAll()` 推送通知给所有 `subscribeUpdates` 订阅者。

---

---

## 依赖 / Dependencies
* Kotlin 1.9
* gRPC Kotlin
* kotlinx‑coroutines
* OkHttp 4
* Prometheus client
* JUnit 5 & kotlinx‑coroutines‑test

---

