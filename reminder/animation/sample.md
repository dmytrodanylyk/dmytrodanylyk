### Android Animations Example of different API

Object Animator

```java
@TargetApi(11)
public class ObjectAnimator {

    public void rotate(View view) {

            PropertyValuesHolder rotateY = PropertyValuesHolder.ofFloat(View.ROTATION_Y, 0, 90);
            PropertyValuesHolder rotateX = PropertyValuesHolder.ofFloat(View.ROTATION_X, 0, 90);

            android.animation.ObjectAnimator animator = android.animation.ObjectAnimator.ofPropertyValuesHolder(view,
                            rotateX, rotateY);
            animator.setDuration(1000); // 1 second
            animator.start();
    }

    public void alpha(View view) {

            PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0, 1);

            android.animation.ObjectAnimator animator = android.animation.ObjectAnimator.ofPropertyValuesHolder(view,
                            alpha);
            animator.setDuration(1000); // 1 second
            animator.start();
    }

    public void scale(View view) {

            PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0, 1);
            PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0, 1);

            android.animation.ObjectAnimator animator = android.animation.ObjectAnimator.ofPropertyValuesHolder(view,
                            scaleY, scaleX);
            animator.setDuration(1000); // 1 second
            animator.start();
    }

    public void translate(View view) {

            PropertyValuesHolder translateX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0, 100);
            PropertyValuesHolder translateY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0, 100);

            android.animation.ObjectAnimator animator = android.animation.ObjectAnimator.ofPropertyValuesHolder(view,
                            translateX, translateY);
            animator.setDuration(1000); // 1 second
            animator.start();
    }
}
```

Property Animations

```java
@TargetApi(12)
public class PropertyAnimations {


    public void rotate(View view) {
            view.animate().rotationX(90).rotationY(90).start();
    }

    public void alpha(View view) {
            view.animate().alpha(0).start();
    }

    public void scale(View view) {
            view.animate().scaleX(0).scaleY(0).start();
    }

    public void translate(View view) {
            view.animate().translationX(100).translationY(100).start();
    }
}
```

View Animations

```java
@TargetApi(1)
public class ViewAnimations {

    public void alpha(View view) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setDuration(1000); // 1 second
            view.startAnimation(alphaAnimation);
    }

    public void rotate(View view) {
            RotateAnimation rotateAnimation = new RotateAnimation(90, 180);
            rotateAnimation.setDuration(1000); // 1 second
            view.startAnimation(rotateAnimation);
    }

    public void scale(View view) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f);
            scaleAnimation.setDuration(1000); // 1 second
            view.startAnimation(scaleAnimation);
    }

    public void translate(View view) {
            TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 100.0f, 0.0f, 100.0f);
            translateAnimation.setDuration(1000); // 1 second
            view.startAnimation(translateAnimation);
    }

    public void animationSet(View view) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f);
            scaleAnimation.setDuration(1000); // 1 second

            AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setDuration(1000); // 1 second

            AnimationSet animationSet = new AnimationSet(true);
            animationSet.addAnimation(scaleAnimation);
            animationSet.addAnimation(alphaAnimation);
            animationSet.setFillAfter(true);

            view.startAnimation(animationSet);
    }
}
```
