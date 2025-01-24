package com.translate.languagetranslator.freetranslation.phrasebook.phrasebookTranslation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.mainScreen.viewModel.MainViewModel
import com.translate.languagetranslator.freetranslation.appUtils.TinyDB
import com.translate.languagetranslator.freetranslation.appUtils.getLangLocale
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable
import com.translate.languagetranslator.freetranslation.interfaces.ClickPhrasesItem
import com.translate.languagetranslator.freetranslation.interfaces.LangCodeInterface
import com.translate.languagetranslator.freetranslation.interfaces.PhraseitemCallback
import kotlinx.android.synthetic.main.add_new_value.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class ItemDetailAdapter(
    var context: Context,
    var list: ArrayList<ItemModel>,
    var langCodeInterface: LangCodeInterface,
    var clickPhrasesItem: ClickPhrasesItem
) :
    RecyclerView.Adapter<ItemDetailAdapter.ViewHolder>() {
    //lateinit var itemModel: ItemModel
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.phrasebook_detail_item, parent, false)
        return ViewHolder(v)
    }

    var isSlideUp = false
    private var newTranslationModel: TranslationTable? = null
    lateinit var mTTS:TextToSpeech
    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var heading: TextView
        var subHeading: TextView
        var Translated: TextView
        var parentLinearLayout: LinearLayout
        lateinit var expandableLayout: ConstraintLayout


        init {

            heading = itemView.findViewById(R.id.heading)
            subHeading = itemView.findViewById(R.id.simple_text)
            Translated = itemView.findViewById(R.id.translated_text)
            parentLinearLayout = itemView.findViewById(R.id.parent_linear_layout)
            expandableLayout = itemView.findViewById(R.id.expandable_layout)
//            expandableLayout.visibility  = View.GONE
//            subHeading.setOnClickListener {
//                if (expandableLayout.visibility == View.VISIBLE) {
////                    if (isSlideUp){
//                    expandableLayout.visibility  = View.GONE
////                    expandableLayout.slideUp()
//            } else {
//                    expandableLayout.visibility = View.VISIBLE
//
////                isSlideUp = true
////                    expandableLayout.slideDown()
//            }
//                var model = list.get(absoluteAdapterPosition)
//                model.expanded = true
//                notifyItemChanged(absoluteAdapterPosition)
//            }
        }
    }

    // method for filtering our recyclerview items.
    fun filterList(filterllist: ArrayList<ItemModel>) {
        // below line is to add our filtered
        // list in our course array list.
        list = filterllist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var itemModel = list.get(position)
//        holder.Text.setImageResource(item.icon)
//        holder.heading.setText(itemModel.heading)
//        TinyDB.getInstance(context).putBoolean("saveHeart", false)

        mTTS = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR){
                //if there is no error then set language
                mTTS.language = Locale.UK
            }
        })

        holder.heading.setText(itemModel.heading)
        var transArrays = itemModel.translated
        var simpleArray = itemModel.subheading

        holder.parentLinearLayout.removeAllViews()
        for (value in itemModel.subheading.indices) {


//            holder.subHeading.setText(value)
          //  Log.e("result", "subheading"+value)

            var inflater = LayoutInflater.from(context).inflate(R.layout.add_new_value, null)
            holder.parentLinearLayout.addView(inflater, holder.parentLinearLayout.childCount)
            inflater.simple_text.setText(simpleArray[value])
            inflater.translated_text.setText(transArrays[value])
            inflater.expandable_layout.visibility = View.GONE
            inflater.fill_heart.visibility = View.GONE


            inflater.subheading.setOnClickListener {
                if (inflater.expandable_layout.visibility == View.VISIBLE) {
//                    inflater.expandable_layout.slideUp()
//                    inflater.expandable_layout.visibility = View.GONE
                    slideDown(inflater.expandable_layout)
                    inflater.arrow_img.setImageResource(R.drawable.arrow_down)
                    slideUp(inflater.arrow_img)
                } else {
                    clickPhrasesItem.onItemClick()

//                    inflater.expandable_layout.slideDown()
//                    inflater.expandable_layout.visibility = View.VISIBLE
                    slideUp(inflater.expandable_layout)

                    inflater.arrow_img.setImageResource(R.drawable.arrow_up)
                    slideUp(inflater.arrow_img)
                }
            }
            inflater.copy_text.setOnClickListener {
                context.copyToClipboard(inflater.translated_text.text.toString())
                Toast.makeText(context, "Copy to Clipboard", Toast.LENGTH_LONG).show()

            }
                if (TinyDB.getInstance(context).getListString("heart").contains(simpleArray[value]))
                 {
//                    ind += 1
                    inflater.fill_heart.visibility = View.VISIBLE
                    inflater.heart.setImageResource(R.drawable.fill_heart)
                   //  Log.e("d===>>","d"+com.translate.languagetranslator.freetranslation.utils.Constants.selectedHeartList.size)

                 }
//                else{
//                    Log.e("===>>>","tinydb"+TinyDB.getInstance(context).getListString("heart").size)
//
//                }

//            else{
//                inflater.fill_heart.visibility = View.GONE
//                inflater.heart.setImageResource(R.drawable.heart_icon)
//            }

             //   ContextCompat.getSystemService(context, ClipboardManager::class.java)

            inflater.share.setOnClickListener {
                share(inflater.translated_text.text.toString())
            }
            inflater.heart.setOnClickListener {



                if(inflater.fill_heart.visibility == View.GONE){
                    inflater.heart.setImageResource(R.drawable.fill_heart)
                    inflater.fill_heart.visibility = View.VISIBLE
                    com.translate.languagetranslator.freetranslation.utils.Constants.selectedHeartList.add(simpleArray[value])
                    TinyDB.getInstance(context).putListString("heart",com.translate.languagetranslator.freetranslation.utils.Constants.selectedHeartList)

                    Log.e("whenclick","arr"+com.translate.languagetranslator.freetranslation.utils.Constants.selectedHeartList.size)
                }else{
                    inflater.heart.setImageResource(R.drawable.heart_icon)
                    inflater.fill_heart.visibility = View.GONE

                    com.translate.languagetranslator.freetranslation.utils.Constants.selectedHeartList = TinyDB.getInstance(context).getListString("heart")
                    com.translate.languagetranslator.freetranslation.utils.Constants.selectedHeartList.remove(simpleArray[value])
//                    TinyDB.getInstance(context).getListString("heart").remove(simpleArray[value])
                    TinyDB.getInstance(context).putListString("heart",com.translate.languagetranslator.freetranslation.utils.Constants.selectedHeartList)

//                    Log.e("afterunclick","after"+TinyDB.getInstance(context).getListString("heart").size)

                }
            }
            inflater.text_to_speak.setOnClickListener {
//                speakOut(inflater.translated_text.text.toString())

                langCodeInterface.onItemSpeak(inflater.translated_text.text.toString())
//                speakInputWord(inflater.translated_text.text.toString())
            }
        }

