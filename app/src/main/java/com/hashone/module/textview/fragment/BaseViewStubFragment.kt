package com.hashone.module.textview.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import com.hashone.module.textview.R
import com.hashone.module.textview.base.BaseFragment
import com.hashone.module.textview.databinding.FragmentViewstubBinding

abstract class BaseViewStubFragment : BaseFragment() {

    private lateinit var binding: FragmentViewstubBinding

    private var mSavedInstanceState: Bundle? = null
    private var hasInflated = false
    private var mViewStub: ViewStub? = null
    private var visible = false

    var showProgress: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_viewstub, container, false)
        binding = FragmentViewstubBinding.bind(view)
        mViewStub = binding.fragmentViewStub
        mViewStub!!.layoutResource = getViewStubLayoutResource()
        mSavedInstanceState = savedInstanceState

        if (visible && !hasInflated) {
            binding.inflateProgressbar.visibility = if (showProgress) View.VISIBLE else View.GONE
            val inflatedView = mViewStub!!.inflate()
            onCreateViewAfterViewStubInflated(inflatedView, mSavedInstanceState)
            afterViewStubInflated(view)
        }

        return binding.root
    }

    protected abstract fun onCreateViewAfterViewStubInflated(
        inflatedView: View, savedInstanceState: Bundle?
    )

    /**
     * The layout ID associated with this ViewStub
     * @see ViewStub.setLayoutResource
     * @return
     */
    @LayoutRes
    protected abstract fun getViewStubLayoutResource(): Int

    /**
     *
     * @param originalViewContainerWithViewStub
     */
    @CallSuper
    protected fun afterViewStubInflated(originalViewContainerWithViewStub: View?) {
        hasInflated = true
        if (originalViewContainerWithViewStub != null) {
            binding.inflateProgressbar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        mActivity.runOnUiThread {
            visible = true
            if (mViewStub != null && !hasInflated) {
                val inflatedView = mViewStub!!.inflate()
                binding.inflateProgressbar.visibility =
                    if (showProgress) View.VISIBLE else View.GONE
                onCreateViewAfterViewStubInflated(inflatedView, mSavedInstanceState)
                afterViewStubInflated(binding.root)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hasInflated = false
    }

    override fun onPause() {
        super.onPause()
        visible = false
    }

    // Thanks to Noa Drach, this will fix the orientation change problem
    override fun onDetach() {
        super.onDetach()
        hasInflated = false
    }
}