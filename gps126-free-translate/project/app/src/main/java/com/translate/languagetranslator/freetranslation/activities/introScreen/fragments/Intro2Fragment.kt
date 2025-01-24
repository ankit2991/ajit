package com.translate.languagetranslator.freetranslation.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.translate.languagetranslator.freetranslation.R

class Intro2Fragment : Fragment(){

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        var v=inflater.inflate(R.layout.intro2, container, false)


        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }





    companion object {
        var TAG = "intro1Frag"
       fun getInst():Fragment{
           return Intro2Fragment()
       }
    }
}
