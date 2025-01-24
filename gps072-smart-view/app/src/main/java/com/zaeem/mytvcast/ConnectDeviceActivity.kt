package com.zaeem.mytvcast

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.device.ConnectableDeviceListener
import com.connectsdk.discovery.DiscoveryManager
import com.connectsdk.discovery.DiscoveryManagerListener
import com.connectsdk.service.DeviceService
import com.connectsdk.service.DeviceService.PairingType
import com.connectsdk.service.command.ServiceCommandError
import com.ironsource.mediationsdk.IronSource
import com.zaeem.mytvcast.Adapters.TVsAdapter
import com.zaeem.mytvcast.Model.DeviceModel
import com.zaeem.mytvcast.Utils.ItemClickSupport
import com.zaeem.mytvcast.Utils.SimpleDividerItemDecoration
import com.zaeem.mytvcast.Utils.StreamingManager
import com.zaeem.mytvcast.Utils.TinyDB
import com.zaeem.mytvcast.databinding.ActivityConnectBinding
import java.util.*

class ConnectDeviceActivity : AppCompatActivity(), DiscoveryManagerListener {
    private lateinit var binding: ActivityConnectBinding

    private var manager: LinearLayoutManager? = null
    private var adapter: TVsAdapter? = null
    private val devices = ArrayList<DeviceModel>()
    private var pairingAlertDialog: AlertDialog? = null
    private var pairingCodeDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            toolbar.setNavigationIcon(R.drawable.b_close)
            setSupportActionBar(toolbar)
        }

        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        manager = LinearLayoutManager(this)
        adapter = TVsAdapter(devices, this)
        binding.apply {
            recycler.addItemDecoration(SimpleDividerItemDecoration(applicationContext))
            recycler.setLayoutManager(manager)
            recycler.setAdapter(adapter)
            ItemClickSupport.addTo(recycler).setOnItemClickListener { recyclerView, position, v ->
                if (position != RecyclerView.NO_POSITION && position >= 0 && position < devices.size) {
                    val mTV = devices[position].connectableDevice
                    mTV.addListener(deviceListener)
                    mTV.setPairingType(null)
                    mTV.connect()
                }
            }
        }

        DiscoveryManager.init(applicationContext)
        startSearch()
        if (!TinyDB.getInstance(this).isPremium(this)) {
            Handler().postDelayed({
                startActivity(
                    Intent(
                        this@ConnectDeviceActivity,
                        PremiumActivity::class.java
                    )
                )
            }, 500)
        }
    }

    fun checkDevices() {
        binding.apply {
            if (devices.size == 0) {
                tvDevices.text = "Searching for devices..."
            } else {
                tvDevices.text = "Found (" + devices.size + ") device(s)"
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        stopSearch()
    }

    fun startSearch() {
        DiscoveryManager.getInstance().registerDefaultDeviceTypes()
        DiscoveryManager.getInstance().pairingLevel = DiscoveryManager.PairingLevel.ON
        DiscoveryManager.getInstance().addListener(this)
        DiscoveryManager.getInstance().start()
    }

    fun stopSearch() {
        try {
            DiscoveryManager.getInstance().stop()
            DiscoveryManager.destroy()
        } catch (e: Exception) {
        }
    }

    override fun onDeviceAdded(manager: DiscoveryManager, device: ConnectableDevice) {
        try {
            val dm = DeviceModel()
            dm.name = device.friendlyName
            dm.series = device.modelName
            dm.deviceIp = device.ipAddress
            dm.connectableDevice = device
            if (!devices.contains(dm) && (isChromecast(device) || device.modelName.toLowerCase() == "chromecast")) {
                devices.add(dm)
                adapter!!.notifyDataSetChanged()
                checkDevices()
            }
        } catch (e: Exception) {
        }
    }

    fun isChromecast(connectableDevice: ConnectableDevice?): Boolean {
        if (connectableDevice != null) {
            val connectedServiceNames = connectableDevice.connectedServiceNames
            if (connectedServiceNames != null) {
                val lowerCase = connectedServiceNames.toLowerCase()
                return lowerCase.contains("chromecast")
            }
        }
        return false
    }

    override fun onDeviceUpdated(manager: DiscoveryManager, device: ConnectableDevice) {}
    override fun onDeviceRemoved(manager: DiscoveryManager, device: ConnectableDevice) {
        for (dm in devices) {
            if (dm.name == device.friendlyName) {
                devices.remove(dm)
                adapter!!.notifyDataSetChanged()
                checkDevices()
                break
            }
        }
    }

    override fun onDiscoveryFailed(manager: DiscoveryManager, error: ServiceCommandError) {}
    private val deviceListener: ConnectableDeviceListener = object : ConnectableDeviceListener {
        override fun onPairingRequired(
            device: ConnectableDevice,
            service: DeviceService,
            pairingType: PairingType
        ) {
            Log.d("Test", "Connected to " + device.ipAddress)
            when (pairingType) {
                PairingType.FIRST_SCREEN -> {
                    Log.d("Test", "First Screen")
                    pairingAlertDialog = AlertDialog.Builder(this@ConnectDeviceActivity)
                        .setTitle("Pairing with TV")
                        .setMessage("Please confirm the connection on your TV.")
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Cancel") { dialog, which -> }
                        .create()
                    if (!isFinishing) pairingAlertDialog?.show()
                }
                PairingType.PIN_CODE, PairingType.MIXED -> {
                    Log.d("Test", "Pin Code")
                    val input = EditText(this@ConnectDeviceActivity)
                    input.inputType = InputType.TYPE_CLASS_TEXT
                    input.requestFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    pairingCodeDialog = AlertDialog.Builder(this@ConnectDeviceActivity)
                        .setTitle("Enter Pairing Code from your TV")
                        .setView(input)
                        .setPositiveButton(android.R.string.ok) { arg0, arg1 ->
                            if (device != null) {
                                val value = input.text.toString().trim { it <= ' ' }
                                device.sendPairingKey(value)
                                imm.hideSoftInputFromWindow(input.windowToken, 0)
                            }
                        }
                        .setNegativeButton(android.R.string.cancel) { dialog, whichButton ->
                            imm.hideSoftInputFromWindow(
                                input.windowToken,
                                0
                            )
                        }
                        .create()
                    if (!isFinishing) {
                        pairingCodeDialog?.show()
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    }
                }
                PairingType.NONE -> {
                }
                else -> {
                }
            }
        }

        override fun onConnectionFailed(device: ConnectableDevice, error: ServiceCommandError) {
            Log.d("Test", "onConnectFailed")
        }

        override fun onDeviceReady(device: ConnectableDevice) {
            Log.d("Test", "onPairingSuccess")
            try {
                if (pairingAlertDialog != null && pairingAlertDialog!!.isShowing) {
                    pairingAlertDialog!!.dismiss()
                }
                if (pairingCodeDialog != null && pairingCodeDialog!!.isShowing) {
                    pairingCodeDialog!!.dismiss()
                }
            } catch (e: Exception) {
            }
            StreamingManager.getInstance(this@ConnectDeviceActivity).device = device
            finish()
            stopSearch()
        }

        override fun onDeviceDisconnected(device: ConnectableDevice) {
            Log.d("Test", "Device Disconnected")
        }

        override fun onCapabilityUpdated(
            device: ConnectableDevice,
            added: List<String>,
            removed: List<String>
        ) {
        }
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }

    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
    }
}