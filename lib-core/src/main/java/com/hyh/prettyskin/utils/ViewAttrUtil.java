package com.hyh.prettyskin.utils;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.hyh.prettyskin.AttrValue;
import com.hyh.prettyskin.ColorStateListFactory;
import com.hyh.prettyskin.DrawableFactory;
import com.hyh.prettyskin.ValueType;
import com.hyh.prettyskin.drawable.ColorListDrawable;
import com.hyh.prettyskin.utils.reflect.Lazy;
import com.hyh.prettyskin.utils.reflect.Reflect;


public class ViewAttrUtil {

    private static final ImageView.ScaleType[] SCALE_TYPE_ARRAY = {
            ImageView.ScaleType.MATRIX,
            ImageView.ScaleType.FIT_XY,
            ImageView.ScaleType.FIT_START,
            ImageView.ScaleType.FIT_CENTER,
            ImageView.ScaleType.FIT_END,
            ImageView.ScaleType.CENTER,
            ImageView.ScaleType.CENTER_CROP,
            ImageView.ScaleType.CENTER_INSIDE
    };


    public static int getStyleAttr(String rClassPath, String styleName) {
        return Reflect.from(rClassPath + "$attr").filed(styleName, int.class).get(null);
    }

    public static int getAndroidStyleAttr(String styleName) {
        return getStyleAttr("android.R", styleName);
    }

    public static int getInternalStyleAttr(String styleName) {
        return getStyleAttr("com.android.internal.R", styleName);
    }

    public static boolean needsTileify(Drawable progressDrawable) {
        if (progressDrawable != null) {
            return Reflect.from(ProgressBar.class)
                    .method("needsTileify", boolean.class)
                    .param(Drawable.class, progressDrawable)
                    .invoke(null);
        }
        return false;
    }

    public static <T> T getTypedValue(AttrValue attrValue, Class<T> valueClass, T defaultValue) {
        if (attrValue == null || valueClass == null) return defaultValue;
        int type = attrValue.getType();
        Object value = attrValue.getValue();
        if (value == null) return defaultValue;
        switch (type) {
            case ValueType.TYPE_NULL: {
                return defaultValue;
            }
            case ValueType.TYPE_REFERENCE: {
                return getTypedReferenceValue(attrValue, valueClass, defaultValue);
            }
            case ValueType.TYPE_INT: {
                return getTypedIntValue(valueClass, defaultValue, value);
            }
            case ValueType.TYPE_FLOAT: {
                if (valueClass == float.class || valueClass == Float.class) {
                    return (T) value;
                } else if (valueClass == int.class || valueClass == Integer.class) {
                    if (value instanceof Float) {
                        Integer round = Math.round((float) value);
                        return (T) round;
                    }
                }
                return defaultValue;
            }
            case ValueType.TYPE_BOOLEAN: {
                if (valueClass == boolean.class || valueClass == Boolean.class) {
                    return (T) value;
                } else {
                    return defaultValue;
                }
            }
            case ValueType.TYPE_STRING: {
                if (valueClass == String.class) {
                    if (value instanceof CharSequence) {
                        return (T) value.toString();
                    }
                } else if (valueClass == CharSequence.class) {
                    if (value instanceof CharSequence) {
                        return (T) value;
                    }
                }
                return defaultValue;
            }
            case ValueType.TYPE_COLOR_INT: {
                if (valueClass == int.class) {
                    return (T) value;
                } else if (valueClass == ColorStateList.class) {
                    int color = (int) value;
                    return (T) ColorStateList.valueOf(color);
                } else if (valueClass == Drawable.class) {
                    int color = (int) value;
                    return (T) new ColorDrawable(color);
                } else {
                    return defaultValue;
                }
            }
            case ValueType.TYPE_COLOR_STATE_LIST: {
                if (valueClass == int.class) {
                    ColorStateList colorStateList = (ColorStateList) value;
                    if (colorStateList != null) {
                        Integer defaultColor = colorStateList.getDefaultColor();
                        return (T) defaultColor;
                    }
                } else if (valueClass == ColorStateList.class) {
                    return (T) value;
                } else if (valueClass == Drawable.class) {
                    ColorStateList colorStateList = (ColorStateList) value;
                    return (T) new ColorListDrawable(colorStateList);
                }
                return defaultValue;
            }
            case ValueType.TYPE_LAZY_COLOR_STATE_LIST: {
                if (value instanceof ColorStateListFactory) {
                    ColorStateListFactory colorStateListFactory = (ColorStateListFactory) value;
                    ColorStateList colorStateList = colorStateListFactory.create();
                    if (colorStateList == null) {
                        return null;
                    }
                    if (valueClass == ColorStateList.class) {
                        return (T) colorStateList;
                    } else if(valueClass == Drawable.class){
                        return (T) new ColorListDrawable(colorStateList);
                    }else {
                        return defaultValue;
                    }
                } else {
                    return defaultValue;
                }
            }
            case ValueType.TYPE_DRAWABLE: {
                if (valueClass == Drawable.class) {
                    return (T) value;
                } else {
                    return defaultValue;
                }
            }
            case ValueType.TYPE_LAZY_DRAWABLE: {
                if (value instanceof DrawableFactory) {
                    DrawableFactory drawableFactory = (DrawableFactory) value;
                    Drawable drawable = drawableFactory.create();
                    if (drawable == null) {
                        return null;
                    }
                    if (Reflect.isAssignableFrom(drawable.getClass(), valueClass)) {
                        return (T) drawable;
                    } else {
                        return defaultValue;
                    }
                } else {
                    return defaultValue;
                }
            }
            case ValueType.TYPE_OBJECT: {
                return getTypedObjectValue(attrValue, valueClass, defaultValue);
            }
        }
        return defaultValue;
    }

