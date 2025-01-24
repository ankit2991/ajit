package com.gpsnavigation.maps.gpsroutefinder.routemap.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.gpsnavigation.maps.gpsroutefinder.routemap.adapters.RoutesListAdapter
import com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels.MainActivityViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.EditRouteActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.Constants.PREF_ROUTE_ID
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.DialogDeleteBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.DialogRenameRouteBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.FragmentRouteListBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.RouteModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.*

import org.jetbrains.anko.toast
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.util.ArrayList

class RouteListFragment : Fragment(), View.OnClickListener {

    var activityContext: Context? = null
    var routesAdapter: RoutesListAdapter? = null
    val mainActivityViewModel: MainActivityViewModel by sharedViewModel()

    lateinit var binding: FragmentRouteListBinding
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val view = inflater.inflate(R.layout.fragment_route_list, container, false)
        binding = FragmentRouteListBinding.inflate(inflater, container, false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers(view)
        binding.btnAddNewRoute.setOnClickListener(this)
        initPaddings()
        routesAdapter = RoutesListAdapter(
            {

                Timber.tag("Route_list_screen").i("User click on route")
                mainActivityViewModel.onRouteClickFormRouteList.value = it
//                (activity as RFMainActivity).viewPagerMain.currentItem = 0
                if (requireActivity() != null) {
                    val viewPagerMain =
                        requireActivity().findViewById<ViewPager2>(R.id.viewPagerMain)

                    viewPagerMain.currentItem = 1
                }
            },
            {

                 Timber.tag("Route_list_screen").i("User click on dupicate route  route button")
                 LoadingDialog.getInstance(activityContext)?.showDialog(null)
                 mainActivityViewModel.duplicateRoute(it) {newRoute->
                     mainActivityViewModel.getRouteList()[newRoute.routeId] = newRoute
                     mainActivityViewModel.getAllRoutesFromRoom {list->
                         routesAdapter?.setData(list as ArrayList<RouteModel>)
                     }
                     LoadingDialog.getInstance(activityContext)?.dismissDialog()
                     Toast.makeText(activityContext, getString(R.string.duplicated), Toast.LENGTH_SHORT).show()
                 }
            }, { routeModel: RouteModel?, i: Int ->
                /*if (mainActivityViewModel.getRouteList().size == 1) {
                    activityContext?.toast(getString(R.string.cant_delete_default_route))
                } else {*/

                    Timber.tag("Route_list_screen").i("User click on delete route button")
                val deleteBinding:DialogDeleteBinding = DialogDeleteBinding.inflate(layoutInflater)
                val deleteView = deleteBinding.root
//                    val deleteView = layoutInflater.inflate(R.layout.dialog_delete, null)
                deleteBinding.btnCancel.setOnClickListener {

                        CustomDialog.getInstance(activityContext)?.dismissDialog()
                    }
                deleteBinding.btnDelete.setOnClickListener {

                        CustomDialog.getInstance(activityContext)?.dismissDialog()
                        LoadingDialog.getInstance(activityContext)?.showDialog(null)
                        mainActivityViewModel.deleteRoute(routeModel!!.routeId)
                        activityContext?.toast("Deleted")
                        mainActivityViewModel.getRouteList().remove(routeModel.routeId)
                        mainActivityViewModel.setSelectedRoute(null)
                        mainActivityViewModel.getAllRoutesFromRoom {
                            routesAdapter?.setData(it as ArrayList<RouteModel>)
                        }
                        LoadingDialog.getInstance(activityContext)?.dismissDialog()
                        mainActivityViewModel.onDeleteRoute.value = true
                    }
                    CustomDialog.getInstance(activityContext)?.setContentView(deleteView, false)
                        ?.showDialog()
                //}
            }, { routeModel: RouteModel?, i: Int ->

                Timber.tag("Route_list_screen").i("User click on rename route button")
                val renameRouteBinding:DialogRenameRouteBinding = DialogRenameRouteBinding.inflate(layoutInflater)
                val dialogView = renameRouteBinding.root
//                val dialogView = layoutInflater.inflate(R.layout.dialog_rename_route, null)
                renameRouteBinding.etRenameRoute.hint = routeModel?.routeName
                renameRouteBinding.btnCancel.setOnClickListener {
                    CustomDialog.getInstance(activityContext)?.dismissDialog()
                }
                renameRouteBinding.btnSave.setOnClickListener {

                    if (renameRouteBinding.etRenameRoute.text.isEmpty()) {
                        renameRouteBinding.etRenameRoute.error = getString(R.string.route_name)
                        renameRouteBinding.etRenameRoute.requestFocus()
                    } else {
                        routeModel?.let {
                            LoadingDialog.getInstance(activityContext)?.showDialog(null)
                            routeModel.routeName =
                                renameRouteBinding.etRenameRoute.text.toString()
                            mainActivityViewModel.getRouteList()[routeModel.routeId] =
                                routeModel
                            mainActivityViewModel.updateRoute(routeModel){
                                mainActivityViewModel.getAllRoutesFromRoom {
                                    routesAdapter?.setData(it as ArrayList<RouteModel>)
                                }
                            }
                            LoadingDialog.getInstance(activityContext)?.dismissDialog()
                            CustomDialog.getInstance(activityContext)?.dismissDialog()
                        }
                    }
                }
                CustomDialog.getInstance(activityContext)?.setContentView(dialogView, false)
                    ?.showDialog()
            },
            { routeModel: RouteModel?, i: Int ->
                Timber.tag("Route_list_screen").i("User click on share route button")
                if (routeModel != null) {
                    if (routeModel.stopsList.size == 0) {
                        activityContext?.toast(getString(R.string.no_stop_found_in_route_add_stop))
                    } else {
                        if (routeModel.stopsList[0].leg == null) {
                            activityContext?.toast(getString(R.string.first_draw_route_then_share))
                        } else {
                            // mainActivityViewModel.shareRoute(routeModel){ DynamicLinksUtility.shareLink(activityContext!!,it) }
                        }
                    }
                }
            })
        binding.rvRoutes.layoutManager = LinearLayoutManager(activityContext)
        binding.rvRoutes.adapter = routesAdapter
        mainActivityViewModel.getAllRoutesFromRoom {
            routesAdapter?.setData(it as ArrayList<RouteModel>)
        }
    }

