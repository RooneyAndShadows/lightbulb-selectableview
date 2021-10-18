package com.github.rooneyandshadows.lightbulb.selectableview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.appcompat.widget.LinearLayoutCompat;

public class SelectableRadioGroup extends LinearLayoutCompat {
    private int checkedId = -1;
    private boolean protectFromCheckedChange = false;
    private OnCheckedChangeListener onCheckedChangeListener;

    public SelectableRadioGroup(Context context) {
        this(context, null);
    }

    public SelectableRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(true);
        readAttributes(context, attrs);
        initViews();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setCheckedIdInternally(checkedId, false);
    }

    @Override
    public void addView(View child) {
        if (!(child instanceof RadioButtonView)) {
            Log.w(SelectableRadioGroup.class.getName(), "Child view is ignored. Reason: Child views must be " + RadioButtonView.class.getName());
            return;
        }
        setupInternalCallbacks((RadioButtonView) child);
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (!(child instanceof RadioButtonView)) {
            Log.w(SelectableRadioGroup.class.getName(), "Child view is ignored. Reason: Child views must be " + RadioButtonView.class.getName());
            return;
        }
        setupInternalCallbacks((RadioButtonView) child);
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int width, int height) {
        if (!(child instanceof RadioButtonView)) {
            Log.w(SelectableRadioGroup.class.getName(), "Child view is ignored. Reason: Child views must be " + RadioButtonView.class.getName());
            return;
        }
        setupInternalCallbacks((RadioButtonView) child);
        super.addView(child, width, height);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (!(child instanceof RadioButtonView)) {
            Log.w(SelectableRadioGroup.class.getName(), "Child view is ignored. Reason: Child views must be " + RadioButtonView.class.getName());
            return;
        }
        setupInternalCallbacks((RadioButtonView) child);
        super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!(child instanceof RadioButtonView)) {
            Log.w(SelectableRadioGroup.class.getName(), "Child view is ignored. Reason: Child views must be " + RadioButtonView.class.getName());
            return;
        }
        setupInternalCallbacks((RadioButtonView) child);
        super.addView(child, index, params);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SelectableRadioGroup, 0, 0);
        try {
            checkedId = a.getResourceId(R.styleable.SelectableRadioGroup_SRG_CheckedId, -1);
        } finally {
            a.recycle();
        }
    }

    private void initViews() {
        setOrientation(VERTICAL);
    }

    public void setCheckedId(int newCheckedId) {
        if (checkedId == newCheckedId)
            return;
        setCheckedIdInternally(newCheckedId, true);
    }

    public int getCheckedId() {
        return checkedId;
    }

    private void setCheckedIdInternally(int newCheckedId, boolean notifyChange) {
        int previouslyCheckedId = this.checkedId;
        RadioButtonView previousCheckedView = findViewById(previouslyCheckedId);
        this.checkedId = newCheckedId;
        if (newCheckedId == -1 && previousCheckedView != null) {
            protectFromCheckedChange = true;
            if (previousCheckedView.isChecked())
                previousCheckedView.setChecked(false);
            if (notifyChange && onCheckedChangeListener != null)
                onCheckedChangeListener.execute(checkedId, previousCheckedView);
            protectFromCheckedChange = false;
        } else {
            RadioButtonView viewToCheck = findViewById(checkedId);
            if (viewToCheck != null)
                viewToCheck.setChecked(true);
            if (notifyChange && onCheckedChangeListener != null)
                onCheckedChangeListener.execute(checkedId, viewToCheck);
        }
    }

    private void setupInternalCallbacks(RadioButtonView targetView) {
        targetView.setOnGroupCheckedListener((view, isChecked) -> {
            if (protectFromCheckedChange)
                return;
            protectFromCheckedChange = true;
            int idToCheck = -1;
            if (isChecked) {
                idToCheck = view.getId();
                getChildren()
                        .stream()
                        .filter(checkableView -> {
                            if (checkableView.getId() == -1)
                                return false;
                            return checkableView.getId() != checkedId;
                        })
                        .forEach(checkableView -> checkableView.setChecked(false));
            }
            protectFromCheckedChange = false;
            if (checkedId == idToCheck)
                return;
            setCheckedIdInternally(idToCheck, true);
        });
    }

    private ArrayList<RadioButtonView> getChildren() {
        int childCount = getChildCount();
        ArrayList<RadioButtonView> children = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            children.add((RadioButtonView) getChildAt(i));
        }
        return children;
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
        myState.checkedId = checkedId;
        return myState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        checkedId = savedState.checkedId;
        RadioButtonView v = findViewById(checkedId);
        if (v != null)
            setCheckedIdInternally(checkedId, false);
    }

    public interface OnCheckedChangeListener {
        void execute(int checkedId, RadioButtonView view);
    }

    private static class SavedState extends BaseSavedState {
        private int checkedId;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            checkedId = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(checkedId);
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
}
