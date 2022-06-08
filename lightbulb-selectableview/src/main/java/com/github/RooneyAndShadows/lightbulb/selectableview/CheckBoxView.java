package com.github.rooneyandshadows.lightbulb.selectableview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.rooneyandshadows.lightbulb.commons.utils.DrawableUtils;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils;
import com.google.android.material.color.MaterialColors;

import java.util.Arrays;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

@SuppressWarnings("unused")
public class CheckBoxView extends LinearLayoutCompat {
    private Drawable iconDrawable;
    private Drawable iconBackgroundDrawable;
    private boolean checked;
    private int textSize;
    private int startIconSize;
    private int startIconSpacing;
    private int endButtonSpacing;
    private String text = "";
    private AppCompatImageView iconView;
    private MaterialCheckBox checkBox;
    private AppCompatTextView textView;
    private TextPosition textPosition;
    private ViewCheckedChangeListener onGroupCheckedListener;
    private ViewCheckedChangeListener onCheckedChangeListener;
    private ViewCheckedChangeListener dataBindingCheckChangeListener;
    private int[] iconPadding;
    private final int CHECKBOX_MARGIN_RIGHT = ResourceUtils.dpToPx(7);

    public CheckBoxView(Context context) {
        this(context, null);
    }

    public CheckBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(true);
        readAttributes(context, attrs);
        initViews(context);
    }

    void setOnGroupCheckedListener(ViewCheckedChangeListener listener) {
        onGroupCheckedListener = listener;
    }

    public void setOnCheckedListener(ViewCheckedChangeListener listener) {
        onCheckedChangeListener = listener;
    }

    public void setChecked(boolean newValue) {
        setCheckedStateInternally(newValue);
    }

    public void setText(String text) {
        setViewText(text);
    }

    public void setIcon(Drawable icon, Drawable iconBackground) {
        iconDrawable = icon;
        iconBackgroundDrawable = iconBackground;
        setupIconView();
    }

    public void setIcon(Drawable icon) {
        setIcon(icon, null);
    }

    public void setIconPadding(int left, int top, int right, int bottom) {
        iconPadding = new int[]{left, top, right, bottom};
        iconView.setPadding(left, top, right, bottom);
    }

    public boolean isChecked() {
        return checked;
    }

    public String getText() {
        return text;
    }

    public AppCompatImageView getIconView() {
        return iconView;
    }

    @BindingAdapter("CBV_Title")
    public static void setText(CheckBoxView view, String title) {
        view.setViewText(title);
    }

    @BindingAdapter("CBV_Checked")
    public static void setSelectableChecked(CheckBoxView view, Boolean checked) {
        if (view.isChecked() != checked)
            view.setCheckedStateInternally(checked);
    }

    @InverseBindingAdapter(attribute = "CBV_Checked", event = "CBV_CheckedAttributeChanged")
    public static Boolean getSelectableChecked(CheckBoxView view) {
        return view.checked;
    }

    @BindingAdapter("CBV_CheckedAttributeChanged")
    public static void setListeners(CheckBoxView view, final InverseBindingListener attrChange) {
        view.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (view.onCheckedChangeListener != null)
                view.onCheckedChangeListener.execute(view, isChecked);
            attrChange.onChange();
        });
    }

    private void initViews(Context ctx) {
        setClickable(true);
        setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP)
                checkBox.performClick();
            return false;
        });
        iconView = new AppCompatImageView(getContext());
        textView = new AppCompatTextView(getContext());
        checkBox = new MaterialCheckBox(getContext());
        setupViews();
    }

    private void setupViews() {
        removeAllViews();
        LinearLayoutCompat.LayoutParams textParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayoutCompat.LayoutParams iconParams = new LayoutParams(startIconSize, startIconSize);
        LinearLayoutCompat.LayoutParams checkboxParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        iconParams.gravity = Gravity.CENTER_VERTICAL;
        checkboxParams.gravity = Gravity.CENTER_VERTICAL;
        textParams.gravity = Gravity.CENTER_VERTICAL;
        textParams.weight = 1;
        checkBox.setMinimumWidth(0);
        checkBox.setMinWidth(0);
        switch (textPosition) {
            case START:
                addView(checkBox, checkboxParams);
                addView(iconView, iconParams);
                addView(textView, textParams);
                break;
            case END:
                addView(iconView, iconParams);
                addView(textView, textParams);
                addView(checkBox, checkboxParams);
                break;
        }
        setupIconView();
        setupTextView();
        setupCheckbox();
    }

    private void setupIconView() {
        LayoutParams textLayoutParams = (LayoutParams) textView.getLayoutParams();
        if (iconDrawable == null) {
            textLayoutParams.setMarginStart(0);
            iconView.setVisibility(GONE);
            return;
        }
        iconView.setVisibility(VISIBLE);
        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iconView.setImageDrawable(iconDrawable);
        iconView.setPadding(iconPadding[0], iconPadding[1], iconPadding[2], iconPadding[3]);
        iconView.setBackground(iconBackgroundDrawable);
        LayoutParams params = (LayoutParams) iconView.getLayoutParams();
    }

    private void setupTextView() {
        textView.setTextAppearance(R.style.Base_TextAppearance_AppCompat);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        textView.setClickable(false);
        MaterialColors.getColor(this,android.R.attr.textColorSecondary);
        textView.setIncludeFontPadding(false);
        LayoutParams params = (LayoutParams) textView.getLayoutParams();
        if (textPosition.equals(TextPosition.START)) {
            if (iconDrawable == null) params.setMarginStart(endButtonSpacing);
        } else params.setMarginEnd(endButtonSpacing);
        textView.setLayoutParams(params);
    }

    private void setupCheckbox() {
        checkBox.setChecked(checked);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> setChecked(isChecked));
        LayoutParams params = (LayoutParams) checkBox.getLayoutParams();
        if (textPosition.equals(TextPosition.END))
            params.setMarginEnd(CHECKBOX_MARGIN_RIGHT);
        checkBox.setLayoutParams(params);
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CheckBoxView, 0, 0);
        try {
            iconDrawable = a.getDrawable(R.styleable.CheckBoxView_CBV_Icon);
            iconBackgroundDrawable = a.getDrawable(R.styleable.CheckBoxView_CBV_IconBackground);
            text = a.getString(R.styleable.CheckBoxView_CBV_Title);
            if (text == null || text.equals(""))
                text = "Radio text";
            checked = a.getBoolean(R.styleable.CheckBoxView_CBV_Checked, false);
            startIconSize = a.getDimensionPixelSize(R.styleable.CheckBoxView_CBV_IconSize, ResourceUtils.getDimenPxById(context, R.dimen.checkable_icon_default_size));
            textSize = a.getDimensionPixelSize(R.styleable.CheckBoxView_CBV_TextSize, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_text_size));
            startIconSpacing = a.getDimensionPixelSize(R.styleable.CheckBoxView_CBV_SpacingIcon, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_text_spacing));
            endButtonSpacing = a.getDimensionPixelSize(R.styleable.CheckBoxView_CBV_SpacingButton, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_text_button_spacing));
            textPosition = TextPosition.valueOf(a.getInt(R.styleable.CheckBoxView_CBV_TextPosition, TextPosition.START.value));
            boolean hasGlobalIconPadding = a.hasValue(R.styleable.CheckBoxView_CBV_IconPadding);
            if (hasGlobalIconPadding) {
                int textPadding = a.getDimensionPixelSize(R.styleable.CheckBoxView_CBV_IconPadding, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_padding));
                iconPadding = new int[]{textPadding, textPadding, textPadding, textPadding};
            } else {
                int left = a.getDimensionPixelSize(R.styleable.CheckBoxView_CBV_IconPaddingStart, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_padding));
                int top = a.getDimensionPixelSize(R.styleable.CheckBoxView_CBV_IconPaddingTop, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_padding));
                int right = a.getDimensionPixelSize(R.styleable.CheckBoxView_CBV_IconPaddingEnd, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_padding));
                int bottom = a.getDimensionPixelSize(R.styleable.CheckBoxView_CBV_IconPaddingBottom, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_padding));
                iconPadding = new int[]{left, top, right, bottom};
            }
        } finally {
            a.recycle();
        }
    }

    private void setViewText(String title) {
        this.text = title;
        textView.setText(title);
    }

    private void setCheckedStateInternally(boolean newValue) {
        if (newValue == checked)
            return;
        checked = newValue;
        checkBox.setChecked(newValue);
        if (onCheckedChangeListener != null)
            onCheckedChangeListener.execute(this, checked);
        if (dataBindingCheckChangeListener != null)
            dataBindingCheckChangeListener.execute(this, checked);
        if (onGroupCheckedListener != null)
            onGroupCheckedListener.execute(this, newValue);
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState myState = new SavedState(superState);
        myState.isChecked = checked;
        myState.textSize = textSize;
        myState.startIconSize = startIconSize;
        myState.startIconSpacing = startIconSpacing;
        myState.textPosition = textPosition.value;
        myState.buttonSpacing = endButtonSpacing;
        myState.title = text;
        return myState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        checked = savedState.isChecked;
        textSize = savedState.textSize;
        startIconSize = savedState.startIconSize;
        startIconSpacing = savedState.startIconSpacing;
        endButtonSpacing = savedState.buttonSpacing;
        textPosition = TextPosition.valueOf(savedState.textPosition);
        text = savedState.title;
        setupViews();
    }

    private static class SavedState extends BaseSavedState {
        private boolean isChecked;
        private int textSize;
        private int startIconSpacing;
        private int buttonSpacing;
        private int startIconSize;
        private int textPosition;
        private int[] IconPadding;
        private String title;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            isChecked = in.readByte() != 0;
            textSize = in.readInt();
            startIconSize = in.readInt();
            startIconSpacing = in.readInt();
            buttonSpacing = in.readInt();
            textPosition = in.readInt();
            title = in.readString();
            in.readIntArray(IconPadding);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (isChecked ? 1 : 0));
            out.writeInt(textSize);
            out.writeInt(startIconSize);
            out.writeInt(startIconSpacing);
            out.writeInt(buttonSpacing);
            out.writeInt(textPosition);
            out.writeString(title);
            out.writeIntArray(IconPadding);
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public enum TextPosition {
        START(0),
        END(1);

        private final int value;
        private static final SparseArray<TextPosition> values = new SparseArray<>();

        TextPosition(int value) {
            this.value = value;
        }

        static {
            for (TextPosition type : TextPosition.values()) {
                values.put(type.value, type);
            }
        }

        public static TextPosition valueOf(int type) {
            return values.get(type);
        }

        public int getValue() {
            return value;
        }
    }

    public interface ViewCheckedChangeListener {
        void execute(CheckBoxView view, boolean isChecked);
    }
}
