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

@implementation AppDelegate (ZgsoftDeepLinkPlugin)

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

@end
