package com.faltenreich.inputhintlayout

import android.animation.Animator
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.EditText
import android.widget.FrameLayout

/**
 * Created by Faltenreich on 21.01.2018
 */

private const val ANIMATION_DURATION_DEFAULT = 300L

@Suppress("MemberVisibilityCanBePrivate")
class InputHintLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private enum class Status {
        IDLE,
        ANIMATING_IN,
        ANIMATING_OUT
    }

    private var customAnimationDuration: Long = 0
    private var customTextSize: Float = 0f
    private var customTextColorNormal: Int = 0
    private var customTextColorSelected: Int = 0

    private var currentStatus: Status = Status.IDLE
    private var pendingStatus: Status = Status.IDLE

    private var maxLineCount: Int = 1

    private val editText: EditText by lazy { views().first { it is EditText } as EditText }

    private val hintView: InputHintView by lazy {
        val hintView = InputHintView(context)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        hintView.layoutParams = layoutParams
        addView(hintView)
        hintView
    }

    private var maxLineWidth: Int = 0
        get() = editText.width - hintView.width - hintView.paddingLeft - hintView.paddingRight

    private var textColor: Int
        get() = hintView.textColors.defaultColor
        set(value) { hintView.setTextColor(value, animationDurationMillis, interpolator) }

    var animationDurationMillis: Long = ANIMATION_DURATION_DEFAULT

    var interpolator: Interpolator = AccelerateDecelerateInterpolator()

    var textSize: Float
        get() = hintView.textSize
        set(value) { hintView.setTextSize(TypedValue.COMPLEX_UNIT_PX, editText.textSize) }

    var textColorNormal: Int = -1
        set(value) {
            field = value
            invalidateHintColor()
        }

    var textColorSelected: Int = -1
        set(value) {
            field = value
            invalidateHintColor()
        }

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.InputHintLayout, 0, 0)
            customAnimationDuration = typedArray.getInt(R.styleable.InputHintLayout_animationDurationMillis, -1).toLong()
            customTextSize = typedArray.getFloat(R.styleable.InputHintLayout_android_textSize, -1f)
            customTextColorNormal = typedArray.getColorStateList(R.styleable.InputHintLayout_android_textColor)?.defaultColor ?: -1
            customTextColorSelected = typedArray.getColorStateList(R.styleable.InputHintLayout_android_tint)?.defaultColor ?: -1
            typedArray.recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initLayout()
    }

    private fun initLayout() {
        editText.gravity = Gravity.TOP
        maxLineCount = editText.getMaxLineCountCompat()

        hintView.setPadding(editText.paddingLeft, editText.paddingTop, editText.paddingRight, editText.paddingBottom)
        hintView.setLayoutGravity(Gravity.BOTTOM or Gravity.END) // TODO: Rtl
        hintView.text = editText.hint

        animationDurationMillis = if (customAnimationDuration >= 0) customAnimationDuration else ANIMATION_DURATION_DEFAULT
        textSize = if (customTextSize >= 0) customTextSize else editText.textSize
        textColorNormal = if (customTextColorNormal >= 0) customTextColorNormal else editText.hintTextColors.defaultColor
        textColorSelected = if (customTextColorSelected >= 0) customTextColorSelected else context.accentColor()

        // TODO: Transition

        if (!isInEditMode) {
            editText.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun onTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {
                    invalidateHintPosition()

                    val firstWord = start == 0
                    val inBounds = text?.length ?: 0 <= 1
                    val emptied = count == 1 && after == 0
                    val unemptied = count == 0 && after == 1
                    if (firstWord && inBounds && (emptied || unemptied)) {
                        toggleHintVisibility(after == 1)
                    }
                }
                override fun afterTextChanged(editable: Editable?) {}
            })

            editText.onFocusChangeListener = OnFocusChangeListener { _, _ -> invalidateHintColor() }

            val showHint = editText.text.isNotEmpty()
            hintView.alpha = if (showHint) 1f else 0f
        }
    }

    private fun invalidateHintColor() {
        textColor = if (editText.hasFocus()) textColorSelected else textColorNormal
    }

    private fun invalidateHintPosition() {
        // Ensure correct line count on shrinking text
        if (editText.lineCount <= maxLineCount) {
            editText.setLines(editText.lineCount)
        }

        val currentLineWidth = editText.getTextWidth(editText.lineCount - 1)
        val exceededLineWidth = currentLineWidth > maxLineWidth
        if (exceededLineWidth) {
            val reachedEnd = editText.lineCount >= maxLineCount
            if (reachedEnd) {
                // TODO: Ellipsize early
            } else {
                editText.setLines(editText.lineCount + 1)
            }
        }
    }

    private fun toggleHintVisibility(isVisible: Boolean) {
        pendingStatus = Status.IDLE
        when (currentStatus) {
            Status.IDLE -> animateHintVisibility(isVisible)
            Status.ANIMATING_IN -> if (!isVisible) pendingStatus = Status.ANIMATING_OUT
            Status.ANIMATING_OUT -> { if (isVisible) pendingStatus = Status.ANIMATING_IN }
        }
    }

    private fun animateHintVisibility(animateIn: Boolean) {
        val animator = if (animateIn) inAnimation(hintView) else outAnimation(hintView)
        animator.duration = animationDurationMillis
        animator.interpolator = interpolator
        animator.setListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator?) {
                currentStatus = if (animateIn) Status.ANIMATING_IN else Status.ANIMATING_OUT
                Log.d(tag(), "Started animation $currentStatus")
            }
            override fun onAnimationEnd(animator: Animator?) {
                Log.d(tag(), "Finished animation $currentStatus")
                currentStatus = Status.IDLE
                when (pendingStatus) {
                    Status.ANIMATING_IN -> toggleHintVisibility(true)
                    Status.ANIMATING_OUT -> toggleHintVisibility(false)
                    else -> Unit
                }
            }
            override fun onAnimationCancel(animator: Animator?) {
                currentStatus = Status.IDLE
            }
            override fun onAnimationRepeat(animator: Animator?) {
                currentStatus = if (animateIn) Status.ANIMATING_IN else Status.ANIMATING_OUT
            }
        })
        animator.start()
    }

    fun inAnimation(view: View): ViewPropertyAnimator = view.animate().alpha(1f)

    fun outAnimation(view: View): ViewPropertyAnimator = view.animate().alpha(0f)
}