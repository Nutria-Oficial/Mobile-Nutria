package com.bea.nutria.ui.Comparacao;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;

public class ForceSoftwareLayout extends ConstraintLayout {

    public ForceSoftwareLayout(Context context) {
        super(context);
        init();
    }

    public ForceSoftwareLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ForceSoftwareLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Forçar software rendering
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setWillNotDraw(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Aplicar software rendering em todos os children
        forceSoftwareRecursive(this);
    }

    private void forceSoftwareRecursive(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            android.view.View child = viewGroup.getChildAt(i);
            child.setLayerType(LAYER_TYPE_SOFTWARE, null);
            if (child instanceof ViewGroup) {
                forceSoftwareRecursive((ViewGroup) child);
            }
        }
    }
}