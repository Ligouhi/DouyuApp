package com.xzh.basepopup;

import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.xzh.basepopup.blur.BlurImageView;
import com.xzh.basepopup.util.log.LogTag;
import com.xzh.basepopup.util.log.LogUtil;

import java.lang.ref.WeakReference;



final class HackWindowManager implements WindowManager {
    private static final String TAG = "HackWindowManager";
    private WeakReference<WindowManager> mWindowManager;
    private WeakReference<PopupController> mPopupController;
    private WeakReference<HackPopupDecorView> mHackPopupDecorView;
    private WeakReference<BasePopupHelper> mPopupHelper;
    private WeakReference<BlurImageView> mBlurImageView;

    public HackWindowManager(WindowManager windowManager, PopupController popupController) {
        mWindowManager = new WeakReference<WindowManager>(windowManager);
        mPopupController = new WeakReference<PopupController>(popupController);
    }

    @Override
    public Display getDefaultDisplay() {
        return getWindowManager() == null ? null : getWindowManager().getDefaultDisplay();
    }

    @Override
    public void removeViewImmediate(View view) {
        if (getWindowManager() == null) return;
        LogUtil.trace(LogTag.i, TAG, "WindowManager.removeViewImmediate  >>>  " + view.getClass().getSimpleName());
        if (checkProxyValided(view) && getHackPopupDecorView() != null) {
            if (getBlurImageView() != null) {
                try {
                    getWindowManager().removeViewImmediate(getBlurImageView());
                    mBlurImageView.clear();
                    mBlurImageView = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            HackPopupDecorView hackPopupDecorView = getHackPopupDecorView();
            getWindowManager().removeViewImmediate(hackPopupDecorView);
            hackPopupDecorView.setPopupController(null);
            mHackPopupDecorView.clear();
            mHackPopupDecorView = null;
        } else {
            getWindowManager().removeViewImmediate(view);
        }
    }

    @Override
    public void addView(View view, ViewGroup.LayoutParams params) {
        if (getWindowManager() == null) return;
        LogUtil.trace(LogTag.i, TAG, "WindowManager.addView  >>>  " + view.getClass().getSimpleName());
        if (checkProxyValided(view)) {
            BasePopupHelper helper = getBasePopupHelper();
            BlurImageView blurImageView = null;
            //添加背景模糊层
            if (helper != null && helper.isAllowToBlur()) {
                blurImageView = new BlurImageView(view.getContext());
                blurImageView.attachBlurOption(helper.getBlurOption());
                mBlurImageView = new WeakReference<BlurImageView>(blurImageView);
                getWindowManager().addView(blurImageView, generateBlurBackgroundWindowParams(params));
            }

            //添加popup主体
            params = applyHelper(params);
            final HackPopupDecorView hackPopupDecorView = new HackPopupDecorView(view.getContext());
            mHackPopupDecorView = new WeakReference<HackPopupDecorView>(hackPopupDecorView);
            hackPopupDecorView.setPopupController(getPopupController());
            hackPopupDecorView.addBlurImageview(blurImageView);

            ViewGroup.LayoutParams decorViewLayoutParams = null;
            if (getPopupController() instanceof BasePopupWindow) {
                View contentView = ((BasePopupWindow) getPopupController()).getContentView();
                if (contentView != null) {
                    decorViewLayoutParams = contentView.getLayoutParams();
                }
            }
            if (decorViewLayoutParams != null) {
                // FIXME: 2018/1/4 这里将popup高度处理为跟contentView高度一致，但尚未清除会否有风险
                params.height = decorViewLayoutParams.height;
                hackPopupDecorView.addView(view, decorViewLayoutParams);
            } else {
                hackPopupDecorView.addView(view);
            }

            getWindowManager().addView(hackPopupDecorView, params);
        } else {
            getWindowManager().addView(view, params);
        }
    }

    @Override
    public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
        if (getWindowManager() == null) return;
        LogUtil.trace(LogTag.i, TAG, "WindowManager.updateViewLayout  >>>  " + view.getClass().getSimpleName());
        if (checkProxyValided(view) && getHackPopupDecorView() != null) {
            HackPopupDecorView hackPopupDecorView = getHackPopupDecorView();
            getWindowManager().updateViewLayout(hackPopupDecorView, params);
        } else {
            getWindowManager().updateViewLayout(view, params);
        }
    }

    @Override
    public void removeView(View view) {
        if (getWindowManager() == null) return;
        LogUtil.trace(LogTag.i, TAG, "WindowManager.removeView  >>>  " + view.getClass().getSimpleName());
        if (checkProxyValided(view) && getHackPopupDecorView() != null) {
            if (getBlurImageView() != null) {
                try {
                    getWindowManager().removeView(getBlurImageView());
                    mBlurImageView.clear();
                    mBlurImageView = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            HackPopupDecorView hackPopupDecorView = getHackPopupDecorView();
            getWindowManager().removeView(hackPopupDecorView);
            hackPopupDecorView.setPopupController(null);
            mHackPopupDecorView.clear();
            mHackPopupDecorView = null;
        } else {
            getWindowManager().removeView(view);
        }
    }


    /**
     * 生成blurimageview的params
     *
     * @param params
     * @return
     */
    private ViewGroup.LayoutParams generateBlurBackgroundWindowParams(ViewGroup.LayoutParams params) {
        ViewGroup.LayoutParams result = new ViewGroup.LayoutParams(params);

        if (params instanceof LayoutParams) {
            LayoutParams mResults = new LayoutParams();
            mResults.copyFrom((LayoutParams) params);

            mResults.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
            mResults.flags |= LayoutParams.FLAG_NOT_TOUCHABLE;
            mResults.flags |= LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            mResults.x = 0;
            mResults.y = 0;
            mResults.format = PixelFormat.RGBA_8888;

            result = mResults;
        }
        result.width = ViewGroup.LayoutParams.MATCH_PARENT;
        result.height = ViewGroup.LayoutParams.MATCH_PARENT;
        return result;
    }

    /**
     * 应用helper的设置到popup的params
     *
     * @param params
     * @return
     */
    private ViewGroup.LayoutParams applyHelper(ViewGroup.LayoutParams params) {
        if (!(params instanceof LayoutParams) || getBasePopupHelper() == null)
            return params;
        LayoutParams p = (LayoutParams) params;
        BasePopupHelper helper = getBasePopupHelper();
        if (helper == null) return params;
        if (!helper.isInterceptTouchEvent()) {
            LogUtil.trace(LogTag.i, TAG, "applyHelper  >>>  不拦截事件");
            p.flags |= LayoutParams.FLAG_NOT_TOUCH_MODAL;
            p.flags |= LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        }
        if (helper.isFullScreen()) {
            LogUtil.trace(LogTag.i, TAG, "applyHelper  >>>  全屏");
            p.flags |= LayoutParams.FLAG_LAYOUT_IN_SCREEN;

            // FIXME: 2017/12/27 全屏跟SOFT_INPUT_ADJUST_RESIZE冲突，暂时没有好的解决方案
            p.softInputMode = LayoutParams.SOFT_INPUT_STATE_UNCHANGED;
        }
        LogUtil.trace(LogTag.i, TAG, "PopupWindow窗口的window type >>  " + p.type);
        return p;
    }


    private boolean checkProxyValided(View v) {
        if (v == null) return false;
        String viewSimpleClassName = v.getClass().getSimpleName();
        return TextUtils.equals(viewSimpleClassName, "PopupDecorView") || TextUtils.equals(viewSimpleClassName, "PopupViewContainer");
    }

    private HackPopupDecorView getHackPopupDecorView() {
        if (mHackPopupDecorView == null) return null;
        return mHackPopupDecorView.get();
    }

    private WindowManager getWindowManager() {
        if (mWindowManager == null) return null;
        return mWindowManager.get();
    }

    private PopupController getPopupController() {
        if (mPopupController == null) return null;
        return mPopupController.get();
    }

    private BasePopupHelper getBasePopupHelper() {
        if (mPopupHelper == null) return null;
        return mPopupHelper.get();
    }

    private BlurImageView getBlurImageView() {
        if (mBlurImageView == null) return null;
        return mBlurImageView.get();
    }

    void bindPopupHelper(BasePopupHelper helper) {
        mPopupHelper = new WeakReference<BasePopupHelper>(helper);
    }
}
