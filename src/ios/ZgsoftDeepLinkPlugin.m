//
//  ZgsoftDeepLinkPlugin.m
//  YGYJS
//
//  Created by zgsoft on 16/9/13.
//
//

#import "ZgsoftDeepLinkPlugin.h"


@implementation ZgsoftDeepLinkPlugin

CDVPluginResult *_storedEvent;
NSString * _subscriber;

+ (BOOL) isFirstRun {
    if ([[NSUserDefaults standardUserDefaults] boolForKey:@"isFirstRun"])
    {
        return true;
    }
    else
    {
        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"isFirstRun"];
        [[NSUserDefaults standardUserDefaults] synchronize];
        return false;
    }
}

#pragma mark Public API

- (void)pluginInitialize {
    [self localInit];
    // Can be used for testing.
    // Just uncomment, close the app and reopen it. That will simulate application launch from the link.
    //    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onResume:) name:UIApplicationWillEnterForegroundNotification object:nil];
}

//- (void)onResume:(NSNotification *)notification {
//    NSUserActivity *activity = [[NSUserActivity alloc] initWithActivityType:NSUserActivityTypeBrowsingWeb];
//    [activity setWebpageURL:[NSURL URLWithString:@"http://site2.com/news/page?q=1&v=2#myhash"]];
//
//    [self handleUserActivity:activity];
//}

- (BOOL)handleUserActivity:(NSUserActivity *)userActivity {
    [self localInit];
    
    NSURL *launchURL = userActivity.webpageURL;
    NSLog(@"ZgsoftDeepLinkPlugin: Handle continueUserActivity (internal) %@", launchURL);
    
    [self storeEvent:launchURL];
    
    return YES;
}



#pragma mark Private API

- (void)localInit {
}

- (void)onAppTerminate {
    _subscriber = nil;
    [super onAppTerminate];
}


- (void)handleOpenURL:(NSNotification*)notification;
{
    
    NSURL* url = [notification object];
    
    if (url != nil) {
        [self storeEvent:url];
    }

}

/**
 *  Store event data for future use.
 *  If we are resuming the app - try to consume it.
 *
 *  @param host        host that matches the launch url
 *  @param originalUrl launch url
 */
- (void)storeEvent : (NSURL *)originalUrl {
    
    if(originalUrl == nil)
        return;
    
     _storedEvent = [self createResult:originalUrl];
    [self tryToConsumeEvent];
}


#pragma mark Methods to send data to JavaScript

/**
 *  Try to send event to the web page.
 *  If there is a subscriber for the event - it will be consumed.
 *  If not - it will stay until someone subscribes to it.
 */
- (void)tryToConsumeEvent
{
    
    if (_subscriber == nil || _storedEvent == nil)
    {
        return;
    }
    
    [self.commandDelegate sendPluginResult:_storedEvent callbackId:_subscriber];
    _storedEvent = nil;
    
}

- (NSString *)URLDecode:url
{
    NSString *result = [(NSString *)url stringByReplacingOccurrencesOfString:@"+" withString:@" "];
    result = [result stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    return result;
}

- (CDVPluginResult *)createResult:(NSURL *)url {
    NSDictionary* data = @{
                           @"url": [url absoluteString] ?: @"",
                           @"path": [url path] ?: @"",
                           @"queryString": [self URLDecode : [url query] ?: @""],
                           @"scheme": [url scheme] ?: @"",
                           @"host": [url host] ?: @"",
                           @"fragment": [url fragment] ?: @""
                           };
    
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:data];
    [result setKeepCallbackAsBool:YES];
    return result;
}


#pragma mark Methods, available from JavaScript side

- (void)jsSubscribe:(CDVInvokedUrlCommand *)command {
    
    _subscriber = command.callbackId;
    [self tryToConsumeEvent];
}

- (void)jsUnsubscribe:(CDVInvokedUrlCommand *)command {
    
    _subscriber = nil;
}

- (void)canOpenApp:(CDVInvokedUrlCommand *)command {
    CDVPluginResult* result = nil;
    
    NSString* scheme = [command.arguments objectAtIndex:0];
    
    if ([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:scheme]]) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:(true)];
    } else {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsBool:(false)];
    }
    
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}


@end
