package com.gpsnavigation.maps.gpsroutefinder.routemap.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.FragmentFirstScreenBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.FragmentSecondScreenBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.systemBarsInsets


/**
 * A simple [Fragment] subclass.
 */
class FirstScreenFragment : Fragment() {
    private lateinit var binding: FragmentFirstScreenBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFirstScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInset()
    }

    private fun setInset() {
        binding.tvTitle.setOnApplyWindowInsetsListener { view, insets ->
            val systemBarsInsets = systemBarsInsets(insets)
            view.updatePadding(
                top = systemBarsInsets.top
            )

            insets
        }
    }
}