    override fun onResume() {
        super.onResume()
        initPaddings()
    }

    private fun initPaddings() {
        if (TinyDB.getInstance(requireContext()).isPremium) {
            (binding.btnAddNewRoute.layoutParams as ConstraintLayout.LayoutParams).apply {
                bottomMargin = 26.dpToPx
            }
        } else {
            (binding.btnAddNewRoute.layoutParams as ConstraintLayout.LayoutParams).apply {
                bottomMargin = 16.dpToPx
            }
        }
    }

    private fun setObservers(view: View) {
        mainActivityViewModel.refreshRouteList.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                it?.let {
                    binding.loadingProgressBar.visibility = View.GONE
                    if (it)
                        mainActivityViewModel.getAllRoutesFromRoom {
                            routesAdapter?.setData(it as ArrayList<RouteModel>)
                        }
                }
            })
        mainActivityViewModel.shakeAddNewRouteButton.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                if (it)
                    shakeView(binding.btnAddNewRoute, 5000)
            })
    }


    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnAddNewRoute -> {

                Timber.tag("Route_list_screen").i("User click on add new route button")
                val renameRouteBinding:DialogRenameRouteBinding = DialogRenameRouteBinding.inflate(layoutInflater)
                val dialogView = renameRouteBinding.root
//                val dialogView = layoutInflater.inflate(R.layout.dialog_rename_route, null)
                renameRouteBinding.etRenameRoute.hint = getString(R.string.route_name)
                renameRouteBinding.etRenameRoute.setText(
                    mainActivityViewModel.getDefaultRouteName()
                )
                renameRouteBinding.etRenameRoute.setSelection(renameRouteBinding.etRenameRoute.text.length)
                renameRouteBinding.etRenameRoute.requestFocus()
                renameRouteBinding.btnCancel.setOnClickListener {

                    CustomDialog.getInstance(activityContext)?.dismissDialog()
                }
                renameRouteBinding.labelHeaderRename.text = getString(R.string.route_name)
                renameRouteBinding.btnSave.text = getString(R.string.create_new_route)
                renameRouteBinding.btnSave.setOnClickListener {

                    LoadingDialog.getInstance(activityContext)?.showDialog(null)
                    mainActivityViewModel.createNewRoute({
                        LoadingDialog.getInstance(activityContext)?.dismissDialog()
                        CustomDialog.getInstance(activityContext)?.dismissDialog()
                        TinyDB.getInstance(activityContext!!).putInt(PREF_ROUTE_ID, it.routeId.toInt())
                        mainActivityViewModel.getRouteList()[it.routeId] = it
                        mainActivityViewModel.getAllRoutesFromRoom {
                            routesAdapter?.setData(it as ArrayList<RouteModel>)
                        }
                        mainActivityViewModel.onRouteClickFormRouteList.value = it
//                        (activity as RFMainActivity).viewPagerMain.currentItem = 0
                        if (requireActivity() != null) {
                            val viewPagerMain =
                                requireActivity().findViewById<ViewPager2>(R.id.viewPagerMain)

                            viewPagerMain.currentItem = 1
                        }
                        val intent = Intent(activityContext, EditRouteActivity::class.java)
                        intent.putExtra(Constants.EDIT_ROUTE_FLAG, it.routeId)
                        startActivity(intent)

                    }, renameRouteBinding.etRenameRoute.text.toString())
                }
                CustomDialog.getInstance(activityContext)?.setContentView(dialogView, false)
                    ?.showDialog()

            }
        }


    }


}