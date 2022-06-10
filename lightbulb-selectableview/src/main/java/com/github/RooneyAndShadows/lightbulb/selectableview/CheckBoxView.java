package com.github.rooneyandshadows.lightbulb.selectableview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.github.rooneyandshadows.java.commons.string.StringUtils;
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

@SuppressWarnings("unused")
public class CheckBoxView extends LinearLayoutCompat {
    private Drawable icon;
    private Drawable iconBackground;
    private boolean checked;
    private boolean validationEnabled;
    private boolean errorEnabled;
    private int textSize;
    private int startIconSize;
    private int textSpacing;
    private int[] iconPadding;
    private String text = "";
    private String errorText = "";
    private AppCompatImageView iconView;
    private MaterialCheckBox checkBox;
    private AppCompatTextView textView;
    private AppCompatTextView errorTextView;
    private TextPosition textPosition;
    private LinearLayoutCompat checkboxWrapper;
    private ScaleType iconScaleType;
    private OnCheckedChangeListener onCheckedChangeListener;
    private OnCheckedChangeListener dataBindingCheckChangeListener;
    private final List<ValidationCallback> validationCallbacks = new ArrayList<>();

    public CheckBoxView(Context context) {
        this(context, null);
    }

    public CheckBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(true);
        readAttributes(context, attrs);
        initializeView();
    }

    public void addValidationCallback(ValidationCallback validationCallback) {
        validationCallbacks.add(validationCallback);
    }

    public void addOrReplaceValidationCallback(ValidationCallback validationCallback) {
        validationCallbacks.remove(validationCallback);
        validationCallbacks.add(validationCallback);
    }

    public void removeValidationCallback(ValidationCallback validationCallback) {
        validationCallbacks.remove(validationCallback);
    }

    public void setOnCheckedListener(OnCheckedChangeListener listener) {
        onCheckedChangeListener = listener;
    }

    public void setText(String text) {
        this.text = text;
        textView.setText(text);
    }

    public void setIcon(Drawable icon) {
        setIcon(icon, null);
    }

    public void setIcon(Drawable icon, Drawable iconBackground) {
        this.icon = icon;
        this.iconBackground = iconBackground;
        setupIconView();
    }

    public void setIconScaleType(ScaleType iconScaleType) {
        this.iconScaleType = iconScaleType;
        iconView.setScaleType(iconScaleType);
    }

    public void setIconBackground(Drawable iconBackground) {
        this.iconBackground = iconBackground;
        iconView.setBackground(iconBackground);
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        textView.setTextSize(textSize);
    }

    public void setStartIconSize(int startIconSize) {
        this.startIconSize = startIconSize;
        setupStartIconSize();
    }

    public void setTextSpacing(int textSpacing) {
        this.textSpacing = textSpacing;
        setupIconView();
    }

    public void setChecked(boolean newValue) {
        if (newValue == checked)
            return;
        checked = newValue;
        checkBox.setChecked(newValue);
        if (onCheckedChangeListener != null)
            onCheckedChangeListener.execute(this, checked);
        if (dataBindingCheckChangeListener != null)
            dataBindingCheckChangeListener.execute(this, checked);
        validate();
    }

    public void setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
        validate();
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
        errorTextView.setText(errorText);
    }

    public void setIconPadding(int left, int top, int right, int bottom) {
        iconPadding = new int[]{left, top, right, bottom};
        iconView.setPadding(left, top, right, bottom);
    }

    public void setTextPosition(TextPosition textPosition) {
        this.textPosition = textPosition;
        setupViewsOrder();
    }

    public boolean isChecked() {
        return checked;
    }

    public String getText() {
        return text;
    }

    public Drawable getIcon() {
        return icon;
    }

    public Drawable getIconBackground() {
        return iconBackground;
    }

    public boolean isValidationEnabled() {
        return validationEnabled;
    }

    public int getTextSize() {
        return textSize;
    }

    public int getStartIconSize() {
        return startIconSize;
    }

    public int getTextSpacing() {
        return textSpacing;
    }

    public String getErrorText() {
        return errorText;
    }

    public boolean validate() {
        boolean isValid = true;
        if (validationEnabled && isEnabled()) {
            for (ValidationCallback validationCallback : validationCallbacks)
                isValid &= validationCallback.execute(isChecked());
        }
        if (!isValid) {
            setErrorEnabled(true);
        } else {
            setErrorEnabled(false);
            setErrorText(null);
        }
        return isValid;
    }

    @BindingAdapter("CBV_Text")
    public static void setText(CheckBoxView view, String title) {
        view.setText(title);
    }

    @BindingAdapter("CBV_Checked")
    public static void setSelectableChecked(CheckBoxView view, Boolean checked) {
        if (view.isChecked() != checked)
            view.setChecked(checked);
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

    private void initializeView() {
        setOrientation(VERTICAL);
        setClickable(true);
        inflate(getContext(), R.layout.checkbox_selectable_view, this);
        setOnClickListener(v -> checkBox.toggle());
        checkboxWrapper = findViewById(R.id.checkboxWrapper);
        iconView = findViewById(R.id.selectableIconImageView);
        textView = findViewById(R.id.selectableTextView);
        errorTextView = findViewById(R.id.errorTextView);
        checkBox = findViewById(R.id.selectableCheckableView);
        setupViews();
    }

    private void setupViews() {
        setupViewsOrder();
        setupIconView();
        setupTextView();
        setupErrorTextView();
        setupCheckboxView();
    }

    private void setupViewsOrder() {
        int childCount = checkboxWrapper.getChildCount();
        switch (textPosition) {
            case START:
                if (checkboxWrapper.getChildAt(0) instanceof MaterialCheckBox) {
                    checkboxWrapper.removeViewAt(0);
                    addView(checkBox, childCount - 1);
                }
                break;
            case END:
                if (!(getChildAt(0) instanceof MaterialCheckBox)) {
                    checkboxWrapper.removeViewAt(childCount - 1);
                    checkboxWrapper.addView(checkBox, 0);
                }
                break;
        }
    }

    private void setErrorEnabled(Boolean errorEnabled) {
        if (this.errorEnabled != errorEnabled) {
            this.errorEnabled = errorEnabled;
            errorTextView.setVisibility(this.errorEnabled ? VISIBLE : GONE);
        }
    }

    private void setupIconView() {
        LayoutParams params = (LayoutParams) iconView.getLayoutParams();
        params.width = startIconSize;
        params.height = startIconSize;
        params.setMarginEnd(textSpacing);
        iconView.setVisibility(icon != null ? VISIBLE : GONE);
        iconView.setLayoutParams(params);
        iconView.setScaleType(iconScaleType);
        iconView.setImageDrawable(icon);
        iconView.setPadding(iconPadding[0], iconPadding[1], iconPadding[2], iconPadding[3]);
        iconView.setBackground(iconBackground);
    }

    private void setupErrorTextView() {
        errorTextView.setText(errorText);
        errorTextView.setVisibility(errorEnabled ? VISIBLE : GONE);
    }

    private void setupTextView() {
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        textView.setClickable(false);
        textView.setIncludeFontPadding(false);
    }

    private void setupCheckboxView() {
        checkBox.setChecked(checked);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> setChecked(isChecked));
    }

    private void setupStartIconSize() {
        LayoutParams params = (LayoutParams) iconView.getLayoutParams();
        params.width = startIconSize;
        params.height = startIconSize;
        iconView.setLayoutParams(params);
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CheckBoxView, 0, 0);
        try {
            icon = a.getDrawable(R.styleable.CheckBoxView_CBV_Icon);
            iconBackground = a.getDrawable(R.styleable.CheckBoxView_CBV_IconBackground);
            text = StringUtils.getOrDefault(a.getString(R.styleable.CheckBoxView_CBV_Text), "Text");
            errorText = a.getString(R.styleable.CheckBoxView_CBV_ErrorText);
            checked = a.getBoolean(R.styleable.CheckBoxView_CBV_Checked, false);
            validationEnabled = a.getBoolean(R.styleable.CheckBoxView_CBV_ValidationEnabled, false);
            startIconSize = a.getDimensionPixelSize(R.styleable.CheckBoxView_CBV_IconSize, ResourceUtils.getDimenPxById(context, R.dimen.checkable_icon_default_size));
            textSize = a.getDimensionPixelSize(R.styleable.CheckBoxView_CBV_TextSize, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_text_size));
            textSpacing = a.getDimensionPixelSize(R.styleable.CheckBoxView_CBV_TextSpacing, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_text_button_spacing));
            textPosition = TextPosition.valueOf(a.getInt(R.styleable.CheckBoxView_CBV_TextPosition, TextPosition.START.value));
            ScaleType[] scaleTypes = ScaleType.values();
            iconScaleType = scaleTypes[a.getInt(R.styleable.CheckBoxView_CBV_IconScaleType, 7)];
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
        myState.validationEnabled = validationEnabled;
        myState.errorEnabled = errorEnabled;
        myState.textSize = textSize;
        myState.startIconSize = startIconSize;
        myState.textPosition = textPosition.value;
        myState.buttonSpacing = textSpacing;
        myState.text = text;
        myState.iconScaleType = iconScaleType.name();
        myState.errorText = errorText;
        return myState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        checked = savedState.isChecked;
        validationEnabled = savedState.validationEnabled;
        errorEnabled = savedState.errorEnabled;
        textSize = savedState.textSize;
        startIconSize = savedState.startIconSize;
        textSpacing = savedState.buttonSpacing;
        textPosition = TextPosition.valueOf(savedState.textPosition);
        text = savedState.text;
        iconScaleType = ScaleType.valueOf(savedState.iconScaleType);
        errorText = savedState.errorText;
        setupViews();
    }

    private static class SavedState extends BaseSavedState {
        private boolean isChecked;
        private boolean validationEnabled;
        private boolean errorEnabled;
        private int textSize;
        private int buttonSpacing;
        private int startIconSize;
        private int textPosition;
        private int[] IconPadding;
        private String text;
        private String errorText;
        private String iconScaleType;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            isChecked = in.readByte() != 0;
            validationEnabled = in.readByte() != 0;
            errorEnabled = in.readByte() != 0;
            textSize = in.readInt();
            startIconSize = in.readInt();
            buttonSpacing = in.readInt();
            textPosition = in.readInt();
            text = in.readString();
            errorText = in.readString();
            iconScaleType = in.readString();
            in.readIntArray(IconPadding);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (isChecked ? 1 : 0));
            out.writeByte((byte) (validationEnabled ? 1 : 0));
            out.writeByte((byte) (errorEnabled ? 1 : 0));
            out.writeInt(textSize);
            out.writeInt(startIconSize);
            out.writeInt(buttonSpacing);
            out.writeInt(textPosition);
            out.writeString(text);
            out.writeString(errorText);
            out.writeString(iconScaleType);
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

    public interface ValidationCallback {
        boolean execute(boolean isChecked);
    }

    public interface OnCheckedChangeListener {
        void execute(CheckBoxView view, boolean isChecked);
    }
}