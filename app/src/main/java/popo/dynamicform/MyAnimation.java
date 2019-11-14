package popo.dynamicform;

import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

class MyAnimation extends Animation {
    private Matrix matrix;

    public MyAnimation(Matrix matrix) {
        this.matrix = matrix;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        t.getMatrix().set(matrix);
    }
}
