package com.hyh.prettyskin.sh.androidx;

import android.util.AttributeSet;
import android.view.View;

import com.hyh.prettyskin.AttrValue;
import com.hyh.prettyskin.sh.EditTextSH;
import com.hyh.prettyskin.utils.ViewAttrUtil;

import androidx.appcompat.widget.AppCompatBackgroundSH;
import androidx.appcompat.widget.AppCompatTextSH;


public class AppCompatEditTextSH extends EditTextSH {

    private final AppCompatBackgroundSH mBackgroundSH;
    private final AppCompatTextSH mTextSH;

    public AppCompatEditTextSH() {
        this(ViewAttrUtil.getStyleAttr("androidx.appcompat.R", "editTextStyle"));
    }

    public AppCompatEditTextSH(int defStyleAttr) {
        this(defStyleAttr, 0);
    }

    public AppCompatEditTextSH(int defStyleAttr, int defStyleRes) {
        super(defStyleAttr, defStyleRes);
        mBackgroundSH = new AppCompatBackgroundSH(defStyleAttr);
        mTextSH = new AppCompatTextSH(defStyleAttr);
    }

    @Override
    public boolean isSupportAttrName(View view, String attrName) {
        return mBackgroundSH.isSupportAttrName(view, attrName)
                || mTextSH.isSupportAttrName(view, attrName)
                || super.isSupportAttrName(view, attrName);
    }

    @Override
    public AttrValue parse(View view, AttributeSet set, String attrName) {
        if (mBackgroundSH.isSupportAttrName(view, attrName)) {
            return mBackgroundSH.parse(view, set, attrName);
        } else if (mTextSH.isSupportAttrName(view, attrName)) {
            return mTextSH.parse(view, set, attrName);
        } else {
            return super.parse(view, set, attrName);
        }
    }

    @Override
    public void replace(View view, String attrName, AttrValue attrValue) {
        if (mBackgroundSH.isSupportAttrName(view, attrName)) {
            mBackgroundSH.replace(view, attrName, attrValue);
        } else if (mTextSH.isSupportAttrName(view, attrName)) {
            mTextSH.replace(view, attrName, attrValue);
        } else {
            super.replace(view, attrName, attrValue);
        }
    }
}
