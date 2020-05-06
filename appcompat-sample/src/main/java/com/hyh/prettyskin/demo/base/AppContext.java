package com.hyh.prettyskin.demo.base;

import android.app.Application;

import com.hyh.prettyskin.AppCompatSkinHandlerMap;
import com.hyh.prettyskin.PrettySkin;
import com.hyh.prettyskin.R;
import com.hyh.prettyskin.ThemeSkin;
import com.hyh.prettyskin.demo.lifecycle.ActivityLifecycleHelper;
import com.hyh.prettyskin.demo.sh.CustomSwipeRefreshLayoutSH;
import com.hyh.prettyskin.demo.sh.ShapeViewSH;
import com.hyh.prettyskin.demo.utils.PreferenceUtil;
import com.hyh.prettyskin.demo.widget.CustomSwipeRefreshLayout;
import com.hyh.prettyskin.demo.widget.ShapeView;

/**
 * @author Administrator
 * @description
 * @data 2018/9/30
 */

public class AppContext extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityLifecycleHelper.getInstance().init(this);

        //初始化
        PrettySkin.getInstance().init(this);

        PrettySkin.getInstance().setParseDefaultAttrValueEnabled(true);

        //添加ShapeView自定义属性处理器
        PrettySkin.getInstance().addSkinHandler(ShapeView.class, new ShapeViewSH());

        //添加CustomSwipeRefreshLayout自定义属性处理器
        PrettySkin.getInstance().addSkinHandler(CustomSwipeRefreshLayout.class, new CustomSwipeRefreshLayoutSH());

        //添加appcompat包中所有View的自定义属性处理器
        PrettySkin.getInstance().addSkinHandler(new AppCompatSkinHandlerMap());

        ThemeSkin themeSkin;
        int skinStyle = PreferenceUtil.getInt(this, "skin_style", 0);
        switch (skinStyle) {
            case SkinStyle.BLACK: {
                themeSkin = new ThemeSkin(this, R.style.PrettySkin_black, R.styleable.class, "PrettySkin");
                break;
            }
            case SkinStyle.PURPLE:
            case SkinStyle.ORANGE:
            case SkinStyle.WHITE:
            default: {
                themeSkin = new ThemeSkin(this, R.style.PrettySkin_white, R.styleable.class, "PrettySkin");
                break;
            }
        }
        PrettySkin.getInstance().replaceSkinAsync(themeSkin, null);
    }

    private static final String TAG = "AppContext";

}