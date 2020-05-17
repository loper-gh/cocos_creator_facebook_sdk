package com.example.myapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import org.cocos2dx.lib.Cocos2dxHelper;
import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;

//AndroidManifest.xml
//<!--      Start  FaceBook   -->
//<meta-data
//        android:name="com.facebook.sdk.ApplicationId"
//        android:value="@string/facebook_app_id" />
//
//<activity
//            android:name="com.facebook.FacebookActivity"
//                    android:configChanges="keyboard|keyboardHidden|screenLayout|screenSize|orientation"
//                    android:label="@string/app_name" />
//<activity
//            android:name="com.facebook.CustomTabActivity"
//                    android:exported="true">
//<intent-filter>
//<action android:name="android.intent.action.VIEW" />
//
//<category android:name="android.intent.category.DEFAULT" />
//<category android:name="android.intent.category.BROWSABLE" />
//
//<data android:scheme="@string/fb_login_protocol_scheme" />
//</intent-filter>
//</activity>
//
//<!--facebook 图片分享需要-->
//<provider
//            android:name="com.facebook.FacebookContentProvider"
//                    android:authorities="com.facebook.app.FacebookContentProvider{你的应用id}"
//                    android:exported="true" />
//<!--      End  FaceBook   -->

public class FacebookSdk {
    private static String TAG = "FacebookSdk";
    private static FacebookSdk mInstance = null;
    private Activity mActivity = null;
    private CallbackManager mCallbackManager = null;
    private ShareDialog mShareDialog = null;

    public static FacebookSdk getInstance() {
        if (null == mInstance) {
            mInstance = new FacebookSdk();
        }
        return mInstance;
    }

    /**
     *  监听 onActivityResult
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult data = " + data.toString());

        if (mCallbackManager != null) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     *  初始化
     */
    public void initSdk(Activity activity) {
        Log.i(TAG, "initSdk");

        mActivity = activity;
        mCallbackManager = CallbackManager.Factory.create();

        initLogin();
        initShare();
    }

    /**
     *  初始化登录
     */
    private void initLogin() {
        Log.i(TAG, "initLogin");

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e(TAG, "login onSuccess loginResult = " + loginResult.toString());

                //获取授权信息
                requestAuthInfo();
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "login onCancel");

                //通知js
                final String eval = String.format("cc.onFacebookLoginCancel()");
                Cocos2dxHelper.runOnGLThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Cocos2dxJavascriptJavaBridge.evalString(eval);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "login onError error = " + error.toString());

                //通知js
                final String eval = String.format("cc.onFacebookLoginError('%s')", error.toString());
                Cocos2dxHelper.runOnGLThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Cocos2dxJavascriptJavaBridge.evalString(eval);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    /**
     *  登录
     */
    public void login() {
        Log.i(TAG, "login");

        if (isAuthorization()) {
            //已经授权
            requestAuthInfo();
            return;
        }

        Collection<String> permissions = Arrays.asList("public_profile", "email");
        LoginManager.getInstance().logInWithReadPermissions(mActivity, permissions);
    }

    /**
     *  是否授权
     */
    private boolean isAuthorization(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null) {
            return false;
        }

        return !accessToken.isExpired();
    }

    /**
     *  获取授权信息
     */
    private void requestAuthInfo() {
        Log.i(TAG, "requestAuthInfo");

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GraphRequest graphRequest = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.e(TAG, "requestAuthInfo onCompleted object = " + object.toString() + " response = " + response.toString());

                if (response.getError() != null) {
                    //授权错误
                    //通知js
                    final String eval = String.format("cc.onFacebookLoginError('%s')", response.getError().getErrorMessage());
                    Cocos2dxHelper.runOnGLThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Cocos2dxJavascriptJavaBridge.evalString(eval);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    return;
                }

                //id:1565455221565
                String id = object.optString("id");
                //昵称：Zhang San
                String name = object.optString("name");
                //性别：比如 male （男）  female （女）
                String gender = object.optString("gender");
                //邮箱：比如：56236545@qq.com
                String email = object.optString("email");
                //头像
                String picture = "";
                JSONObject objPicture = object.optJSONObject("picture");
                if (objPicture != null) {
                    JSONObject objPictureData = objPicture.optJSONObject("data");
                    if (objPictureData != null) {
                        picture = objPictureData.optString("url");
                    }
                }
                //头像
                String avatar = String.format("https://graph.facebook.com/%s/picture?type=large", id);
                String authInfo = String.format("{\"id\":\"%s\",\"name\":\"%s\",\"gender\":\"%s\",\"email\":\"%s\",\"picture\":\"%s\",\"avatar\":\"%s\"}",
                        id, name, gender, email, picture, avatar);

                //通知js
                final String eval = String.format("cc.onFacebookLoginSuccess('%s')", authInfo);
                Cocos2dxHelper.runOnGLThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Cocos2dxJavascriptJavaBridge.evalString(eval);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        Bundle param = new Bundle();
        param.putString("fields", "id,name,gender,email,picture");
        graphRequest.setParameters(param);
        graphRequest.executeAsync();
    }

    /**
     *  登出
     */
    public void logout() {
        Log.i(TAG, "logout");

        LoginManager.getInstance().logOut();
    }

    /**
     *  初始化分享
     */
    private void initShare() {
        Log.i(TAG, "initShare");

        mShareDialog = new ShareDialog(mActivity);
        mShareDialog.registerCallback(mCallbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.e(TAG, "share onSuccess result = " + result.toString());

                //通知js
                final String eval = String.format("cc.onFacebookShareSuccess('%s')", result.toString());
                Cocos2dxHelper.runOnGLThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Cocos2dxJavascriptJavaBridge.evalString(eval);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "share onCancel");

                //通知js
                final String eval = String.format("cc.onFacebookShareCancel()");
                Cocos2dxHelper.runOnGLThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Cocos2dxJavascriptJavaBridge.evalString(eval);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "share onError error = " + error.toString());

                //通知js
                final String eval = String.format("cc.onFacebookShareError('%s')", error.toString());
                Cocos2dxHelper.runOnGLThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Cocos2dxJavascriptJavaBridge.evalString(eval);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    /**
     *  分享
     */
    public void share(String shareInfo) {
        Log.i(TAG, "share shareInfo = " + shareInfo);

        try {
            JSONObject json = new JSONObject(shareInfo);
            int shareType = json.getInt("shareType");
            String shareUrl = json.getString("shareUrl");
            String imgPath = json.getString("imgPath");

            if (shareType == 0) {
                //链接
                if (ShareDialog.canShow(ShareLinkContent.class)) {
                    ShareLinkContent content = new ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse(shareUrl))
                            .build();
                    mShareDialog.show(content);
                }
            } else if (shareType == 1) {
                //图片
                if (ShareDialog.canShow(SharePhotoContent.class)) {
                    Bitmap img = BitmapFactory.decodeFile(imgPath);
                    SharePhoto photo = new SharePhoto.Builder()
                            .setBitmap(img)
                            .build();
                    SharePhotoContent content = new SharePhotoContent.Builder()
                            .addPhoto(photo)
                            .build();
                    mShareDialog.show(content);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
