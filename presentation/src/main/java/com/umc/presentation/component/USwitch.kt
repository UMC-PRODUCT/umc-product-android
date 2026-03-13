package com.umc.presentation.component

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.umc.presentation.R
import com.umc.presentation.databinding.CustomSwitchBinding

class USwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    private val binding = CustomSwitchBinding.inflate(LayoutInflater.from(context), this, true)
    private val colorEvaluator = ArgbEvaluator()

    private var backgroundColorOn: Int = 0
    private var backgroundColorOff: Int = 0
    private var thumbColorOn: Int = 0
    private var thumbColorOff: Int = 0

    /**
     * 스위치 관련 상수
     */
    private object Constants {
        const val THUMB_SIZE_ON = 22f
        const val THUMB_SIZE_OFF = 16f
        const val TRACK_WIDTH = 52f
        const val TRACK_HEIGHT = 32f
        const val STROKE_WIDTH = 2f
        const val ANIMATION_DURATION = 200L
    }

    // DP를 PX로 변환
    private val thumbSizeOnPx by lazy { dpToPx(Constants.THUMB_SIZE_ON) }
    private val thumbSizeOffPx by lazy { dpToPx(Constants.THUMB_SIZE_OFF) }
    private val trackWidthPx by lazy { dpToPx(Constants.TRACK_WIDTH) }
    private val strokeWidthPx by lazy { dpToPx(Constants.STROKE_WIDTH) }

    // Thumb의 마진 계산 (Track 중앙 정렬용)
    private val marginOff by lazy { dpToPx((Constants.TRACK_HEIGHT - Constants.THUMB_SIZE_OFF) / 2f) }
    private val marginOn by lazy { dpToPx((Constants.TRACK_HEIGHT - Constants.THUMB_SIZE_ON) / 2f) }

    /**
     * 스위치의 체크 상태
     */
    var isChecked: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                animateSwitch(value)
                onCheckedChangeListener?.onCheckedChanged(this, value)
            }
        }

    private var onCheckedChangeListener: OnCheckedChangeListener? = null

    init {
        setupAttributes(attrs, defStyle)
        // 초기 뷰 상태 업데이트
        updateVisuals()
    }

    /**
     * XML에서 정의한 커스텀 속성 초기화
     */
    private fun setupAttributes(attrs: AttributeSet?, defStyle: Int) {
        isClickable = true
        isFocusable = true

        val a = context.obtainStyledAttributes(attrs, R.styleable.USwitch, defStyle, 0)
        try {
            backgroundColorOn = a.getColor(
                R.styleable.USwitch_backgroundColorOn,
                ContextCompat.getColor(context, R.color.primary500)
            )
            backgroundColorOff = a.getColor(
                R.styleable.USwitch_backgroundColorOff,
                ContextCompat.getColor(context, R.color.neutral100)
            )
            thumbColorOn = a.getColor(
                R.styleable.USwitch_thumbColorOn,
                ContextCompat.getColor(context, R.color.neutral000)
            )
            thumbColorOff = a.getColor(
                R.styleable.USwitch_thumbColorOff,
                ContextCompat.getColor(context, R.color.neutral300)
            )
            // 초기값 설정 시 애니메이션을 피하기 위해 필드에 직접 접근
            isChecked = a.getBoolean(R.styleable.USwitch_android_checked, false)
        } finally {
            a.recycle()
        }

        // 클릭 시 상태 반전
        setOnClickListener { isChecked = !isChecked }
    }

    /**
     * On/Off 상태 전환 시 크기, 위치, 색상, 스트로크 변화 애니메이션
     */
    private fun animateSwitch(checked: Boolean) {
        // 크기 변화
        val (startSize, endSize) = if (checked) {
            thumbSizeOffPx to thumbSizeOnPx
        } else {
            thumbSizeOnPx to thumbSizeOffPx
        }

        // 위치 변화
        val maxTranslation = calculateMaxTranslation()
        val (startTranslation, endTranslation) = if (checked) {
            marginOff to maxTranslation
        } else {
            maxTranslation to marginOff
        }

        // 테두리 변화
        val (startStroke, endStroke) = if (checked) {
            strokeWidthPx to 0f
        } else {
            0f to strokeWidthPx
        }

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = Constants.ANIMATION_DURATION
            addUpdateListener { animator ->
                val fraction = animator.animatedValue as Float
                updateThumbSize(startSize, endSize, fraction)
                updateThumbPosition(startTranslation, endTranslation, fraction)
                updateStroke(startStroke, endStroke, fraction)
                updateColors(checked, fraction)
            }
            start()
        }
    }

    /**
     * 애니메이션 진행도에 따라 Thumb의 크기를 변경
     */
    private fun updateThumbSize(startSize: Float, endSize: Float, fraction: Float) {
        val currentSize = (startSize + (endSize - startSize) * fraction).toInt()
        binding.thumb.layoutParams = binding.thumb.layoutParams.apply {
            width = currentSize
            height = currentSize
        }
        binding.thumb.radius = currentSize / 2f
    }

    /**
     * 애니메이션 진행도에 따라 Thumb의 X 좌표를 변경
     */
    private fun updateThumbPosition(start: Float, end: Float, fraction: Float) {
        binding.thumb.translationX = start + (end - start) * fraction
    }

    /**
     * 애니메이션 진행도에 따라 Track의 테두리 두께를 변경
     */
    private fun updateStroke(start: Float, end: Float, fraction: Float) {
        val currentStroke = (start + (end - start) * fraction).toInt()
        binding.track.strokeWidth = currentStroke
        binding.track.strokeColor = thumbColorOff
    }

    /**
     * 애니메이션 진행도에 따라 Track과 Thumb의 색상 변경
     */
    private fun updateColors(checked: Boolean, fraction: Float) {
        val trackColor = colorEvaluator.evaluate(
            fraction,
            if (checked) backgroundColorOff else backgroundColorOn,
            if (checked) backgroundColorOn else backgroundColorOff
        ) as Int

        val thumbColor = colorEvaluator.evaluate(
            fraction,
            if (checked) thumbColorOff else thumbColorOn,
            if (checked) thumbColorOn else thumbColorOff
        ) as Int

        binding.track.setCardBackgroundColor(trackColor)
        binding.thumb.setCardBackgroundColor(thumbColor)
    }

    /**
     * Thumb이 이동할 수 있는 최대 X 좌표를 계산
     */
    private fun calculateMaxTranslation(): Float {
        return trackWidthPx - thumbSizeOnPx - marginOn
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    /**
     * 애니메이션 없이 현재 [isChecked] 상태에 맞게 뷰의 외형을 즉시 업데이트
     */
    private fun updateVisuals() {
        val thumbSize = if (isChecked) thumbSizeOnPx else thumbSizeOffPx

        binding.thumb.layoutParams = binding.thumb.layoutParams.apply {
            width = thumbSize.toInt()
            height = thumbSize.toInt()
        }
        binding.thumb.radius = thumbSize / 2f
        binding.thumb.translationX = if (isChecked) calculateMaxTranslation() else marginOff

        binding.track.setCardBackgroundColor(
            if (isChecked) backgroundColorOn else backgroundColorOff
        )
        binding.thumb.setCardBackgroundColor(
            if (isChecked) thumbColorOn else thumbColorOff
        )

        // 초기 상태가 Off라면 테두리 설정
        binding.track.strokeWidth = if (isChecked) 0 else strokeWidthPx.toInt()
        binding.track.strokeColor = thumbColorOff
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        onCheckedChangeListener = listener
    }

    fun interface OnCheckedChangeListener {
        fun onCheckedChanged(view: USwitch, isChecked: Boolean)
    }

    /**
     * Data Binding 및 Two-way Binding 지원을 위한 어댑터 설정
     */
    companion object {
        @JvmStatic
        @BindingAdapter("android:checked")
        fun setChecked(view: USwitch, checked: Boolean) {
            // 무한 루프 방지를 위해 값이 다를 때만 변경
            if (view.isChecked != checked) {
                view.isChecked = checked
            }
        }

        @JvmStatic
        @InverseBindingAdapter(attribute = "android:checked")
        fun isChecked(view: USwitch): Boolean = view.isChecked

        @JvmStatic
        @BindingAdapter("android:checkedAttrChanged")
        fun setOnCheckedChangeListener(view: USwitch, listener: InverseBindingListener?) {
            view.setOnCheckedChangeListener { _, _ ->
                // 뷰의 상태가 바뀌면 바인딩된 데이터에 알림
                listener?.onChange()
            }
        }
    }
}