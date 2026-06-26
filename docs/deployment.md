# 部署说明

## 环境变量

### JWT_SECRET

生产环境必须设置 `JWT_SECRET` 环境变量，用于 JWT Token 签名。

```bash
export JWT_SECRET="$(openssl rand -base64 32)"
```

- 长度至少 256 位（32 字节）。
- 不同环境必须使用不同密钥。
- 不要将该密钥提交到代码仓库。

若未配置 `JWT_SECRET`，系统启动时会生成临时随机密钥并打印警告日志；这会导致服务重启后已有 Token 失效，因此仅适用于本地开发或测试。

## 开发环境

如需在开发环境使用固定默认值，可激活 `dev` profile：

```bash
java -jar -Dspring.profiles.active=dev yxtyg-admin.jar
```

开发默认值为 `dev-secret-change-in-production`，生产环境请勿使用。