    private static <T> T getTypedReferenceValue(AttrValue attrValue, Class<T> valueClass, T defaultValue) {
        Context themeContext = attrValue.getThemeContext();
        Object value = attrValue.getValue();
        if (themeContext == null) return defaultValue;
        Resources resources = themeContext.getResources();
        Integer resourceId = (int) value;
        String resourceTypeName = resources.getResourceTypeName(resourceId);
        if (TextUtils.isEmpty(resourceTypeName)) return defaultValue;
        switch (resourceTypeName) {
            case "integer": {
                if (valueClass == int.class || valueClass == Integer.class) {
                    Integer integer = resources.getInteger(resourceId);
                    return (T) integer;
                } else if (valueClass == float.class || valueClass == Float.class) {
                    int integer = resources.getInteger(resourceId);
                    Float f = (float) integer;
                    return (T) f;
                }
                return defaultValue;
            }
            case "style": {
                if (valueClass == int.class || valueClass == Integer.class) {
                    return (T) value;
                }
                return defaultValue;
            }
            case "fraction": {
                Float fraction = null;
                try {
                    TypedValue typedValue = new TypedValue();
                    resources.getValue(resourceId, typedValue, true);
                    switch (typedValue.type) {
                        case TypedValue.TYPE_FLOAT: {
                            fraction = typedValue.getFloat();
                        }
                        case TypedValue.TYPE_FRACTION: {
                            fraction = typedValue.getFraction(1, 1);
                        }
                    }
                } catch (Exception e) {
                    SkinLogger.w("convert reference value to fraction failed, resourceId = " + resourceId + " ", e);
                }
                if (fraction == null) return defaultValue;

                if (valueClass == float.class || valueClass == Float.class) {
                    return (T) fraction;
                } else if (valueClass == int.class || valueClass == Integer.class) {
                    Integer round = Math.round((float) value);
                    return (T) round;
                }
                return defaultValue;
            }
            case "dimen": {
                if (valueClass == float.class || valueClass == Float.class) {
                    Float dimen = resources.getDimension(resourceId);
                    return (T) dimen;
                } else if (valueClass == int.class || valueClass == Integer.class) {
                    Integer dimen = resources.getDimensionPixelSize(resourceId);
                    return (T) dimen;
                }
                return defaultValue;
            }
            case "bool": {
                if (valueClass == boolean.class || valueClass == Boolean.class) {
                    Boolean bool = resources.getBoolean(resourceId);
                    return (T) bool;
                } else if (valueClass == int.class || valueClass == Integer.class) {
                    return (T) value;
                }
                return defaultValue;
            }
            case "string": {
                if (valueClass == String.class || valueClass == CharSequence.class) {
                    String string = resources.getString(resourceId);
                    return (T) string;
                } else if (valueClass == int.class || valueClass == Integer.class) {
                    return (T) value;
                }
                return defaultValue;
            }
            case "color": {
                if (valueClass == int.class || valueClass == Integer.class) {
                    Integer color = resources.getColor(resourceId);
                    return (T) color;
                } else if (valueClass == ColorStateList.class) {
                    return (T) resources.getColorStateList(resourceId);
                }
                return defaultValue;
            }
            case "drawable":
            case "mipmap": {
                if (valueClass == Drawable.class) {
                    Drawable drawable = resources.getDrawable(resourceId);
                    return (T) drawable;
                } else if (valueClass == int.class || valueClass == Integer.class) {
                    return (T) value;
                }
                return defaultValue;
            }
            case "anim": {
                if (valueClass == Animation.class) {
                    try {
                        return (T) AnimationUtils.loadAnimation(themeContext, resourceId);
                    } catch (Exception e) {
                        SkinLogger.w("value convert to animation failed ", e);
                    }
                } else if (valueClass == LayoutAnimationController.class) {
                    try {
                        return (T) AnimationUtils.loadLayoutAnimation(themeContext, resourceId);
                    } catch (Exception e) {
                        SkinLogger.w("value convert to layout animation failed ", e);
                    }
                } else if (valueClass == Interpolator.class) {
                    try {
                        return (T) AnimationUtils.loadInterpolator(themeContext, resourceId);
                    } catch (Exception e) {
                        SkinLogger.w("value convert to interpolator failed ", e);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && valueClass == StateListAnimator.class) {
                    try {
                        return (T) AnimatorInflater.loadStateListAnimator(themeContext, resourceId);
                    } catch (Exception e) {
                        SkinLogger.w("value convert to StateListAnimator failed ", e);
                    }
                } else if (valueClass == int.class || valueClass == Integer.class) {
                    return (T) value;
                }
                return defaultValue;
            }
            case "array": {
                if (valueClass == int[].class) {
                    try {
                        return (T) resources.getIntArray(resourceId);
                    } catch (Exception e) {
                        SkinLogger.w("value convert to int[] failed ", e);
                    }
                } else if (valueClass == String[].class) {
                    try {
                        return (T) resources.getStringArray(resourceId);
                    } catch (Exception e) {
                        SkinLogger.w("value convert to String[] failed ", e);
                    }
                } else if (valueClass == int.class || valueClass == Integer.class) {
                    return (T) value;
                }
                return defaultValue;
            }
            default: {
                return defaultValue;
            }
        }
    }

    private static <T> T getTypedIntValue(Class<T> valueClass, T defaultValue, Object value) {
        if (valueClass == int.class || valueClass == Integer.class) {
            return (T) value;
        } else if (valueClass == float.class || valueClass == Float.class) {
            if (value instanceof Integer) {
                int num = (int) value;
                Float f = (float) num;
                return (T) f;
            }
            return defaultValue;
        } else if (valueClass == PorterDuff.Mode.class) {
            int index = (int) value;
            return (T) getTintMode(index, (PorterDuff.Mode) defaultValue);
        } else if (valueClass == Typeface.class) {
            int index = (int) value;
            return (T) getTextTypeface(index, (Typeface) defaultValue);
        } else if (valueClass == ImageView.ScaleType.class) {
            int index = (int) value;
            return (T) getImageScaleType(index, (ImageView.ScaleType) defaultValue);
        } else if (valueClass == TextUtils.TruncateAt.class) {
            int index = (int) value;
            return (T) getTextEllipsize(index, (TextUtils.TruncateAt) defaultValue);
        } else {
            return defaultValue;
        }
    }

    private static <T> T getTypedObjectValue(AttrValue attrValue, Class<T> valueClass, T defaultValue) {
        Object value = attrValue.getValue();
        if (value == null || !valueClass.isAssignableFrom(value.getClass())) return defaultValue;
        return (T) value;
    }

    private static PorterDuff.Mode getTintMode(int index, PorterDuff.Mode defaultMode) {
        return Reflect.from(Drawable.class)
                .method("parseTintMode", PorterDuff.Mode.class)
                .param(int.class, index)
                .param(PorterDuff.Mode.class, null)
                .defaultValue(new Lazy<PorterDuff.Mode>() {
                    @Override
                    protected PorterDuff.Mode create() {
                        return parseTintMode(index, defaultMode);
                    }
                })
                .invoke(null);
    }


    private static PorterDuff.Mode parseTintMode(int value, PorterDuff.Mode defaultMode) {
        switch (value) {
            case 3:
                return PorterDuff.Mode.SRC_OVER;
            case 5:
                return PorterDuff.Mode.SRC_IN;
            case 9:
                return PorterDuff.Mode.SRC_ATOP;
            case 14:
                return PorterDuff.Mode.MULTIPLY;
            case 15:
                return PorterDuff.Mode.SCREEN;
            case 16:
                return PorterDuff.Mode.ADD;
            default:
                return defaultMode;
        }
    }

    private static ImageView.ScaleType getImageScaleType(int index, ImageView.ScaleType defaultScaleType) {
        if (index < 0 || index >= SCALE_TYPE_ARRAY.length) {
            return defaultScaleType;
        }
        return SCALE_TYPE_ARRAY[index];
    }

    private static Typeface getTextTypeface(int index, Typeface defaultTypeface) {
        Typeface tf = defaultTypeface;
        switch (index) {
            case 1:
                tf = Typeface.SANS_SERIF;
                break;
            case 2:
                tf = Typeface.SERIF;
                break;
            case 3:
                tf = Typeface.MONOSPACE;
                break;
        }
        return tf;
    }

    private static TextUtils.TruncateAt getTextEllipsize(int index, TextUtils.TruncateAt defaultEllipsize) {
        TextUtils.TruncateAt where = defaultEllipsize;
        switch (index) {
            case 1:
                where = TextUtils.TruncateAt.START;
                break;
            case 2:
                where = TextUtils.TruncateAt.MIDDLE;
                break;
            case 3:
                where = TextUtils.TruncateAt.END;
                break;
            case 4:
                where = TextUtils.TruncateAt.MARQUEE;
                break;
        }
        return where;
    }

    public static int getDescendantFocusability(int index) {
        int descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS;
        switch (index) {
            case 0: {
                descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS;
                break;
            }
            case 1: {
                descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS;
                break;
            }
            case 2: {
                descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS;
                break;
            }
        }
        return descendantFocusability;
    }
}