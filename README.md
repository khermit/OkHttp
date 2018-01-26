# OkHttp
collect info for sdn

# 主要用到的技术
 - service以及service与activity通信
 - okhttp
 - FTP下载
 - wifi管理

# 一、service以及service与activity通信

## （一）、MyService

    在activity中启动并绑定service，注意service仍然属于activity的主线程，不能直接在service中执行耗时操作，必须在service中新建线程来执行耗时操作（FTP、okhttp、wifi等）。
    一般操作顺序：启动服务、绑定服务 -> 解绑服务、停止服务。

## （二）、MyService与activity通信

### A.将activity中的值传递到MyService中

 - 1、在MyService中新建一个Binder类，继承自android.os.Binder。并在该类中实现一些方法。
 - 2、在MyService中重写的onBind方法中，new一个Binder类实例，并返回。即将该实例返回给activity中的onServiceConnected方法的参数。
 - 3、在服务绑定成功时，将执行onServiceConnected方法。该方法传入的iBinder访问到的是MyService中onBind()的返回值。故可以在activity中通过该方法得到MyService中的Binder。
 - 4、activity中直接调用Binder的方法，将参数传入Binder的方法中即可。在activity中调binder.getService().setCallback(new MyService.Callback()用Binder的方法，等用于在Service中调用Binder的方法。
 - 5、Binder方法：传入的参数来自activity的调用，方法内部将参数赋值给MyService的成员，即实现将activity中的值传递到MyService中。

### B.将MyService中的信息传递到activity中

 原理：通过监听实现，将MyService中动态变化的值返回给activity。通过Binder将MyService.this传入activity中，以便在activity中实现MyService中定义的回调接口。
 - 1、在MyService中定义一个Callback接口,并在里面定义onDataChange(String data)函数。
 - 2、在MyService的线程中执行callback.onDataChange(myData)，即将MyService的数据myData传入，调用监听函数。
 - 3、在MyService的Binder类中实现getService()方法，该方法返回MyService.this。
 - 4、在activity的onServiceConnected方法中，得到MyService.this并实现Callback接口，即binder.getService().setCallback(new MyService.Callback(),重写onDataChange(String data)方法,此方法的data参数即为MyService传递过来的。
 - 5、在activity重写的onDataChange方法中得到MyService的数据，并通过Handler更新UI。