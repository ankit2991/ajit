package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.MainActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.adapters.LocalizationAdapter
import com.gpsnavigation.maps.gpsroutefinder.routemap.callbacks.OnLanguageClicked
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.FragmentRouteListBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.LanguageDialogeBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.LocaleLanguageModel


class LanguageDialoge : AppCompatDialogFragment(){
    lateinit var binding: LanguageDialogeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return inflater.inflate(R.layout.language_dialoge, container)
        binding = LanguageDialogeBinding.inflate(inflater, container, false)
        return binding.root
    }

    var selectedPosition = 0
    var selectedLangCode = "en"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val appLangList = binding.appLangList
        val langList = ArrayList<LocaleLanguageModel>()

        context?.let {
            val language = LocaleHelper1.getLanguage(it)

            if (language != null) {
                selectedLangCode = language
            }else{
                selectedLangCode = "en"
            }
        }
        val recyclerLayoutManager = LinearLayoutManager(requireContext())
        appLangList.layoutManager = recyclerLayoutManager

        val dividerItemDecoration = DividerItemDecoration(appLangList.context, recyclerLayoutManager.orientation)
        appLangList.addItemDecoration(dividerItemDecoration)

        val appLanguages = resources.getStringArray(R.array.appLanguages)
        val appLangCode = resources.getStringArray(R.array.appLangCode)

        for (i in appLanguages.indices) {
            if (selectedLangCode.equals(appLangCode[i], ignoreCase = true))
                selectedPosition = i

            val languageModel = LocaleLanguageModel(
                appLanguages[i],
                appLangCode[i],
                "online")
            langList.add(languageModel)
        }
        val localizationAdapter =
            LocalizationAdapter(
                langList,
                selectedPosition
            )
        appLangList.adapter = localizationAdapter
        localizationAdapter.setOnLanguageSelected(OnLanguageClicked {
            selectedLangCode = it.code
            dismiss()
            LocaleHelper1.setAppLocale(requireContext(), selectedLangCode)
//            LocaleHelper.setLocale(requireContext(), selectedLangCode)
//            LocaleHelper1.setAppLocale(requireContext(), selectedLangCode)

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                LocaleHelper.setLocale(requireContext(), selectedLangCode)
//            }else{
//                LocaleHelper1.setAppLocale(requireContext(), selectedLangCode)
//            }

            startActivity(Intent(requireContext(), MainActivity::class.java))
            activity?.finish()
        })

//        inits()
        super.onViewCreated(view, savedInstanceState)
    }

//    private fun inits() {
//        btnClose.setOnClickListener { dismiss() }

//        tvOkay.setOnClickListener {
//            dismiss()
//            LocaleHelper1.setAppLocale(requireContext(), selectedLangCode)
////            LocaleHelper.setLocale(requireContext(), selectedLangCode)
////            LocaleHelper1.setAppLocale(requireContext(), selectedLangCode)
//
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
////                LocaleHelper.setLocale(requireContext(), selectedLangCode)
////            }else{
////                LocaleHelper1.setAppLocale(requireContext(), selectedLangCode)
////            }
//
//            startActivity(Intent(requireContext(), MainActivity::class.java))
//            activity?.finish()
//
//        }
//        tvCancel.setOnClickListener { dismiss() }
//    }

    override fun onStart() {
        super.onStart()
        //dialog.setTitle("Live Track Duration")
        val displayMetrics = DisplayMetrics()
        (context as (Activity)).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        dialog?.window?.setLayout((width * .85).toInt(), (height * .60).toInt())
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        return dialog
    }

    lateinit var contxt: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.contxt = context
    }

    companion object {
        fun newInstance(): LanguageDialoge {
            val args = Bundle()
            val fragment = LanguageDialoge()
            fragment.arguments = args
            return fragment
        }
    }
}