package com.amy.curveview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Created by idic
 */
public class CurveView extends View {

    List<Point> localList = new ArrayList<>();
    // X,Y轴的单位长度
    private int width;
    private int height;
    // 画笔
    private Paint paintCurve;
    private Context mContext;
    //圆画笔
    private Paint mCirclePaint;
    private Paint mDescPaint;
    private Paint mBorderLinePaint;
    private Matrix mMatrix;
    private Bitmap mBoatBitmap;
    private PathMeasure mBoatPathMeasure;
    //边框的左边距
    private float mBrokenLineLeft = 40;
    //边框的上边距
    private float mBrokenLineTop = 40;
    //边框的下边距
    private float mBrokenLineBottom = 40;
    //边框的右边距
    private float mBrokenLinerRight = 20;

    private int currentType = TYPE_TWO;
    private final static int TYPE_ONE=1;
    private final static int TYPE_TWO=2;

    private static int WAVE_OFFSET = 5;
    private int mCurWaveOffset = 0;
    private int mPath2Offset = 0;
    private float currentValue = 0f;
    private ValueAnimator mAnimator;
    //波浪曲线1
    Path wavePath;
    private int WAVE_LENGTH;
    private int WAVE_HEIGHT;
    //波浪曲线2
    Path wavePath2;
    private int WAVE_HEIGHT2;
    //鱼曲线
    Path fishPath;
    private int FISH_LENGTH;
    private int FISH_HEIGHT;


    public CurveView(Context context) {
        this(context, null);
    }

