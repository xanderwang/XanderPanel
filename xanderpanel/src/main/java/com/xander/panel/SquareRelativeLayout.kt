package com.xander.panel

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.RelativeLayout

/**
 * Created by wangxiaoyang on 16-9-9.
 */
class SquareRelativeLayout : RelativeLayout {

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec))
        val childWidthSize = measuredWidth
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY)
        val maxSize = resources.displayMetrics.widthPixels / 3
        val childHeightSize = Math.min(maxSize, childWidthSize)
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize, MeasureSpec.EXACTLY)
        Log.d("wxy", "size $widthMeasureSpec,$heightMeasureSpec")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}