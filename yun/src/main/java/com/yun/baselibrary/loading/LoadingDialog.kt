package com.yun.baselibrary.loading

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.yun.baselibrary.R
import kotlinx.android.synthetic.main.dialog_loading.*

/**
 * loading dialog
 */
class LoadingDialog : DialogFragment() {


    private var rootView: View? = null

    override fun onStart() {
        super.onStart()
        val width = context?.resources?.displayMetrics?.widthPixels!!.toFloat()
        val window = dialog!!.window
        val lp = window!!.attributes
        lp.gravity = Gravity.CENTER
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = lp
        window.setBackgroundDrawable(ColorDrawable(0x00000000))

        tvLoading.text = arguments?.getString("text",getString(R.string.str_loading)) ?: getString(R.string.str_loading)
    }

    fun setTvLoaddingText(text: String){
        tvLoading.text = text
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.dialog_loading, container, false)
        }
        return rootView
    }
}