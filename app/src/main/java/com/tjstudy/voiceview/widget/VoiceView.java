package com.tjstudy.voiceview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.tjstudy.voiceview.R;

/**
 * Created by tjstudy on 2017/3/6.
 */

public class VoiceView extends View {

    private float beginDegree;
    private float endDegree;
    private float mLeftDegree;
    private float partDegree;
    private float mGapDegree;
    private int mPartNums;
    private Paint mPaint;
    private int height;
    private int width;
    private int mCircleWidth;
    private int mCurrentPart;
    private Context mContext;
    private TypedArray typedArray;
    private int mIconVoice;

    public VoiceView(Context context) {
        this(context, null);
    }

    public VoiceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoiceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.VoiceView, defStyleAttr, 0);

        getData();
        initPaint();
    }

    /**
     * 获取属性数据
     */
    private void getData() {
        //间隔度数
        mGapDegree = typedArray.getFloat(R.styleable.VoiceView_gapDegree, 10);
        //遗留下多少度数
        mLeftDegree = typedArray.getFloat(R.styleable.VoiceView_leftDegree, 60);
        //块的个数
        mPartNums = typedArray.getInteger(R.styleable.VoiceView_partNums, 10);
        //块的宽度
        mCircleWidth = typedArray.getInteger(R.styleable.VoiceView_circleWidth, 20);
        //默认已经显示的块数
        mCurrentPart = typedArray.getInteger(R.styleable.VoiceView_defaultPartNum, 1);
        //显示音量图片
        mIconVoice = typedArray.getInteger(R.styleable.VoiceView_iconVoice, R.mipmap.ic_launcher);

        typedArray.recycle();

        setStartDegree();
        setPartDegree();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setStrokeWidth(mCircleWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST) {
            width = 200;
        } else {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST) {
            height = 200;
        } else {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(width / 2, height / 2);
        //范围内的rect
        RectF rectF = new RectF(-width / 2 + mCircleWidth, -height / 2 + mCircleWidth,
                width / 2 - mCircleWidth, height / 2 - mCircleWidth);
        //1、画背景块
        mPaint.setColor(Color.GRAY);
        float bgStartDegree = beginDegree;
        for (int i = 0; i < mPartNums; i++) {
            canvas.drawArc(rectF, bgStartDegree, partDegree, false, mPaint);
            bgStartDegree += mGapDegree + partDegree;
        }
        //2、画前景块
        mPaint.setColor(Color.BLUE);
        float fStartDegree = beginDegree;
        for (int i = 0; i < mCurrentPart; i++) {
            canvas.drawArc(rectF, fStartDegree, partDegree, false, mPaint);
            fStartDegree += mGapDegree + partDegree;
        }
        //3、画图片
        //计算图片开始位置
        RectF picRect = new RectF();
        float innerRadius = (width / 2 - mCircleWidth) / 2;
        picRect.left = -(float) (innerRadius * Math.sqrt(2) / 2);
        picRect.top = picRect.left;
        picRect.right = -picRect.left;
        picRect.bottom = -picRect.left;

        //处理图片
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), mIconVoice);
        picRect.left = picRect.left > -bitmap.getWidth() / 2 ? picRect.left : -bitmap.getWidth() / 2;
        picRect.top = picRect.top > -bitmap.getHeight() ? picRect.top : -bitmap.getHeight() / 2;
        picRect.right = -picRect.left;
        picRect.bottom = -picRect.top;
        canvas.drawBitmap(bitmap, null, picRect, mPaint);
    }

    /**
     * 计算开始角度和结束角度
     */
    private void setStartDegree() {
        beginDegree = 90 + mLeftDegree / 2;
        endDegree = 360 + (90 - mLeftDegree / 2);
    }

    /**
     * 计算每一块的角度
     */
    private void setPartDegree() {
        float useDegree = 360 - mLeftDegree;
        float usePartDegree = useDegree - mGapDegree * (mPartNums - 1);
        partDegree = usePartDegree / mPartNums;
    }

    /**
     * 添加块
     */
    private void addPart() {
        if (mCurrentPart == mPartNums) {
            Toast.makeText(mContext, "音量最大了", Toast.LENGTH_SHORT).show();
            return;
        }
        mCurrentPart++;
        postInvalidate();
    }

    /**
     * 减少块
     */
    private void subPart() {
        if (mCurrentPart == 0) {
            Toast.makeText(mContext, "音量最小了", Toast.LENGTH_SHORT).show();
            return;
        }
        mCurrentPart--;
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //怎样的触摸为加 怎样为减
        //触摸 手指抬起的位置 在原点的左边为减 右边为加
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP://手指抬起的时候
                float x = event.getX();//这是相对于屏幕的坐标
                if (x < width / 2) {
                    subPart();
                } else {
                    addPart();
                }
                break;
        }
        return true;
    }
}
