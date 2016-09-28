//
//  AppDelegate+ZgsoftDeepLinkPlugin.h
//  YGYJS
//
//  Created by zgsoft on 16/9/14.
//
//

#import "AppDelegate.h"

/**
 *  Category for the AppDelegate that overrides application:continueUserActivity:restorationHandler method,
 *  so we could handle application launch when user clicks on the link in the browser.
 */

@interface AppDelegate (ZgsoftDeepLinkPlugin)

- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void (^)(NSArray * _Nullable))restorationHandler;

- (void)restoreDeepLink;


@end
