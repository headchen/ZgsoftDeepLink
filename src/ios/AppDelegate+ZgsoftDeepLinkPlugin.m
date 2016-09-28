//
//  AppDelegate+ZgsoftDeepLinkPlugin.m
//  YGYJS
//
//  Created by zgsoft on 16/9/14.
//
//

#import "AppDelegate+ZgsoftDeepLinkPlugin.h"

#import "ZgsoftDeepLinkPlugin.h"

/**
 *  Plugin name in config.xml
 */
static NSString *const PLUGIN_NAME = @"ZgsoftDeepLinkPlugin";
static NSString *const DEEPLINK_COOKIE_URL_KEY= @"ZgsoftDeferredDeepLinkUrl";


@implementation AppDelegate (ZgsoftDeepLinkPlugin)

 UIWindow * _secondWindow;



- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity
 restorationHandler:(void (^)(NSArray * _Nullable))restorationHandler

{
    // ignore activities that are not for Universal Links
    if (![userActivity.activityType isEqualToString:NSUserActivityTypeBrowsingWeb] || userActivity.webpageURL == nil) {
        return NO;
    }
    
    // get instance of the plugin and let it handle the userActivity object
    ZgsoftDeepLinkPlugin *plugin = [self.viewController getCommandInstance:PLUGIN_NAME];
    if (plugin == nil) {
        return NO;
    }
    
    return [plugin handleUserActivity:userActivity];
}

//can call many times
+(BOOL)isFirstRun
{
      static BOOL flag=NO;
      static BOOL result;
        if(!flag){
            if ([[NSUserDefaults standardUserDefaults] boolForKey:@"hasLaunchedOnce"])
            {
                result=NO;
            } else
            {
                [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"hasLaunchedOnce"];
                [[NSUserDefaults standardUserDefaults] synchronize];
                result=YES;
            }
            
            flag=YES;
        }
        return result;
    }

- (NSString *) getOSVersion
{
    UIDevice *device = [UIDevice currentDevice];
    return [device systemVersion];

}

- (NSString *) getDeepLinkCookieUrl
{
    NSDictionary *infoPlistDict = [[NSBundle mainBundle] infoDictionary];
    return [infoPlistDict valueForKey:DEEPLINK_COOKIE_URL_KEY];

 }

- (void)restoreDeepLink
{
    if ([AppDelegate isFirstRun] && [self getOSVersion].integerValue >=9)
    {
        [self presentSafariVCWithURL:[self getDeepLinkCookieUrl]];
    }
    
}


- (void)presentSafariVCWithURL:(NSString *)url {
    if(!url)
        return;
    
    NSURL *cookieURL = [NSURL URLWithString:url];
    if (!cookieURL) {
        return;
    }
    
    Class SFSafariViewControllerClass = NSClassFromString(@"SFSafariViewController");
    Class UIApplicationClass = NSClassFromString(@"UIApplication");
    if (SFSafariViewControllerClass) {
        
        // Must be on next run loop to avoid a warning
        dispatch_async(dispatch_get_main_queue(), ^{
            UIViewController * safController = [[SFSafariViewControllerClass alloc] initWithURL:cookieURL];
            _secondWindow = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
            _secondWindow.rootViewController = safController;
            _secondWindow.windowLevel = UIWindowLevelNormal - 100;
            [_secondWindow setHidden:NO];
            UIWindow *keyWindow = [[UIApplicationClass sharedApplication] keyWindow];
            [_secondWindow makeKeyWindow];
            
            // Give enough time for Safari to load the request (optimized for 3G)
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                [keyWindow makeKeyWindow];
                
                // Remove the window and release it's strong reference. This is important to ensure that
                // applications using view controller based status bar appearance are restored.
                [_secondWindow removeFromSuperview];
                _secondWindow = nil;
                

            });
        });
    }
 }


@end
