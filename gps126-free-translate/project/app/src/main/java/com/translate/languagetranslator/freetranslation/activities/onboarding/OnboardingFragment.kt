package com.translate.languagetranslator.freetranslation.activities.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.translate.languagetranslator.freetranslation.R

class OnboardingFragment : Fragment() {

    companion object {

        const val stepParam = "STEP"

    }

    lateinit var imageView: ImageView
    lateinit var tvTitle: TextView
    lateinit var tvMessage: TextView
    lateinit var btnContinue: Button

    private var step: Int? = null

    var onClick: ((Int) -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        step = arguments?.getInt(stepParam)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_onboarding, container, false).also {

        imageView = it.findViewById(R.id.image_view)
        tvTitle = it.findViewById(R.id.tv_title)
        tvMessage = it.findViewById(R.id.tv_message)
        btnContinue = it.findViewById(R.id.btn_continue)
        btnContinue.setOnClickListener {

            onClick?.invoke(step ?: 0)
        }
        setStep(step ?: 0)
    }

    fun setStep(currentStep: Int) {

       tvTitle.setText(when (currentStep) {

            0 -> R.string.step1_title
            1 -> R.string.step2_title
            2 -> R.string.step3_title
            else -> R.string.step1_title
        })

        tvMessage.setText(when (currentStep) {

            0 -> R.string.step1_message
            1 -> R.string.step2_message
            2 -> R.string.step3_message
            else -> R.string.step1_message
        })

        imageView.setImageDrawable(when(currentStep){
            0 -> ContextCompat.getDrawable(requireContext(),R.drawable.onboard_translate)
            1 -> ContextCompat.getDrawable(requireContext(),R.drawable.onboard_fictional_language)
            2 -> ContextCompat.getDrawable(requireContext(),R.drawable.onboard_lost_country)
            else -> ContextCompat.getDrawable(requireContext(),R.drawable.onboard_translate)
        })



    }

    private fun boolToVisibility(b: Boolean) = if (b) {

        View.VISIBLE
    } else {

        View.GONE
    }
}