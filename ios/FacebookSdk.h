#ifndef FacebookSdk_h
#define FacebookSdk_h

#import <Foundation/Foundation.h>
#import "RootViewController.h"

@interface FacebookSdk : NSObject

+ (instancetype)getInstance;

/**
 初始化
 */
- (void)initSdk:(RootViewController *)viewController;

/**
 登录
 */
- (void)login;

/**
 登出
 */
- (void)logout;

/**
 分享
 @param shareInfo 分享信息
 */
- (void)share:(NSString *)shareInfo;

@end

#endif /* FacebookSdk_h */
