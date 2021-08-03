# 项目综述
一个交易机器人，主要用于加密货币相关。
基于springboot，无状态。
代码分两部分，java下的全是从Binance提供的SDK项目拷贝源码，用于方便修改实现，补充缺失的api
kotlin是主体代码

目前支持以下功能
1. binance平台
2. telegram，weixin，dingding的通知，telegram进行相关策略启动停止，以及额外的查下控制等功能。
3. 依托ta4j实现各个交易策略，目前已经有supertrend，2B形态识别，leigong等等
4. 支持回测，依赖jfreechart展示回测结果
5. 其他杂碎功能，例如bybt灰度购买量查询，VPVR计算，最大跌幅和最大恢复的排名

目前缺失
1. 自动下单买卖
2. UI操作页面（目前仅通过telegram交互）
3. 测试
4. 股票数据源
5. ftx交易api封装
6. 持久化部分（目前暂无需要就是）

# 项目初始化配置
1. 在src/main/resources下创建secret.properties
2. secret.properties内添加下列配置
```properties
#binance API secretKEY  APIKEY
tradebot.binanceSECRET_KEY_ONLINE=
tradebot.binanceAPI_KEY_ONLINE=

#binance testnet API secretKEY
tradebot.binanceSECRET_KEY_TESTNET=
tradebot.binance_API_KEY_TESTNET=

#wx callback
tradebot.wxCallbackURL=
#dingding callback
tradebot.DingdignCallbackURL=
#telegram bot token user groupid
tradebot.telegraBOT_TOKEN=
tradebot.telegraBOT_USDERNAME=
tradebot.telegraMsgGroupId=
```
**注意**

其中```tradebot.binanceSECRET_KEY_ONLINE```和```tradebot.binanceAPI_KEY_ONLINE```是必填项！否则项目启动异常
需要开发者去Binance上申请自己的APIKEY的SecretKey

其他配置项属于可选配置，按需进行配置，留空则模块不启用。

这里写配置字符串前后不要加引号

# 关于交易策略
所有交易策略都放置在tradebot/src/main/kotlin/com/pecpwee/tradebot/strategy下
所有策略都有共同父类AbsStrategy
子类上标注了@Component就会被自动注册到StrategyManagerCenterService里并运行

## 怎么上新策略

参考AbsStrategy的子类，例如简单一些的有MACrossStrategy（MACD快慢均线交叉买入买出策略）
实现一个策略大概有以下步骤

1. 继承AbsStrategy
2. 实现buildStrategy方法，这里是用于执行策略主体，可以参考目前已有代码实现，或ta4j项目的例子
3. 实现getBarReuqestConfig方法,这里可以指定策略下列内容
   1. 交易对，例如BTCUSDT，ETHUSDT等等
   2. 时间间隔，例如5min，10min 
   3. 这里可以指定是否是回测模式，如果开启则执行时会立即进行回测并展示图形
4. 策略类上加上@Components注册策略
5. 进行策略回测，查看效果（成功率，最大回撤等指标），解bug
6. 在testnet测试网测试
7. 实盘

# 调试和部署
## 调试
IDEA直接run即可

## Docker编译和部署

```
./gradlew clean bootjar --stacktrace --no-daemon
docker build -t user/reponame .
```