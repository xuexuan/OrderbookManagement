# OrderbookManagement

## This project invovle:-
* 1, NOSQL(mongoDB) design, using embedded document structure to handle one-many relation.
for example, user portfolio desgin as user id with embedded order list.
* 2, javax.websocket, each connection will create a serverendpoint object, and tomcat handle these connection with NIO. for OnMessage annotation, when the client not yet complete the request, the other upcoming onmessage request will on hold.
* 3, With spring boot framework, it will fail to inject component inside serverendpoint, which mean "autowried" will get null object.
* 4, Order matching algorithm, using the FIFO at this moment, as not many user at this moment, so each match will trigger a tick update. a tick update will include whole buy and sell orders.
* 5,the price is decided by buy side, take the matched buy order price as the current price.

* https://www.zhihu.com/question/26950456#:~:text=tick%E8%A1%8C%E6%83%85%E5%8F%88%E7%A7%B0%E9%80%90,%E7%B2%BE%E7%BB%86%E5%92%8C%E5%AE%8C%E6%95%B4%E7%9A%84%E6%95%B0%E6%8D%AE%E3%80%82
* https://www.hkex.com.hk/Services/Trading/Securities/Overview/Trading-Mechanism?sc_lang=zh-HK
* https://www.cmegroup.com/education/matching-algorithm-overview.html
* https://wizardry.liberty.me/how-to-build-a-bitcoin-exchange-part-1-design-goals-risk-management/
* https://github.com/matthiaszimmermann/order-matching-demo
* https://yq.aliyun.com/articles/710640
* https://medium.com/lgogroup/a-matching-engine-for-our-values-part-1-795a29b400fa

## Some thoughts of this development:-
* 1, Design the strcture store in the DB first, then control logic(how react to client request, ack, real time respond), then business logic(order matching algorithm)
