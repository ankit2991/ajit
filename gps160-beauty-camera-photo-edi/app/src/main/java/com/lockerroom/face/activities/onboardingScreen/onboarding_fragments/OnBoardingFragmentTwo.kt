package com.lockerroom.face.activities.onboardingScreen.onboarding_fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.lockerroom.face.R

class OnBoardingFragmentTwo : Fragment() {

    lateinit var image: ImageView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_on_boarding_two, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        image = view.findViewById(R.id.mainImage2)
        try {
            val reqActivity = activity
            if (reqActivity != null) {
                Glide.with(reqActivity)
                    .load(R.drawable.onboarding_img2)
                    .into(image)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "image fail to load", Toast.LENGTH_SHORT).show()
        }
    }
}