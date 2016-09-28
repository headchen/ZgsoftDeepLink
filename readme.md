#Deep Link Cordova插件

通过UrlSchema来实现 deeplink的cordova 插件  

# Installation(安装)  
> cordova plugin add cordova-plugin-zgsoftDeepLink --variable URL_SCHEME=myapp

设置合适的Url_Schema以便使得您的应用以如下形式调用：

> myapp://xxxxx    

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