    public CurveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CurveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    /**
     * 初始化数据值和画笔
     */
    public void init() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        mBoatBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_fish2, options);
        mMatrix = new Matrix();
        mBoatPathMeasure = new PathMeasure();

        paintCurve = new Paint();
        paintCurve.setAntiAlias(true);


        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setColor(Color.BLACK);

        mDescPaint = new Paint();
        mDescPaint.setAntiAlias(true);
        mDescPaint.setColor(Color.parseColor("#FAAB18"));
        mDescPaint.setTextSize(sp2px(mContext, 13));
        if (mBorderLinePaint == null) {
            mBorderLinePaint = new Paint();
            mBorderLinePaint.setTextSize(20);
        }

        mAnimator = ValueAnimator.ofFloat(0, 1f);
        mAnimator.setDuration(4000);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentValue = (float) animation.getAnimatedValue();
                mCurWaveOffset = (mCurWaveOffset + WAVE_OFFSET) % width;
                mPath2Offset = (mPath2Offset + WAVE_OFFSET / 2) % width;
                postInvalidate();
            }
        });

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();

        wavePath = new Path();
        WAVE_LENGTH = width / 3;
        WAVE_HEIGHT = 40;
        initPath(wavePath, WAVE_LENGTH, WAVE_HEIGHT, 2, true);

        WAVE_HEIGHT2 = 30;
        wavePath2 = new Path();
        initPath(wavePath2, WAVE_LENGTH, WAVE_HEIGHT2, 2, true);

        FISH_LENGTH = width / 2;
        FISH_HEIGHT = 120;
        fishPath = new Path();
        initPath(fishPath, FISH_LENGTH, FISH_HEIGHT, 1, false);

        // 让 PathMeasure 与 Path 关联
        mBoatPathMeasure.setPath(fishPath, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        initXY();
        drawBorderLineAndText(canvas);//画边框线
        drawCurve(canvas);//画曲线
    }

    /**
     * 绘制边框线和边框文本
     */
    private void drawBorderLineAndText(Canvas canvas) {
        mBorderLinePaint.setStyle(Paint.Style.FILL);
        mBorderLinePaint.setColor(ContextCompat.getColor(mContext, R.color.lineColor));
        /**绘制边框竖线*/
        canvas.drawLine(mBrokenLineLeft, mBrokenLineTop - 10, mBrokenLineLeft,
                getMeasuredHeight() - mBrokenLineBottom, mBorderLinePaint);
        /**绘制边框横线*/
        canvas.drawLine(mBrokenLineLeft, getMeasuredHeight() - mBrokenLineBottom,
                getMeasuredHeight(), getMeasuredHeight() - mBrokenLineBottom, mBorderLinePaint);
        /**绘制边框分段横线与分段文本*/
        float averageHeight = (getMeasuredHeight() - mBrokenLineBottom - mBrokenLineTop) / 10;
        mBorderLinePaint.setTextAlign(Paint.Align.RIGHT);
        mBorderLinePaint.setColor(Color.GRAY);
        for (int i = 0; i < 10; i++) {
            float lineHeight = averageHeight * i;
            canvas.drawLine(mBrokenLineLeft, lineHeight + mBrokenLineTop,
                    getMeasuredHeight() - mBrokenLinerRight, lineHeight + mBrokenLineTop,
                    mBorderLinePaint);
            canvas.drawText((10 - i) * 10 + "", mBrokenLineLeft - 5, lineHeight + mBrokenLineTop,
                    mBorderLinePaint);
        }

    }


    /**
     * 获取二阶贝塞尔控制点坐标
     *
     * @param localList
     * @param index     下标
     * @param pStart    开始点
     * @param pEnd      结束点
     * @return
     */
    float rate=3f;
    private Point getControPoint(List<Point> localList, int index, Point pStart, Point pEnd) {
        Point control = new Point();
        int xhalf = (pStart.getX() + pEnd.getX()) / 2;
        int yhalf = (pStart.getY() + pEnd.getY()) / 2;
        int x = localList.get(index).getxPercent();
        if (index == 0 || index == 3) {
            control.setY((int) (yhalf + 10*rate));
            control.setX((int) (xhalf + 10*rate));
        } else {
            if (index == 1) {
                control.setX((int) (xhalf + 10*rate));
            }
            if (index == 2) {
                control.setX((int) (xhalf - 10*rate));
            }
            control.setY((int) (yhalf - 40*rate));
        }

        return control;
    }


    private void drawDesText(Canvas canvas) {
        for (int i = 0; i < localList.size(); i++) {
            Point point = localList.get(i);
            if (!point.isShowDesc()) {
                continue;
            }
            String text = point.getDesc();
            mDescPaint.setColor(point.getDescColor());
            canvas.drawText(text, point.getX() - getTextWidth(mDescPaint, text) / 2,
                    point.getY() + getTextHeight(mDescPaint, text) + 20, mDescPaint);

        }
    }

    public static int getTextWidth(Paint paint, String text) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.width();
    }

    public static int getTextHeight(Paint paint, String text) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }


    public int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    private void drawCurve(Canvas canvas) {
        paintCurve.setColor(getResources().getColor(R.color.lineColor));
        if (currentType == TYPE_ONE) {
            drawDesText(canvas);//画描述文字
            paintCurve.setStyle(Paint.Style.STROKE);
            paintCurve.setDither(true);
            paintCurve.setStrokeWidth(10);
            paintCurve.setColor(ContextCompat.getColor(mContext, R.color.white));
            for (int i = 0; i <= (localList.size() - 1); i++) {
                Path path = new Path();
                if (localList.size() > i + 1) {
                    Point pStart = localList.get(i);
                    Point pEnd = localList.get(i + 1);
                    Point point5 = getControPoint(localList, i, pStart, pEnd);
                    paintCurve.setColor(pStart.getLineColor());
                    path.moveTo(pStart.getX(), pStart.getY());
                    path.quadTo(point5.getX(), point5.getY(), pEnd.getX(),
                            pEnd.getY());
                    canvas.drawPath(path, paintCurve);
                }
            }
        }
        if (currentType == TYPE_TWO) {
            paintCurve.setStyle(Paint.Style.FILL);
            float length = mBoatPathMeasure.getLength();
            mBoatPathMeasure.getMatrix(length * currentValue,
                    mMatrix,
                    PathMeasure.POSITION_MATRIX_FLAG | PathMeasure.TANGENT_MATRIX_FLAG);
            mMatrix.preTranslate(-mBoatBitmap.getWidth() / 2, -mBoatBitmap.getHeight() * 5 / 6);
            canvas.drawBitmap(mBoatBitmap, mMatrix, null);
            canvas.save();
            canvas.translate(-mCurWaveOffset, 0);
            paintCurve.setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            canvas.drawPath(wavePath, paintCurve);
            canvas.restore();

            canvas.save();
            canvas.translate(-mPath2Offset, 0);
            paintCurve.setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            canvas.drawPath(wavePath2, paintCurve);
            canvas.restore();
        }
    }

    private void initPath(Path path, int length, int pathHeight, float lengthTime, boolean close) {
        path.moveTo(-length, height / 2);
        for (int i = -length; i < width * lengthTime + length; i += length) {
            // rQuadTo 和 quadTo 区别在于
            // rQuadTo 是相对上一个点 而 quadTo是相对于画布
            path.rQuadTo(length / 4,
                    -pathHeight,
                    length / 2,
                    0);
            path.rQuadTo(length / 4,
                    pathHeight,
                    length / 2,
                    0);
        }
        if (close) {
            path.rLineTo(0, height / 2);
            path.rLineTo(-(width * 2 + 2 * length), 0);
            path.close();
        }

    }


    /**
     * 初始化XY 的值，代表水平方向上的值，y代表竖直方向上的百分比
     */
    private void initXY() {
        localList.clear();
        List<Float> yPercent = new ArrayList<>();
        List<Integer> xPercent = new ArrayList<>();
        if (currentType == TYPE_ONE) {
            yPercent.add(23f);
            yPercent.add(26f);
            yPercent.add(48f);
            yPercent.add(23f);
            yPercent.add(21f);

            xPercent.add(0);
            xPercent.add(15);
            xPercent.add(50);
            xPercent.add(85);
            xPercent.add(100);
        }
        if (currentType == TYPE_TWO) {
            for (int i = 0; i < 10; i++) {
                if (i % 2 == 0) {
                    yPercent.add(40f);
                } else {
                    yPercent.add(50f);
                }
                xPercent.add(i * 10);
            }
        }

        List<Point> points = new ArrayList<>();
        for (int i = 0; i < xPercent.size(); i++) {
            Point point = new Point();
            point.setxPercent(xPercent.get(i));
            point.setyPercent(yPercent.get(i));
            int pointX = (int) (point.getxPercent() / 100f * width + mBrokenLineLeft);
            int pointY = (int) (
                    (100 - point.getyPercent()) / 100f * height + mBrokenLineTop
                            - mBrokenLineBottom);
            point.setX(pointX);
            point.setY(pointY);
            if (i == 1) {
                point.setDesc("低于" + yPercent.get(i) + "%");
                point.setDescColor(getResources().getColor(R.color.lineColor));
                point.setShowDesc(true);
            }
            if (i == 2 || i == 3) {
                point.setDesc("超过" + yPercent.get(i) + "%");
                point.setDescColor(getResources().getColor(R.color.lineColor2));
                point.setShowDesc(true);
            }
            if (i == 3) {
                point.setServer(true);
            }
            if (i == 0 || i == 3) {
                point.setLineColor(getResources().getColor(R.color.lineColor));
            } else {
                point.setLineColor(getResources().getColor(R.color.lineColor2));
            }
            points.add(point);
        }
        localList = points;
    }

    public void startAnim() {
        mAnimator.start();
    }

    public void stopAnim() {
        mAnimator.cancel();
    }

    public void switchType() {
        if (currentType == TYPE_ONE) {
            currentType =TYPE_TWO;
        }else{
            currentType =TYPE_ONE;
        }
        requestLayout();
    }

}
