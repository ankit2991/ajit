package com.translate.languagetranslator.freetranslation.activities.camera.fragments

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.theartofdev.edmodo.cropper.CropImageView
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.camera.CustomCamView
import com.translate.languagetranslator.freetranslation.activities.camera.viewModel.CameraViewModel
import com.translate.languagetranslator.freetranslation.appUtils.Constants
import com.translate.languagetranslator.freetranslation.appUtils.showToast
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.layout_ocr_image_preview.*

class OcrImageFragment : Fragment(), CropImageView.OnCropImageCompleteListener {

    private var isImageCaptured = false
    val viewModel: CameraViewModel by lazy {
        ViewModelProviders.of(this).get(CameraViewModel::class.java)
    }

    private var fragmentView: View? = null
    private var camera: Camera? = null
    private var camParams: Camera.Parameters? = null
    private var cameraDisplay: Display? = null
    private var mContext: Context? = null
    private var mActivity: Activity? = null
    private var isFlashLightOn = false
    private var isDetailAvailable = true


    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        this.mActivity = activity
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.layout_ocr_image_preview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentView = view
        setClickListneres()

    }

    private fun setClickListneres() {
        fragmentView?.apply {
            iv_gallery_ocr.setOnClickListener {
                openGallery()
            }
            iv_camera_ocr.setOnClickListener {
                captureImage()
            }
            iv_flash_ocr_toggle.setOnClickListener { toggleFlash() }

            layout_translate_btn.setOnClickListener {
                extractTextFromImage()
            }
            iv_clear_ocr.setOnClickListener {
                hideResultView()
            }
        }
    }

    private fun hideResultView() {
        fragmentView?.apply {
            layout_ocr_result_view.visibility = View.GONE
            ocr_result_container_output.visibility = View.GONE
            progress_ocr.visibility = View.VISIBLE
            tv_ocr_input.text = ""
            tv_ocr_result_src_lang.text = ""
            tv_ocr_result_tar_lang.text = ""
            tv_ocr_output.text = ""
        }
    }

    var strBuilder: StringBuilder? = null

    private fun extractTextFromImage() {

        image_holder.setOnCropImageCompleteListener(this)
        image_holder.getCroppedImageAsync()

    }

    private fun toggleFlash() {

    }

    private fun captureImage() {
        if (isFlashLightOn) {
            turnOffFlash()
        }
        kotlin.runCatching {
            mActivity?.apply {
                camera?.let { cam ->

                    cam.takePicture(null, null,
                        { bytes: ByteArray, camera: Camera? ->
                            Thread {
                                val imageBitMap =
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                if (imageBitMap != null) {
                                    convertImageToBitmap(imageBitMap)
                                } else {
                                    runOnUiThread {
                                        showToast(this, "Try Again")
                                        resetAll()
                                    }
                                }
                            }.start()
                        })
                }
            }

        }

    }

    private fun resetAll() {

    }

    private fun convertImageToBitmap(imageBitMap: Bitmap) {
        viewModel.decodeBitMap(imageBitMap) { bitmap1 ->
            fetchImageFromSource(bitmap1)
            null
        }

    }

    private fun fetchImageFromSource(bitmap: Bitmap?) {
        var imageBitMap = bitmap
        if (cameraDisplay != null) {
            val rotation = cameraDisplay!!.rotation
            if (rotation != 0) {
                when (rotation) {
                    Surface.ROTATION_180 -> imageBitMap = viewModel.getImageBitMap(imageBitMap, 270)
                    Surface.ROTATION_270 -> imageBitMap = viewModel.getImageBitMap(imageBitMap, 180)
                }
            } else {
                imageBitMap = viewModel.getImageBitMap(imageBitMap, 90)
            }
        }
        val finalImageBitMap = imageBitMap
        mActivity?.apply {
            runOnUiThread { processCapturedImage(finalImageBitMap) }

        }
    }

    private fun processCapturedImage(finalImageBitMap: Bitmap?) {
        stopCameraPreview()
        image_holder.setImageBitmap(finalImageBitMap)
        layout_image_captured.visibility = View.VISIBLE

        layout_ocr_image_bottom.visibility = View.GONE
        layout_translate_btn.visibility = View.VISIBLE
        isImageCaptured = true
//        if (isAdLoaded) {
//            camera_banner_container.visibility = View.VISIBLE
//        }
    }

    private fun stopCameraPreview() {
        camera?.let { cam ->
            cam.cancelAutoFocus()
            cam.setPreviewCallback(null)
            cam.stopPreview()
        }
    }


    private fun turnOffFlash() {
        kotlin.runCatching {
            camera?.let { cam ->
                camParams = cam.parameters
                camParams?.flashMode = Camera.Parameters.FLASH_MODE_OFF
                camera!!.parameters = camParams
                isFlashLightOn = false


            }
        }

    }

    private fun openGallery() {
    }

    private fun openCameraPreview() {
        kotlin.runCatching {
            fragmentView?.apply {
                mContext?.let { context ->
                    camera = Camera.open()
                    val customCamView = CustomCamView(context, camera)
                    camera_layout.removeAllViews()
                    camera_layout.addView(customCamView)
                    camera?.let { cam ->
                        var parameters: Camera.Parameters = cam.parameters
                        parameters.pictureFormat = 256
                        cameraDisplay = mActivity?.windowManager?.defaultDisplay
                        when (cameraDisplay!!.rotation) {
                            Surface.ROTATION_0 -> {
                                cam.setDisplayOrientation(90)
                            }
                            Surface.ROTATION_90 -> {
                                cam.setDisplayOrientation(0)
                            }
                            Surface.ROTATION_180 -> {
                                cam.setDisplayOrientation(270)
                            }
                            Surface.ROTATION_270 -> {
                                cam.setDisplayOrientation(180)
                            }
                        }
                        parameters = Constants.getCamParams(mActivity, parameters)
                        cam.parameters = parameters
                    }

                }
            }

        }

        handler.sendEmptyMessageDelayed(0, 500)

    }

    private var handler: Handler = object : Handler() {
        override fun handleMessage(message: Message) {
            super.handleMessage(message)
            when (message.what) {
                0 -> {
                    setCamParams()
                    return
                }
                1 -> {
                    showToast(mContext!!, "Error")
                    return
                }
                else -> {
                }
            }
        }
    }

    fun setCamParams() {
        try {
            val parameters = camera!!.parameters
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            camera!!.parameters = parameters
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        openCameraPreview()
    }


    override fun onCropImageComplete(view: CropImageView?, result: CropImageView.CropResult?) {
        kotlin.runCatching {
            mActivity?.apply {
                val croppedImage = result?.bitmap
                val leftLangCode = viewModel.getOcrLeftLangCode(this)
                val leftLangName = viewModel.getOcrLeftLangName(this)
                val rightLangCode = viewModel.getOcrRightLangCode(this)

                fragmentView?.apply {
                    viewModel.extractText(mActivity!!, croppedImage, leftLangCode) { result ->
                        layout_image_captured.visibility = View.GONE
                        layout_ocr_result_view.visibility = View.VISIBLE
                        tv_ocr_input.text = result
                        tv_ocr_result_src_lang.text = leftLangName
                        isDetailAvailable = true


                        viewModel.translateWord(result!!, leftLangCode, rightLangCode) {
                            it?.let {
                                setOutputData(result, it, leftLangCode, rightLangCode)
                            }
                        }
                    }

                }
            }

        }

    }

    private fun setOutputData(
        result: String,
        it: String,
        leftLangCode: String,
        rightLangCode: String
    ) {
        kotlin.runCatching {
            fragmentView?.apply {
                progress_ocr.visibility = View.GONE
                ocr_result_container_output.visibility = View.VISIBLE
                tv_ocr_result_tar_lang.text = viewModel.getOcrRightLangName(mActivity!!)
                tv_ocr_output.text = result

            }
        }

    }

}