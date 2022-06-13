package com.github.rooneyandshadows.lightbulb.selectableview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.widget.ImageView.ScaleType;

import com.github.rooneyandshadows.java.commons.string.StringUtils;
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

@SuppressWarnings("unused")
public class RadioButtonView extends LinearLayoutCompat {
    private Drawable icon;
    private Drawable iconBackground;
    private boolean checked;
    private boolean validationEnabled;
    private boolean errorEnabled;
    private boolean enabled;
    private int textSize;
    private int startIconSize;
    private int textSpacing;
    private int iconSpacing;
    private int[] iconPadding;
    private String text = "";
    private String errorText = "";
    private AppCompatImageView iconView;
    private MaterialRadioButton radioButton;
    private AppCompatTextView textView;
    private AppCompatTextView errorTextView;
    private TextPosition textPosition;
    private LinearLayoutCompat radioButtonWrapper;
    private ScaleType iconScaleType;
    private OnCheckedChangeListener onCheckedChangeListener;
    private OnCheckedChangeListener dataBindingCheckChangeListener;
    private OnCheckedChangeListener onGroupCheckedListener;
    private final List<ValidationCallback> validationCallbacks = new ArrayList<>();

    public RadioButtonView(Context context) {
        this(context, null);
    }

    public RadioButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(true);
        readAttributes(context, attrs);
        initializeView();
    }

    void setOnGroupCheckedListener(OnCheckedChangeListener listener) {
        onGroupCheckedListener = listener;
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

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enabled = enabled;
        textView.setEnabled(enabled);
        radioButton.setEnabled(enabled);
        iconView.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
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

    public void setIconSpacing(int iconSpacing) {
        this.iconSpacing = textSpacing;
        setupIconSpacing();
    }

    public void setTextSpacing(int textSpacing) {
        this.textSpacing = textSpacing;
        setupTextSpacing();
    }

    public void setChecked(boolean newValue) {
        if (newValue == checked)
            return;
        checked = newValue;
        radioButton.setChecked(newValue);
        if (onCheckedChangeListener != null)
            onCheckedChangeListener.execute(this, checked);
        if (dataBindingCheckChangeListener != null)
            dataBindingCheckChangeListener.execute(this, checked);
        if (onGroupCheckedListener != null)
            onGroupCheckedListener.execute(this, newValue);
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
    public static void setText(RadioButtonView view, String title) {
        view.setText(title);
    }

    @BindingAdapter("CBV_Checked")
    public static void setSelectableChecked(RadioButtonView view, Boolean checked) {
        if (view.isChecked() != checked)
            view.setChecked(checked);
    }

    @InverseBindingAdapter(attribute = "CBV_Checked", event = "CBV_CheckedAttributeChanged")
    public static Boolean getSelectableChecked(RadioButtonView view) {
        return view.checked;
    }

    @BindingAdapter("CBV_CheckedAttributeChanged")
    public static void setListeners(RadioButtonView view, final InverseBindingListener attrChange) {
        view.radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (view.onCheckedChangeListener != null)
                view.onCheckedChangeListener.execute(view, isChecked);
            attrChange.onChange();
        });
    }

    private void initializeView() {
        setOrientation(VERTICAL);
        setClickable(true);
        inflate(getContext(), R.layout.radio_selectable_view, this);
        setOnClickListener(v -> radioButton.toggle());
        radioButtonWrapper = findViewById(R.id.radioButtonWrapper);
        iconView = findViewById(R.id.selectableIconImageView);
        textView = findViewById(R.id.selectableTextView);
        errorTextView = findViewById(R.id.errorTextView);
        radioButton = findViewById(R.id.selectableCheckableView);
        setupViews();
    }

    private void setupViews() {
        textView.setEnabled(isEnabled());
        radioButton.setEnabled(isEnabled());
        iconView.setEnabled(isEnabled());
        setupViewsOrder();
        setupIconView();
        setupTextView();
        setupErrorTextView();
        setupRadioButtonView();
    }

    private void setupViewsOrder() {
        int childCount = radioButtonWrapper.getChildCount();
        switch (textPosition) {
            case START:
                if (radioButtonWrapper.getChildAt(0) instanceof MaterialCheckBox) {
                    radioButtonWrapper.removeViewAt(0);
                    addView(radioButton, childCount - 1);
                }
                break;
            case END:
                if (!(getChildAt(0) instanceof MaterialCheckBox)) {
                    radioButtonWrapper.removeViewAt(childCount - 1);
                    radioButtonWrapper.addView(radioButton, 0);
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
        iconView.setVisibility(icon != null ? VISIBLE : GONE);
        iconView.setLayoutParams(params);
        iconView.setScaleType(iconScaleType);
        iconView.setImageDrawable(icon);
        iconView.setPadding(iconPadding[0], iconPadding[1], iconPadding[2], iconPadding[3]);
        iconView.setBackground(iconBackground);
        setupIconSpacing();
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
        setupTextSpacing();
    }

    private void setupIconSpacing() {
        LayoutParams params = (LayoutParams) iconView.getLayoutParams();
        switch (textPosition) {
            case START:
                params.setMarginEnd(iconSpacing);
                break;
            case END:
                params.setMarginStart(iconSpacing);
                break;
        }
        iconView.setLayoutParams(params);
    }

    private void setupTextSpacing() {
        switch (textPosition) {
            case START:
                textView.setPadding(textView.getPaddingLeft(), textView.getPaddingTop(), textSpacing, textView.getPaddingBottom());
                break;
            case END:
                textView.setPadding(textSpacing, textView.getPaddingTop(), textView.getPaddingRight(), textView.getPaddingBottom());
                break;
        }
    }

    private void setupRadioButtonView() {
        radioButton.setChecked(checked);
        radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> setChecked(isChecked));
    }

    private void setupStartIconSize() {
        LayoutParams params = (LayoutParams) iconView.getLayoutParams();
        params.width = startIconSize;
        params.height = startIconSize;
        iconView.setLayoutParams(params);
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RadioButtonView, 0, 0);
        try {
            icon = a.getDrawable(R.styleable.RadioButtonView_RBV_Icon);
            iconBackground = a.getDrawable(R.styleable.RadioButtonView_RBV_IconBackground);
            text = StringUtils.getOrDefault(a.getString(R.styleable.RadioButtonView_RBV_Text), "Text");
            errorText = a.getString(R.styleable.RadioButtonView_RBV_ErrorText);
            checked = a.getBoolean(R.styleable.RadioButtonView_RBV_Checked, false);
            validationEnabled = a.getBoolean(R.styleable.RadioButtonView_RBV_ValidationEnabled, false);
            enabled = a.getBoolean(R.styleable.RadioButtonView_RBV_ValidationEnabled, true);
            startIconSize = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_IconSize, ResourceUtils.getDimenPxById(context, R.dimen.checkable_icon_default_size));
            textSize = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_TextSize, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_text_size));
            textSpacing = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_TextSpacing, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_text_spacing));
            iconSpacing = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_IconSpacing, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_spacing));
            textPosition = TextPosition.valueOf(a.getInt(R.styleable.RadioButtonView_RBV_TextPosition, TextPosition.START.value));
            ScaleType[] scaleTypes = ScaleType.values();
            iconScaleType = scaleTypes[a.getInt(R.styleable.RadioButtonView_RBV_IconScaleType, 7)];
            boolean hasGlobalIconPadding = a.hasValue(R.styleable.RadioButtonView_RBV_IconPadding);
            if (hasGlobalIconPadding) {
                int textPadding = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_IconPadding, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_spacing));
                iconPadding = new int[]{textPadding, textPadding, textPadding, textPadding};
            } else {
                int left = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_IconPaddingStart, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_spacing));
                int top = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_IconPaddingTop, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_spacing));
                int right = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_IconPaddingEnd, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_spacing));
                int bottom = a.getDimensionPixelSize(R.styleable.RadioButtonView_RBV_IconPaddingBottom, ResourceUtils.getDimenPxById(context, R.dimen.checkable_default_icon_spacing));
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
        myState.enabled = enabled;
        myState.textSize = textSize;
        myState.startIconSize = startIconSize;
        myState.textPosition = textPosition.value;
        myState.textSpacing = textSpacing;
        myState.iconSpacing = iconSpacing;
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
        enabled = savedState.enabled;
        textSize = savedState.textSize;
        startIconSize = savedState.startIconSize;
        textSpacing = savedState.textSpacing;
        iconSpacing = savedState.iconSpacing;
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
        private boolean enabled;
        private int textSize;
        private int textSpacing;
        private int iconSpacing;
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
            enabled = in.readByte() != 0;
            textSize = in.readInt();
            startIconSize = in.readInt();
            textSpacing = in.readInt();
            iconSpacing = in.readInt();
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
            out.writeByte((byte) (enabled ? 1 : 0));
            out.writeInt(textSize);
            out.writeInt(startIconSize);
            out.writeInt(textSpacing);
            out.writeInt(iconSpacing);
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
        void execute(RadioButtonView view, boolean isChecked);
    }
}