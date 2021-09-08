<h1 align="center"> GLOCASH PAY </h1>
<p align="center"> glocash payment SDK for SpringBoot</p>
<h3 align="center"> <a target="_blank" href="https://docs.glocash.com">文档地址</a> </h3>



配置信息
```
在application 将配置信息填入 
具体信息请查看商户后台技术对接模块
位置 glocash\src\main\resources\application.yml

glocash:
  mchEmail: 2101653220@qq.com
  liveMode: true
  debug: true
  sandbox:
    url: https://sandbox.glocashpayment.com
    key: 9dc6a0682d7cb718fa140d0b8017a01c4e9a9820beeb45da020601a2e0a63514
  live:
    url: https://pay.glocashpayment.com
    key: c2e38e7d93dbdd3efaa61028c3d27a1a2577df84fa62ae752df587b4f90b8ef7
```

## 实例使用

本实例使用为java1.8 版本
配置好以后 使用maven 更新相关jar包 然后即可直接使用

### 交易创建页面
```
GET http://localhost:8080/transaction/create/{{orderId}}
```
### 交易查询页面
```
GET http://localhost:8080/transaction/query/{{gcid}}
```

### 交易异步通知页面
```
POST http://localhost:8080/transaction/notify
```

### 支付成功页面 可以自定义
```
POST http://localhost:8080/transaction/success/{{tns_id}}
```

### 支付失败页面
```
POST http://localhost:8080/transaction/failed/{{tns_id}}
```

### 交易退款页面
```
GET http://localhost:8080/transaction/refund/{{gcid}}/{{amount}}
```
