package com.hyh.prettyskin.sh.support;

import android.annotation.SuppressLint;

import com.hyh.prettyskin.sh.SeekBarSH;


@SuppressLint("RestrictedApi")
public class AppCompatSeekBarSH extends SeekBarSH {

    public AppCompatSeekBarSH() {
        super(android.support.v7.appcompat.R.attr.seekBarStyle);
    }

    public AppCompatSeekBarSH(int defStyleAttr) {
        super(defStyleAttr);
    }

    public AppCompatSeekBarSH(int defStyleAttr, int defStyleRes) {
        super(defStyleAttr, defStyleRes);
    }
}
