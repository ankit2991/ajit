package com.gpsnavigation.maps.gpsroutefinder.routemap.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.FragmentTutorialFourthBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.systemBarsInsets

class TutorialFourthFragment : Fragment() {
    private lateinit var binding: FragmentTutorialFourthBinding

    private var callback: TutorialFragment.OnClickListener? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callback = context as TutorialFragment.OnClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnSkipClickListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTutorialFourthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setInset()
    }

    private fun initView() = with(binding) {

        pin1.setOnClickListener {
            callback?.onPinImageClick(R.id.pin1)
        }

        pin2.setOnClickListener {
            callback?.onPinImageClick(R.id.pin2)
        }

        pin3.setOnClickListener {
            callback?.onPinImageClick(R.id.pin3)
        }

        pin4.setOnClickListener {
            callback?.onPinImageClick(R.id.pin4)
        }

        if (!TinyDB.getInstance(requireContext()).isPremium) {
            binding.nativeAdsContainer.isVisible = true
            MaxAdManager.showNativeAds(
                requireContext(),
                binding.nativeAdsContainer,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun setInset() {
        binding.nativeAdsContainer.setOnApplyWindowInsetsListener { view, insets ->
            val systemBarsInsets = systemBarsInsets(insets)
            (view.layoutParams as ConstraintLayout.LayoutParams).apply {
                bottomMargin = systemBarsInsets.bottom
            }

            insets
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = TutorialFourthFragment()
    }
}