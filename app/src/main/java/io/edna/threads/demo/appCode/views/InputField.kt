package io.edna.threads.demo.appCode.views

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.textfield.TextInputEditText
import io.edna.threads.demo.R
import io.edna.threads.demo.R.styleable.InputField

class InputField : FrameLayout, View.OnFocusChangeListener, TextWatcher {

    private var isInFocus: Boolean = false
    private var textInputField: TextInputEditText
    private var hintText: AppCompatTextView
    private var commonLayout: FrameLayout

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attr,
        defStyleAttr
    ) {
        attr?.let { att ->
            val attributes = context.obtainStyledAttributes(att, InputField)
            attributes.getString(R.styleable.InputField_hint)?.let {
                hint = it
            }
            attributes.getString(R.styleable.InputField_text)?.let {
                text = it
            }
            updateHintView()
            onFocusChange(this, isInFocus)
            attributes.recycle()
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.input_field, this, true)
        textInputField = findViewById(R.id.textInputField)
        hintText = findViewById(R.id.hintText)
        commonLayout = findViewById(R.id.commonLayout)
        textInputField.onFocusChangeListener = this
        textInputField.addTextChangedListener(this)
        commonLayout.setOnClickListener {
            textInputField.requestFocus()
            showKeyboard(textInputField)
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        isInFocus = hasFocus
        updateHintView()
    }

    /** Переносит текст подсказски в центр, на место поля ввода */
    private fun hintToCenter() {
        val params: LayoutParams = hintText.layoutParams as LayoutParams
        params.apply {
            gravity = Gravity.CENTER and Gravity.CENTER_VERTICAL
        }
        hintText.layoutParams = params
        hintText.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.text_size_16)
        )
        visibility = View.VISIBLE
        if (!TextUtils.isEmpty(hint)) {
            hintText.text = hint
        }
    }

    /**
     * Переносит текст подсказски в верх, когда в поле ввода есть какой-то
     * текст
     */
    private fun hintToTop() {
        val params: LayoutParams = hintText.layoutParams as LayoutParams
        params.apply {
            gravity = Gravity.TOP
        }
        hintText.layoutParams = params
        hintText.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.text_size_12)
        )
        visibility = View.VISIBLE
    }

    private fun updateHintView() {
        if (!TextUtils.isEmpty(hint)) {
            if (!TextUtils.isEmpty(text) || isInFocus) {
                hintToTop()
            } else {
                hintToCenter()
            }
        }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (TextUtils.isEmpty(textInputField.text) && !isInFocus) {
            hintToCenter()
        } else {
            hintToTop()
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable?) {}

    private fun showKeyboard(view: View) {
        view.requestFocus()
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, 0)
        view.requestFocus()
    }

    var hint: String?
        get() = hintText.text.toString()
        set(value) {
            hintText.text = value
        }

    var text: String?
        get() = textInputField.text.toString()
        set(value) = textInputField.setText(value)

    fun setTextChangedListener(listener: TextWatcher) {
        textInputField.addTextChangedListener(listener)
    }
}
