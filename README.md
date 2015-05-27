task scheduler 
=====================

client ==> proxy ==>server

=====================
![http代理原理](http://7xizg2.com1.z0.glb.clouddn.com/otherhttp代理原理.jpg)

1. server(proxy.mock.workstation.HttpDelayServer),listen port 8081,every request return response after Random().nextInt(5000) ms
2. proxy(proxy.server.HttpServer),listen port 8080,forward every request to workstation
3. client(proxy.client.HttpClientBatch),sent reuqest to scheduler




