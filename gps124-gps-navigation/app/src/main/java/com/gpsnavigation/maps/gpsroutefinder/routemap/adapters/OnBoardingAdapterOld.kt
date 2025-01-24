package com.gpsnavigation.maps.gpsroutefinder.routemap.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.OnboardingActivity


class OnBoardingAdapterOld(
    private val context: Context,
    private var list: MutableList<OnboardingActivity.OnBoarding>,
    private var callback: (OnboardingActivity.OnBoarding) -> Unit
) :

    RecyclerView.Adapter<OnBoardingAdapterOld.MyHolder>() {


    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_new_onboarding, parent, false
        )
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val model = list[position]
        holder.bind(model)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    inner class MyHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        //private val videoView: VideoView = itemView.findViewById(R.id.videoView)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val vItemLayout: View = itemView


        fun bind(sliderItem: OnboardingActivity.OnBoarding) {
            try {
                when (sliderItem) {
                    OnboardingActivity.OnBoarding.Screen1 -> {
                        context.run {
                            tvTitle.text = getString(R.string.new_onboarding_title_one)
                            vItemLayout.setBackgroundResource(R.drawable.onboarding_9)
                            /*val path =
                                "android.resource://" + packageName.toString() + "/" + R.raw.video1
                            videoView.setVideoURI(Uri.parse(path))
                            videoView.start()*/
                        }
                    }
                    OnboardingActivity.OnBoarding.Screen2 -> {
                        context.run {
                            tvTitle.text = getString(R.string.new_onboarding_title_two)
                            vItemLayout.setBackgroundResource(R.drawable.onboarding_10)
                            /*val path =
                                "android.resource://" + packageName.toString() + "/" + R.raw.video2
                            videoView.setVideoURI(Uri.parse(path))
                            videoView.start()*/
                        }
                    }

                    OnboardingActivity.OnBoarding.Screen3 -> {
                        context.run {
                            tvTitle.text = getString(R.string.new_onboarding_title_three)
                            vItemLayout.setBackgroundResource(R.drawable.onboarding_11)
/*                            val path =
                                "android.resource://" + packageName.toString() + "/" + R.raw.video3
                            videoView.setVideoURI(Uri.parse(path))
                            videoView.start()*/
                        }
                    }
                }
                //videoView.setOnPreparedListener { mp -> mp.isLooping = true }


            } catch (e: Exception) {
                Log.d("Exception on Bind %s", e.message.toString())
                //     FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
    }
}
