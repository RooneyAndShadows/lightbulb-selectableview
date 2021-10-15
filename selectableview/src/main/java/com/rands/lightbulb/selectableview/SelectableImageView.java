package com.rands.lightbulb.selectableview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.rands.lightbulb.commons.utils.ResourceUtils;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

@SuppressWarnings("unused")
public class SelectableImageView extends AppCompatImageView {
    private boolean checked;
    private boolean isCheckable;
    private Drawable drawableUnselected;
    private Drawable drawableSelected;
    private int drawableTint;
    private ObjectAnimator oa1;
    private ObjectAnimator oa2;
    private ViewCheckedChangeListener onGroupCheckedListener;
    private ViewCheckedChangeListener onCheckedChangeListener;
    private ViewCheckedChangeListener dataBindingCheckChangeListener;

    public SelectableImageView(Context context, AttributeSet attr) {
        super(context, attr);
        readAttributes(context, attr);
        initializeView();
    }

    public SelectableImageView(Context context) {
        this(context, null);
    }

    void setOnGroupCheckedListener(ViewCheckedChangeListener listener) {
        onGroupCheckedListener = listener;
    }

    public void setOnCheckedListener(ViewCheckedChangeListener listener) {
        this.onCheckedChangeListener = listener;
    }

    protected void readAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SelectableImageView, 0, 0);
        try {
            drawableTint = a.getColor(R.styleable.SelectableImageView_SIV_DrawableTint, ResourceUtils.getColorByAttribute(context, R.attr.colorOnSurface));
            drawableUnselected = a.getDrawable(R.styleable.SelectableImageView_SIV_DrawableUnselected);
            drawableSelected = a.getDrawable(R.styleable.SelectableImageView_SIV_DrawableSelected);
            if (drawableSelected == null)
                drawableSelected = ResourceUtils.getDrawable(getContext(), R.drawable.indicator_selected);
            if (drawableUnselected == null)
                drawableUnselected = ResourceUtils.getDrawable(getContext(), R.drawable.indicator_not_selected);
            checked = a.getBoolean(R.styleable.SelectableImageView_SIV_Checked, false);
            isCheckable = a.getBoolean(R.styleable.SelectableImageView_SIV_Checked, true);
        } finally {
            a.recycle();
        }
    }

    @BindingAdapter("SIV_Checked")
    public static void setSelectableChecked(SelectableImageView view, Boolean checked) {
        if (view.isChecked() != checked)
            view.setCheckedStateInternally(checked, false);
    }

    @InverseBindingAdapter(attribute = "SIV_Checked", event = "SIV_CheckedAttributeChanged")
    public static Boolean getSelectableChecked(SelectableImageView view) {
        return view.checked;
    }

    @BindingAdapter("SIV_CheckedAttributeChanged")
    public static void setListeners(SelectableImageView selectableView, final InverseBindingListener attrChange) {
        selectableView.dataBindingCheckChangeListener = (view, isChecked) -> {
            if (view.onCheckedChangeListener != null)
                view.onCheckedChangeListener.execute(view, isChecked);
            attrChange.onChange();
        };
    }

    public void setCheckable(boolean checkable) {
        isCheckable = checkable;
    }

    public void setChecked(boolean newState) {
        if (!isCheckable)
            return;
        setCheckedStateInternally(newState, true);
    }

    public void setChecked(boolean newState, boolean animate) {
        if (!isCheckable)
            return;
        setCheckedStateInternally(newState, animate);
    }

    public void toggleChecked(boolean animate) {
        if (!isCheckable)
            return;
        setCheckedStateInternally(!checked, animate);
    }

    public void setDrawableUnselected(Drawable defaultImage) {
        this.drawableUnselected = defaultImage;
        if (this.drawableUnselected != null)
            this.drawableUnselected.setTint(drawableTint);
        changeCurrentDrawableAccordingToState(false);
    }

    public void setSelectedDrawable(Drawable selectedImage) {
        this.drawableSelected = selectedImage;
        if (this.drawableSelected != null)
            this.drawableSelected.setTint(drawableTint);
        changeCurrentDrawableAccordingToState(false);
    }

    public void setDrawables(Drawable unselected, Drawable selected) {
        drawableUnselected = unselected;
        drawableSelected = unselected;
        setupDrawables();
    }

    public void setDrawableTint(@ColorInt int drawableTint) {
        this.drawableTint = drawableTint;
        setupDrawables();
    }

    public boolean isChecked() {
        return checked;
    }

    private void initializeView() {
        if (isInEditMode()) {
            getLayoutParams().height = ResourceUtils.dpToPx(40);
            getLayoutParams().width = ResourceUtils.dpToPx(40);
        }
        setScaleType(ScaleType.FIT_XY);
        setupDrawables();
        setupAnimations();
        setupEvents();
    }

    private void setupDrawables() {
        if (drawableUnselected != null)
            drawableUnselected.setTint(drawableTint);
        if (drawableSelected != null)
            drawableSelected.setTint(drawableTint);
        changeCurrentDrawableAccordingToState(false);
    }

    private void setCheckedStateInternally(boolean newState, boolean animate) {
        if (this.checked == newState)
            return;
        this.checked = newState;
        changeCurrentDrawableAccordingToState(animate);
        if (onCheckedChangeListener != null)
            onCheckedChangeListener.execute(this, newState);
        if (dataBindingCheckChangeListener != null)
            dataBindingCheckChangeListener.execute(this, checked);
        if (onGroupCheckedListener != null)
            onGroupCheckedListener.execute(this, newState);
    }

    private void changeCurrentDrawableAccordingToState(boolean animate) {
        if (animate) {
            if (oa1.isStarted())
                oa1.end();
            if (oa2.isStarted())
                oa2.end();
            oa1.start();
        } else {
            setImageDrawable(checked ? drawableSelected : drawableUnselected);
        }
    }

    private void setupAnimations() {
        oa1 = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0f);
        oa2 = ObjectAnimator.ofFloat(this, "scaleX", 0f, 1f);
        oa1.setDuration(100);
        oa2.setDuration(100);
        oa1.setInterpolator(new DecelerateInterpolator());
        oa2.setInterpolator(new AccelerateDecelerateInterpolator());
        oa1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setImageDrawable(checked ? drawableSelected : drawableUnselected);
                oa2.start();
            }
        });
    }

    private void setupEvents() {
        setOnClickListener(view -> setChecked(!checked, true));
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState myState = new SavedState(superState);
        myState.checked = checked;
        myState.isCheckable = isCheckable;
        myState.drawableTint = drawableTint;
        return myState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        drawableTint = savedState.drawableTint;
        checked = savedState.checked;
        isCheckable = savedState.isCheckable;
        setupDrawables();
    }

    private static class SavedState extends BaseSavedState {
        private int drawableTint;
        private boolean checked;
        private boolean isCheckable;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            checked = in.readByte() != 0;
            isCheckable = in.readByte() != 0;
            drawableTint = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (checked ? 1 : 0));
            out.writeByte((byte) (isCheckable ? 1 : 0));
            out.writeInt(drawableTint);
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
        void execute(SelectableImageView view, boolean isChecked);
    }
}