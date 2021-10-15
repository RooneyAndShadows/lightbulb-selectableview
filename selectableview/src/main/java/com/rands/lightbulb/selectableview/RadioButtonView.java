package com.rands.lightbulb.selectableview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.google.android.material.radiobutton.MaterialRadioButton;
import com.rands.lightbulb.commons.utils.ResourceUtils;
import com.rands.java.commons.string.StringUtils;

import java.util.Arrays;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

@SuppressWarnings("unused")
public class RadioButtonView extends LinearLayoutCompat {
    private Drawable iconDrawable;
    private Drawable iconBackgroundDrawable;
    private boolean checked;
    private int textSize;
    private int startIconSize;
    private int startIconSpacing;
    private int endButtonSpacing;
    private int iconScaleType;
    private String text = "";
    private AppCompatImageView iconView;
    private MaterialRadioButton radioButton;
    private AppCompatTextView textView;
    private ViewCheckedChangeListener onGroupCheckedListener;
    private ViewCheckedChangeListener onCheckedChangeListener;
    private ViewCheckedChangeListener dataBindingCheckChangeListener;
    private int[] iconPadding;

    public RadioButtonView(Context context) {
        this(context, null);
    }

    public RadioButtonView(Context context, AttributeSet attrs) {
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

    @BindingAdapter("RBV_Title")
    public static void setText(RadioButtonView view, String title) {
        view.setViewText(title);
    }

    @BindingAdapter("RBV_Checked")
    public static void setSelectableChecked(RadioButtonView view, Boolean checked) {
        if (view.isChecked() != checked)
            view.setCheckedStateInternally(checked);
    }

    @InverseBindingAdapter(attribute = "RBV_Checked", event = "RBV_CheckedAttributeChanged")
    public static Boolean getSelectableChecked(RadioButtonView view) {
        return view.checked;
    }

    @BindingAdapter("RBV_CheckedAttributeChanged")
    public static void setListeners(RadioButtonView view, final InverseBindingListener attrChange) {
        view.dataBindingCheckChangeListener = (view1, isChecked) -> {
            if (view.onCheckedChangeListener != null)
                view.onCheckedChangeListener.execute(view, isChecked);
            attrChange.onChange();
        };
    }

    private void initViews(Context ctx) {
        inflate(getContext(), R.layout.radio_selectable_view, this);
        setClickable(true);
        setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP)
                radioButton.performClick();
            return false;
        });
        iconView = findViewById(R.id.selectableIconImageView);
        textView = findViewById(R.id.selectableTextView);
        radioButton = findViewById(R.id.selectableRadioButtonView);
        setupViews();
    }

    private void setupViews() {
        setupIconView();
        setupTextView();
        setupCheckbox();
    }

    private AppCompatImageView.ScaleType resolveScaleType() {
        return Arrays.stream(AppCompatImageView.ScaleType.values())
                .filter(scaleType -> scaleType.ordinal() == iconScaleType)
                .findFirst()
                .orElse(AppCompatImageView.ScaleType.CENTER_INSIDE);
    }

    private void setupIconView() {
        LayoutParams textLayoutParams = (LayoutParams) textView.getLayoutParams();
        iconView.setLayoutParams(new LayoutParams(startIconSize, startIconSize));
        if (iconDrawable == null) {
            textLayoutParams.setMarginStart(0);
            iconView.setVisibility(GONE);
            return;
        }
        iconView.setVisibility(VISIBLE);
        textLayoutParams.setMarginStart(startIconSpacing);
        iconView.setScaleType(resolveScaleType());
        iconView.setImageDrawable(iconDrawable);
        iconView.setPadding(iconPadding[0], iconPadding[1], iconPadding[2], iconPadding[3]);
        iconView.setBackground(iconBackgroundDrawable);
        LayoutParams params = (LayoutParams) iconView.getLayoutParams();
        params.setMargins(0, 0, startIconSpacing, 0);
    }

    private void setupTextView() {
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        textView.setClickable(false);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        LayoutParams params = (LayoutParams) textView.getLayoutParams();
        params.setMarginEnd(endButtonSpacing);
        textView.setLayoutParams(params);
    }

    private void setupCheckbox() {
        radioButton.setChecked(checked);
        radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> setChecked(isChecked));
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RadioButtonView, 0, 0);
        try {
            iconDrawable = a.getDrawable(R.styleable.RadioButtonView_RBV_Icon);
            iconBackgroundDrawable = a.getDrawable(R.styleable.RadioButtonView_RBV_IconBackground);
            text = StringUtils.getOrDefault(a.getString(R.styleable.RadioButtonView_RBV_Title), "Radio button");
            checked = a.getBoolean(R.styleable.RadioButtonView_RBV_Checked, false);
            startIconSize = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_IconSize, ResourceUtils.getDimenPxById(context, R.dimen.checkable_icon_default_size));
            textSize = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_TextSize, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_text_size));
            startIconSpacing = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_SpacingIcon, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_text_spacing));
            endButtonSpacing = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_SpacingButton, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_text_button_spacing));
            iconScaleType = a.getInt(R.styleable.RadioButtonView_RBV_IconScaleType, AppCompatImageView.ScaleType.CENTER_INSIDE.ordinal());
            boolean hasGlobalIconPadding = a.hasValue(R.styleable.RadioButtonView_RBV_IconPadding);
            if (hasGlobalIconPadding) {
                int textPadding = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_IconPadding, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_padding));
                iconPadding = new int[]{textPadding, textPadding, textPadding, textPadding};
            } else {
                int left = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_IconPaddingStart, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_padding));
                int top = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_IconPaddingTop, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_padding));
                int right = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_IconPaddingEnd, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_padding));
                int bottom = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_IconPaddingBottom, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_padding));
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
        radioButton.setChecked(newValue);
        if (onCheckedChangeListener != null)
            onCheckedChangeListener.execute(this, newValue);
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
        myState.iconScaleType = iconScaleType;
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
        iconScaleType = savedState.iconScaleType;
        text = savedState.title;
        setupViews();
    }

    private static class SavedState extends BaseSavedState {
        private boolean isChecked;
        private int textSize;
        private int startIconSpacing;
        private int buttonSpacing;
        private int startIconSize;
        private int iconScaleType;
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
            iconScaleType = in.readInt();
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
            out.writeInt(iconScaleType);
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

    public interface ViewCheckedChangeListener {
        void execute(RadioButtonView view, boolean isChecked);
    }
}
