#Deep Link Cordova插件

通过UrlSchema来实现 deeplink的cordova 插件  

# Installation(安装)  
> cordova plugin add cordova-plugin-zgsoftDeepLink --variable URL_SCHEME=myapp

设置合适的Url_Schema以便使得您的应用以如下形式调用：

> myapp://xxxxx    

# IOS的配置

IOS除了安装插件到指定位置之外，需要手动修改如下内容：  

1. 编辑-info.plist ，增加：ZgsoftDeferredDeepLinkUrl  http://dev.jxcsoft.com/webapi/test.html（把这个替换为恢复链接的相关地址，在此地址中读取cookie并转向：urlscheme://xxxxx即可）。
2. 编辑-info.plist: 增加 URL types/item 0/URL Schemes/item 0: ygyjs(这里为响应的CustomScheme)
3. Universal Link: 配置，请参阅苹果响应的文档即可。

# 如何调用  

可以在home加载完毕之后进行「订阅」即可，比如：在homeController.js 中最后加上如下代码即可

`````` javascript
window.cordova.exec(
    function(json)
     {
       if(json.fragment)
         {
         var myregexp = /spmx\/(\w+)$/i;
       var result=myregexp.exec(json.fragment);
       if(result)
        $scope.doSpmxWithBh(result[1]);
       }
     },
     
     function(error){
            console.log(error);
        },
     "ZgsoftDeepLink","jsSubscribe",[]);

``````