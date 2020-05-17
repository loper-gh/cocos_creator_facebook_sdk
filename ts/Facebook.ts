export default class Facebook {

    /**
     * 分享类型
     */
    public static SHARE_TYPE = {
        /**
         * 链接
         */
        LINK: 0,
        /**
         * 图片
         */
        PHOTO: 1
    }

    /**
     * 登录
     */
    public static login() {
        console.log("Facebook login");

        if (cc.sys.isNative) {
            if (cc.sys.os == cc.sys.OS_ANDROID) {
                let className = "com/util/BridgeUtil";
                let methodName = "facebookLogin";
                let methodSignature = "()V";
                jsb.reflection.callStaticMethod(className, methodName, methodSignature);
            } else if (cc.sys.os == cc.sys.OS_IOS) {
                let className = "BridgeUtil";
                let methodName = "facebookLogin";
                jsb.reflection.callStaticMethod(className, methodName);
            }
        } else {
            let authInfo = '{"id":"1111111111111111","name":"Test","gender":"","email":"123456@qq.com","picture":"https://platform-lookaside.fbsbx.com/platform/profilepic/?asid=xxx&height=50&width=50&ext=xxx","avatar":"https://graph.facebook.com/xxx/picture?type=large"}';
            cc.onFacebookLoginSuccess(authInfo);
        }
    }

    /**
     * 登出
     */
    public static logout() {
        console.log("Facebook logout");

        if (cc.sys.isNative) {
            if (cc.sys.os == cc.sys.OS_ANDROID) {
                let className = "com/util/BridgeUtil";
                let methodName = "facebookLogout";
                let methodSignature = "()V";
                jsb.reflection.callStaticMethod(className, methodName, methodSignature);
            } else if (cc.sys.os == cc.sys.OS_IOS) {
                let className = "BridgeUtil";
                let methodName = "facebookLogout";
                jsb.reflection.callStaticMethod(className, methodName);
            }
        }
    }

    /**
     * 分享
     * @param shareType 分享类型 SHARE_TYPE
     * @param shareUrl 分享链接
     * @param imgPath 图片路径
     * @example
     * Facebook.share(Facebook.SHARE_TYPE.LINK, 'https://www.cocos.com/', '');
     */
    public static share(shareType: number, shareUrl: string, imgPath: string) {
        console.log("Facebook share shareType = " + shareType + ", shareUrl = " + ", imgPath = " + imgPath);
        ß
        let data = {
            shareType: shareType,
            shareUrl: shareUrl,
            imgPath: imgPath
        };
        let shareInfo = JSON.stringify(data);
        console.log('Facebook share shareInfo = ' + shareInfo);

        if (cc.sys.isNative) {
            if (cc.sys.os == cc.sys.OS_ANDROID) {
                let className = "com/util/BridgeUtil";
                let methodName = "facebookShare";
                let methodSignature = "(Ljava/lang/String;)V";
                jsb.reflection.callStaticMethod(className, methodName, methodSignature, shareInfo);
            } else if (cc.sys.os == cc.sys.OS_IOS) {
                let className = "BridgeUtil";
                let methodName = "facebookShare:";
                jsb.reflection.callStaticMethod(className, methodName, shareInfo);
            }
        } else {
            cc.onFacebookShareSuccess('test');
        }
    }
}

/**
 * 登录成功
 */
cc.onFacebookLoginSuccess = function (authInfo) {
    console.log("onFacebookLoginSuccess authInfo = " + authInfo);
    //处理登录逻辑
};

/**
 * 登录取消
 */
cc.onFacebookLoginCancel = function () {
    console.log("onFacebookLoginCancel");
};

/**
 * 登录错误
 */
cc.onFacebookLoginError = function (error) {
    console.log("onFacebookLoginError error = " + error);
};

/**
 * 分享成功
 */
cc.onFacebookShareSuccess = function (result) {
    console.log("onFacebookShareSuccess result = " + result);
    //处理分享逻辑
};

/**
 * 分享取消
 */
cc.onFacebookShareCancel = function () {
    console.log("onFacebookShareCancel");
};

/**
 * 分享错误
 */
cc.onFacebookShareError = function (error) {
    console.log("onFacebookShareError error = " + error);
};