//        for (valueTrans in itemModel.translated) {

//            inflater.translated_text.setText(valueTrans)
//        }
//        for (valueTrans in itemModel.translated){
//
//            holder.Translated.setText(valueTrans)
//        }

//        if (sub) {
//            holder.expandableLayout.visibility = View.VISIBLE
//        } else {
//            holder.expandableLayout.visibility = View.GONE
//
//        }
        holder.itemView.setOnClickListener {
        }


    }

//    fun View.slideUp(duration: Int = 500) {
//        visibility = View.VISIBLE
//        val animate = TranslateAnimation(0f, 0f, this.height.toFloat(), 0f)
//        animate.duration = duration.toLong()
//        animate.fillAfter = true
//        this.startAnimation(animate)
//        isSlideUp = false
//    }
//
//    fun View.slideDown(duration: Int = 500) {
//        visibility = View.VISIBLE
//        val animate = TranslateAnimation(0f, 0f, 0f, this.height.toFloat())
//        animate.duration = duration.toLong()
//        animate.fillAfter = true
//        this.startAnimation(animate)
//        isSlideUp = true
//
//    }


    fun slideDown(view: View) {
        view.animate()
            .translationY(view.height.toFloat())
            .alpha(0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // superfluous restoration
                    view.visibility = View.GONE
                    view.alpha = 1f
                    view.translationY = 0f
                }
            })
    }

    fun slideUp(view: View) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        if (view.height > 0) {
            slideUpNow(view)
        } else {
            // wait till height is measured
            view.post { slideUpNow(view) }
        }
    }

    private fun slideUpNow(view: View) {
        view.translationY = view.height.toFloat()
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.VISIBLE
                    view.alpha = 1f
                }
            })
    }

    fun Context.copyToClipboard(text: CharSequence){
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label",text)
        clipboard.setPrimaryClip(clip)
    }

    private fun share(result: String) {
        val intent= Intent()
        intent.action=Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT,result)
        intent.type="text/plain"
        context.startActivity(Intent.createChooser(intent,"Share To:"))
    }

    private fun speakOut(text: String) {

//        mTTS.setLanguage(Locale.)
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
    }

    private fun speakInputWord(inputWord: String) {
        newTranslationModel?.let { model ->

//            val inputWord = model.inputStr
            speakWord(model.getInputLanguage(), inputWord)
        }

    }
    private fun speakWord(langCode: String, speakingWord: String) {
        val langLocal = getLangLocale(langCode)
        mTTS.language = langLocal
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "UniqueID"
        @Suppress("DEPRECATION")
        mTTS.speak(speakingWord, TextToSpeech.QUEUE_FLUSH, map)
    }
}