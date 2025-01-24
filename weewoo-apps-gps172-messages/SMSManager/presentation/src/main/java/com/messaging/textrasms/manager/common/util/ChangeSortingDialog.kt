package com.messaging.textrasms.manager.common.util

import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.leavjenn.smoothdaterangepicker.date.SmoothDateRangePickerFragment
import com.messaging.textrasms.manager.InterstitialConditionDisplay
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.model.logDebug
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.resolveThemeColor
import com.whiteelephant.monthpicker.MonthPickerDialog
import kotlinx.android.synthetic.main.custom_sort_picker.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class ChangeSortingDialog(val activity: Activity, val callback: () -> Unit) :
    DialogInterface.OnClickListener {
    private var view: View
    val dialog: BottomSheetDialog

    companion object {

        const val Date = 0
        const val Month = 1
        const val Year = 2
        const val default = 3
        const val OnlyDate = 4
    }

    init {
        dialog = BottomSheetDialog(activity, R.style.AppTheme1)
        view = activity.layoutInflater.inflate(R.layout.custom_sort_picker, null).apply {


        }
        dialog.setContentView(view)
        //  val bottomSheet = dialog.findViewById<FrameLayout>(R.id.sheet)


        if (dialog.window != null) {
            dialog.window!!.attributes.windowAnimations = R.style.popup_window_animation
            // dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            //  dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)


        }
        val default_layout = view.findViewById<LinearLayout>(R.id.default_layout)
        val cleartxt = view.findViewById<TextView>(R.id.cleartxt)
        val date_layout = view.findViewById<LinearLayout>(R.id.date_layout)
        val month_layout = view.findViewById<LinearLayout>(R.id.month_layout)
        val year_layout = view.findViewById<LinearLayout>(R.id.year_layout)
        val only_date_layout = view.findViewById<LinearLayout>(R.id.only_date_layout)
        val acending_layout = view.findViewById<LinearLayout>(R.id.acending_layout)
        val desending_layout = view.findViewById<LinearLayout>(R.id.desending_layout)
        unselected(view.findViewById(R.id.default_layout), view.findViewById(R.id.default_date))
        unselected(view.findViewById(R.id.date_layout), view.findViewById(R.id.selcted_date))
        unselected(view.findViewById(R.id.month_layout), view.findViewById(R.id.selected_month))
        unselected(view.findViewById(R.id.year_layout), view.findViewById(R.id.selected_year))
        unselected(
            view.findViewById(R.id.only_date_layout),
            view.findViewById(R.id.selcted_date_only)
        )




        unselecctedclick(view.findViewById(R.id.cleartxt))
        unselecctedclick(view.findViewById(R.id.cancel))
        selecctedclick(view.findViewById(R.id.save))
        var clicked = Preferences.getIntVal((activity).applicationContext, "clicked", 0)
        logDebug("checked" + clicked)

        desending_layout.setBackgroundResource(0)
        acending_layout.setBackgroundResource(0)
        val sorted = Preferences.getIntVal((activity).applicationContext, "which", 3)
        when {
            sorted.equals(0) -> seleccted(
                view.findViewById(R.id.date_layout),
                view.findViewById(R.id.selcted_date)
            )
            sorted.equals(1) -> seleccted(
                view.findViewById(R.id.month_layout),
                view.findViewById(R.id.selected_month)
            )
            sorted.equals(2) -> seleccted(
                view.findViewById(R.id.year_layout),
                view.findViewById(R.id.selected_year)
            )
            sorted.equals(3) -> seleccted(
                view.findViewById(R.id.default_layout),
                view.findViewById(R.id.default_date)
            )
            sorted.equals(4) -> seleccted(
                view.findViewById(R.id.only_date_layout),
                view.findViewById(R.id.selcted_date_only)
            )


        }
        val which = Preferences.getIntVal((activity).applicationContext, "which")
//
        view.selection.setVisible(!which.equals(3))

        var day = Preferences.getIntVal((activity).applicationContext, "day", 0)
        var month = Preferences.getIntVal((activity).applicationContext, "selectedMonth", 0)
        val year = Preferences.getIntVal((activity).applicationContext, "selectedYear", 0)
        var newmonth: String
        var datnew: String
        if (sorted.equals(4)) {
            if (day != 0 && month != 0 && year != 0) {
                if (month < 10) {

                    newmonth = "0" + month
                } else {
                    newmonth = month.toString()
                }
                if (day < 10) {

                    datnew = "0" + day
                } else {
                    datnew = day.toString()

                }
                view.selection.text =
                    "last selection -" + toDate("" + year + "-" + month + "-" + day)
            }
        } else if (sorted.equals(1)) {
            if (month != 0 && year != 0) {
                view.selection.text =
                    "last selection" + "-" + toMonth("" + (month + 1) + "-" + year)
            }
        } else if (sorted.equals(2)) {
            if (year != 0) {
                view.selection.text = "last selection" + "-" + year
            }
        } else if (sorted.equals(0)) {
            val start = Preferences.getStringVal((activity).applicationContext, "dayStart")
            val end = Preferences.getStringVal((activity).applicationContext, "dayEnd")
            if (!start.equals("") && !end.equals("") && !start.equals(end)) {
                view.selection.text = "last selection" + "-" + start + " " + "to" + " " + end
            }
        }
        cleartxt.setVisible(true)
        cleartxt.setOnClickListener(View.OnClickListener {
            Preferences.setIntVal((activity).applicationContext, "clicked", 2)
            cleartxt.setVisible(false)
            view.selection.text = ""
            Preferences.setIntVal((activity).applicationContext, "which", 3)
            Preferences.setIntVal((activity).applicationContext, "selectedMonth", 0)
            Preferences.setIntVal((activity).applicationContext, "selectedYear", 0)
            Preferences.setIntVal((activity).applicationContext, "day", 0)
            unselected(
                view.findViewById(R.id.only_date_layout),
                view.findViewById(R.id.selcted_date_only)
            )
            seleccted(view.findViewById(R.id.default_layout), view.findViewById(R.id.default_date))
            unselected(view.findViewById(R.id.month_layout), view.findViewById(R.id.selected_month))
            unselected(view.findViewById(R.id.year_layout), view.findViewById(R.id.selected_year))
            unselected(view.findViewById(R.id.date_layout), view.findViewById(R.id.selcted_date))
        })


        val order_sort = Preferences.getIntVal((activity).applicationContext, "order", 2)
        when {
            order_sort.equals(1) -> {
                acending_layout.setBackgroundResource(R.drawable.order_bg_asending)
                selectimage(
                    view.findViewById(R.id.acending_img),
                    view.findViewById(R.id.acending_layout),
                    view.findViewById(R.id.acsending_txt)
                )
            }
            order_sort.equals(2) -> {
                desending_layout.setBackgroundResource(R.drawable.order_bg_desending)
                selectimage(
                    view.findViewById(R.id.decending_img),
                    view.findViewById(R.id.desending_layout),
                    view.findViewById(R.id.descending_txt)
                )
            }


        }
        // unselected(view.findViewById(R.id.date_layout), view.findViewById(R.id.selcted_date))

        default_layout.setOnClickListener(View.OnClickListener {
            unselected(
                view.findViewById(R.id.only_date_layout),
                view.findViewById(R.id.selcted_date_only)
            )
            seleccted(view.findViewById(R.id.default_layout), view.findViewById(R.id.default_date))
            unselected(view.findViewById(R.id.month_layout), view.findViewById(R.id.selected_month))
            unselected(view.findViewById(R.id.year_layout), view.findViewById(R.id.selected_year))
            unselected(view.findViewById(R.id.date_layout), view.findViewById(R.id.selcted_date))
            Preferences.setIntVal((activity).applicationContext, "which", default)
            Preferences.setIntVal((activity).applicationContext, "day", 0)


        })
        date_layout.setOnClickListener(View.OnClickListener {
            ShowDatePicker()
            unselected(
                view.findViewById(R.id.only_date_layout),
                view.findViewById(R.id.selcted_date_only)
            )
            unselected(view.findViewById(R.id.default_layout), view.findViewById(R.id.default_date))
            unselected(view.findViewById(R.id.month_layout), view.findViewById(R.id.selected_month))
            unselected(view.findViewById(R.id.year_layout), view.findViewById(R.id.selected_year))
            seleccted(view.findViewById(R.id.date_layout), view.findViewById(R.id.selcted_date))


        })
        only_date_layout.setOnClickListener(View.OnClickListener {
            ShowDate()
            unselected(view.findViewById(R.id.default_layout), view.findViewById(R.id.default_date))
            unselected(view.findViewById(R.id.month_layout), view.findViewById(R.id.selected_month))
            unselected(view.findViewById(R.id.year_layout), view.findViewById(R.id.selected_year))
            unselected(view.findViewById(R.id.date_layout), view.findViewById(R.id.selcted_date))
            seleccted(
                view.findViewById(R.id.only_date_layout),
                view.findViewById(R.id.selcted_date_only)
            )


        })
        month_layout.setOnClickListener(View.OnClickListener {
            ShowMonthPicker()
            unselected(
                view.findViewById(R.id.only_date_layout),
                view.findViewById(R.id.selcted_date_only)
            )
            unselected(view.findViewById(R.id.default_layout), view.findViewById(R.id.default_date))
            unselected(view.findViewById(R.id.date_layout), view.findViewById(R.id.selcted_date))
            unselected(view.findViewById(R.id.year_layout), view.findViewById(R.id.selected_year))
            seleccted(view.findViewById(R.id.month_layout), view.findViewById(R.id.selected_month))
        })
        year_layout.setOnClickListener(View.OnClickListener {
            ShowyearPicker()
            unselected(
                view.findViewById(R.id.only_date_layout),
                view.findViewById(R.id.selcted_date_only)
            )
            unselected(view.findViewById(R.id.default_layout), view.findViewById(R.id.default_date))
            unselected(view.findViewById(R.id.date_layout), view.findViewById(R.id.selcted_date))
            unselected(view.findViewById(R.id.month_layout), view.findViewById(R.id.selected_month))
            seleccted(view.findViewById(R.id.year_layout), view.findViewById(R.id.selected_year))

        })


        acending_layout.setOnClickListener(View.OnClickListener {
            Preferences.setIntVal((activity).applicationContext, "order", 1)
            acending_layout.setBackgroundResource(R.drawable.order_bg_asending)
            desending_layout.setBackgroundResource(0)
            unselectimage(
                view.findViewById(R.id.decending_img),
                view.findViewById(R.id.desending_layout),
                view.findViewById(R.id.descending_txt)
            )
            selectimage(
                view.findViewById(R.id.acending_img),
                view.findViewById(R.id.acending_layout),
                view.findViewById(R.id.acsending_txt)
            )
        })
        desending_layout.setOnClickListener(View.OnClickListener {
            desending_layout.setBackgroundResource(R.drawable.order_bg_desending)
            acending_layout.setBackgroundResource(0)
            Preferences.setIntVal((activity).applicationContext, "order", 2)
            selectimage(
                view.findViewById(R.id.decending_img),
                view.findViewById(R.id.desending_layout),
                view.findViewById(R.id.descending_txt)
            )
            unselectimage(
                view.findViewById(R.id.acending_img),
                view.findViewById(R.id.acending_layout),
                view.findViewById(R.id.acsending_txt)
            )
        })

//        AlertDialog.Builder(activity)
//                .setPositiveButton(R.string.button_save, this)
//                .setNegativeButton(R.string.button_cancel, null)

        dialog.create().apply {
            // activity.setupDialogStuff(view, this, R.string.sort_by)
            view.cancel.setBackgroundResource(R.drawable.grey_bg)
            view.cleartxt.setBackgroundResource(R.drawable.grey_bg)
            view.save.setBackgroundResource(R.drawable.order_bg_desending)
            view.findViewById<TextView>(R.id.save).setOnClickListener(View.OnClickListener {
                Preferences.setIntVal((activity).applicationContext, "clicked", 0)
                if (dialog.isShowing && !activity.isFinishing) {
                    dialog.dismiss()
                    callback()
                }
                InterstitialConditionDisplay.getInstance().increaseClicked()
            })
            view.findViewById<TextView>(R.id.cancel).setOnClickListener(View.OnClickListener {
                Preferences.setIntVal((activity).applicationContext, "clicked", 1)
                if (dialog.isShowing && !activity.isFinishing) {
                    dialog.dismiss()
                }
//                        callback()
            })
        }
        if (!activity.isFinishing) {
            try {
                dialog.show()
            } catch (e: Exception) {

            }

        }
        dialog.window!!.setBackgroundDrawableResource(R.color.dialog_bg)
        // setupSortRadio()
        //setupOrderRadio()
    }


    fun toDate(dateString: String): String? {
        var date: Date
        var formattedDate: String = ""
        var finalDate: Date? = null
        Log.e("parse: ", dateString)
        // val formatter = SimpleDateFormat("yyyy-MM-dd")
        try {
//            date = formatter.parse(dateString)
//
//            val timeFormat = SimpleDateFormat("MMM dd ,yyyy")
//             finalDate = timeFormat.parse(date.toString())


            val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val targetFormat = SimpleDateFormat(" MMM,dd yyyy")
            val date = originalFormat.parse(dateString)
            formattedDate = targetFormat.format(date)
            Log.e("Print9999 : ", formattedDate)
        } catch (e1: ParseException) {
            e1.printStackTrace()
        }

        return formattedDate
    }

    fun toMonth(dateString: String): String? {
        var formattedDate: String = ""
        Log.e("parse: ", dateString)
        // val formatter = SimpleDateFormat("yyyy-MM-dd")
        try {
//            date = formatter.parse(dateString)
//
//            val timeFormat = SimpleDateFormat("MMM dd ,yyyy")
//             finalDate = timeFormat.parse(date.toString())


            val originalFormat = SimpleDateFormat("MM-yyyy")
            val targetFormat = SimpleDateFormat(" MMM, yyyy")
            val date = originalFormat.parse(dateString)
            formattedDate = targetFormat.format(date)
            Log.e("Print9999 : ", formattedDate)
        } catch (e1: ParseException) {
            e1.printStackTrace()
        }

        return formattedDate
    }

    override fun onClick(dialog: DialogInterface, which: Int) {

        callback()
    }

    fun Activity.setupDialogStuff(
        view: View,
        dialog: AlertDialog,
        titleId: Int = 0,
        titleText: String = "",
        callback: (() -> Unit)? = null
    ) {
        if (isDestroyed || isFinishing) {
            return
        }


        dialog.apply {
            setView(view)
            if (window != null) {
                window!!.attributes.windowAnimations = R.style.popup_window_animation
                window!!.setBackgroundDrawableResource(R.drawable.rectangle)

            }


            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCanceledOnTouchOutside(false)
            show()

        }
        callback?.invoke()
    }

    fun seleccted(view: View, textView: TextView) {
        view.setBackgroundResource(R.drawable.sort_btn_bg)
        textView.setTextColor(Color.WHITE)
        //textView.setPadding(25,25,25,25)
    }

    fun unselected(view: View, textView: TextView) {
        view.setBackgroundResource(R.drawable.sort_unselect_bg)
        textView.setTextColor(activity.resolveThemeColor(android.R.attr.textColorPrimary))
        // textView.setPadding(0,0,0,0)

    }

    fun unselectimage(imageView: ImageView, view: View, textView: TextView) {
        textView.setTextColor(activity.resolveThemeColor(android.R.attr.textColorPrimary))
        imageView.setColorFilter(activity.resolveThemeColor(android.R.attr.textColorPrimary))

    }

    fun selectimage(imageView: ImageView, view: View, textView: TextView) {
        //view.setBackgroundResource(R.drawable.default_btn_bg)
        textView.setTextColor(Color.WHITE)
        imageView.setColorFilter(Color.WHITE)

    }

    fun selecctedclick(view: TextView) {
        view.setBackgroundResource(R.drawable.order_bg_desending)
        view.setTextColor(Color.WHITE)
        //textView.setPadding(25,25,25,25)
    }

    fun unselecctedclick(view: TextView) {
        view.setBackgroundResource(R.drawable.grey_bg)
        view.setTextColor(activity.resolveThemeColor(android.R.attr.textColorPrimary))
        //textView.setPadding(25,25,25,25)
    }

    fun ShowMonthPicker() {
        val which = Preferences.getIntVal((activity).applicationContext, "which")
        val date = Preferences.getIntVal((activity).applicationContext, "selectedMonth", 0)
        val year = Preferences.getIntVal((activity).applicationContext, "selectedYear", 0)
        val calendar = Calendar.getInstance()
        var date_after: Int
        if (date != 0) {
            if (which.equals(OnlyDate)) {
                date_after = (date - 1)
            } else if (which.equals(Date)) {
                date_after = (date)
            } else {
                date_after = date
            }
        } else {
            date_after = calendar.get(Calendar.MONTH)
        }
        if (year != 0) {
            calendar.set(Calendar.YEAR, year)

        } else {
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
        }

        calendar.set(Calendar.MONTH, date_after)
        val builder = MonthPickerDialog.Builder(
            activity,
            MonthPickerDialog.OnDateSetListener { selectedMonth, selectedYear ->
                //  viewModel.sorting("lastMessage.month", selectedMonth.toLong(), Sort.DESCENDING)
                Preferences.setIntVal(
                    (activity).applicationContext,
                    "selectedMonth",
                    (selectedMonth)
                )
                Preferences.setIntVal((activity).applicationContext, "selectedYear", selectedYear)
                Preferences.setIntVal((activity).applicationContext, "which", Month)
                //  view.cleartxt.setVisible(true)
                view.selection.setVisible(true)
                view.selection.text =
                    "last selection" + "-" + toMonth("" + (selectedMonth + 1) + "-" + selectedYear)

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH)
        )


        try {


            builder.setActivatedMonth(calendar.get(Calendar.MONTH))
                .setMinYear(1990)
                .setActivatedYear(calendar.get(Calendar.YEAR))
                .setMaxYear(Calendar.getInstance().get(Calendar.YEAR))
                .setMinMonth(Calendar.JANUARY)
                .setTitle("Select trading month")
                .setMonthRange(Calendar.JANUARY, Calendar.DECEMBER)
                .setOnYearChangedListener(MonthPickerDialog.OnYearChangedListener { year: Int -> })
                .setOnMonthChangedListener(MonthPickerDialog.OnMonthChangedListener { selectedMonth: Int ->
                })
                .build().show()
        } catch (e: Exception) {

        }
    }

    fun ShowyearPicker() {
        try {


            val date = Preferences.getIntVal((activity).applicationContext, "selectedYear", 0)
            val calendar = Calendar.getInstance()
            var date_after: Int
            if (date != 0) {

                date_after = date
            } else {
                date_after = calendar.get(Calendar.YEAR)
            }

            calendar.set(Calendar.YEAR, date_after)
            val builder = MonthPickerDialog.Builder(
                activity,
                MonthPickerDialog.OnDateSetListener { selectedMonth, selectedYear ->
                    //  viewModel.sorting("lastMessage.year", selectedYear.toLong(), Sort.DESCENDING)
                    Preferences.setIntVal(
                        (activity).applicationContext,
                        "selectedMonth",
                        selectedMonth
                    )
                    Preferences.setIntVal(
                        (activity).applicationContext,
                        "selectedYear",
                        selectedYear
                    )
                    Preferences.setIntVal((activity).applicationContext, "which", Year)
                    //  view.cleartxt.setVisible(true)
                    view.selection.setVisible(true)
                    view.selection.text = "last selection" + "-" + selectedYear
                },
                calendar.get(Calendar.YEAR),
                0
            ).setMaxYear(calendar.get(Calendar.YEAR))



            builder.showYearOnly()
                .setYearRange(1990, Calendar.getInstance().get(Calendar.YEAR))
                .build().show()
        } catch (e: java.lang.Exception) {

        }

    }

    fun ShowDatePicker() {
        val smoothDateRangePickerFragment =
            SmoothDateRangePickerFragment.newInstance { view1, yearStart, monthStart, dayStart, yearEnd, monthEnd, dayEnd ->
                // grab the date range, do what you want
                Preferences.setIntVal((activity).applicationContext, "selectedMonth", monthEnd)
                Preferences.setIntVal((activity).applicationContext, "selectedYear", yearEnd)
                Preferences.setStringVal(
                    (activity).applicationContext,
                    "dayStart",
                    "" + yearStart + "-" + (monthStart + 1) + "-" + dayStart
                )
                Preferences.setStringVal(
                    (activity).applicationContext,
                    "dayEnd",
                    "" + yearEnd + "-" + (monthEnd + 1) + "-" + dayEnd
                )
                Preferences.setIntVal((activity).applicationContext, "which", Date)
                logDebug("checkdate" + dayStart + ">>>" + "" + yearEnd + "-" + (monthEnd + 1) + "-" + dayEnd)
                view.selection.setVisible(true)
                //  view.cleartxt.setVisible(true)
                view.selection.text =
                    "last selection" + "-" + "" + yearStart + "-" + (monthStart + 1) + "-" + dayStart + " " + "to" + " " + "" + yearEnd + "-" + (monthEnd + 1) + "-" + dayEnd
//            if (day != 0 && month != 0 && year!=0)
//                view.cleartxt.setText("selected date" + day+"/"+month+"/"+year)
                //  viewModel.sorting("lastMessage.month", monthStart.toLong(), Sort.DESCENDING)
            }

        smoothDateRangePickerFragment.show(activity.fragmentManager, "smoothDateRangePicker")
    }

    fun ShowDate() {
        val which = Preferences.getIntVal((activity).applicationContext, "which", 3)
        logDebug("which" + which)
        val calendar = Calendar.getInstance()
        val date = Preferences.getIntVal((activity).applicationContext, "day", 0)
        val month = Preferences.getIntVal((activity).applicationContext, "selectedMonth", 0)
        val year = Preferences.getIntVal((activity).applicationContext, "selectedYear", 0)

        var date_after: Int = 0
        if (date != 0) {
            if (!which.equals(3)) {
                date_after = date
                calendar.set(Calendar.DAY_OF_MONTH, date_after)
            }
        } else {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
        }
        if (month != 0) {
            if (!which.equals(3)) {
                if (which.equals(OnlyDate)) {
                    calendar.set(Calendar.MONTH, (month - 1))
                } else if (which.equals(Date)) {
                    calendar.set(Calendar.MONTH, month)
                }
            } else {
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            }
        } else {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
        }
        if (year != 0) {
            calendar.set(Calendar.YEAR, year)

        }

        logDebug("checkdate" + calendar.get(Calendar.MONTH) + ">>>" + date_after)

        DatePickerDialog(
            activity,
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                Preferences.setIntVal((activity).applicationContext, "selectedMonth", (month + 1))
                Preferences.setIntVal((activity).applicationContext, "selectedYear", year)
                Preferences.setIntVal((activity).applicationContext, "day", day)
                Preferences.setStringVal(
                    (activity).applicationContext,
                    "dayStart",
                    "" + year + "-" + (month + 1) + "-" + day
                )
                Preferences.setStringVal(
                    (activity).applicationContext,
                    "dayEnd",
                    "" + year + "-" + (month + 1) + "-" + day
                )
                Preferences.setIntVal((activity).applicationContext, "which", OnlyDate)
                view.selection.setVisible(true)
                logDebug("checkdate" + year + ">>>" + "" + month + "-" + day)
                if (day != 0 && month != 0 && year != 0) {
                    view.cleartxt.setVisible(true)
                    view.selection.text =
                        "last selection -" + toDate("" + year + "-" + (month + 1) + "-" + day)
                }

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }


}
