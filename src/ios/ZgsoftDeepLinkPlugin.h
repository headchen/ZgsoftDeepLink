//
//  DeepLinkPlugin.h
//  YGYJS
//
//  Created by zgsoft on 16/9/13.
//
//

#import <cordova/CDVPlugin.h>

@interface ZgsoftDeepLinkPlugin : CDVPlugin

/**
 *  Subscribe to event.
 *
 *  @param command command from js side with event name and callback id.
 */
- (void)jsSubscribe:(CDVInvokedUrlCommand *)command;

/**
 *  Unsubscribe from event.
 *
 *  @param command command from js side with event name
 */
- (void)jsUnsubscribe:(CDVInvokedUrlCommand *)command;

/**
 *  Try to hanlde application launch when user clicked on the link.
 *
 *  @param userActivity object with information about the application launch
 *
 *  @return <code>true</code> - if this is a universal link and it is defined in config.xml; otherwise - <code>false</code>
 */
- (BOOL)handleUserActivity:(NSUserActivity *)userActivity;


/**
 *  Test If can OpenApp , 注意-infoList白名单：IOS9.
 *
 *  @return <code>true</code> - if this is a universal link and it is defined in config.xml; otherwise - <code>false</code>
 */

- (void)canOpenApp:(CDVInvokedUrlCommand *)command;

@end
