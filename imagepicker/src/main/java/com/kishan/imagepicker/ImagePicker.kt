package com.kishan.imagepicker

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.kishan.imagepicker.databinding.LayoutPickerviewBinding
import com.spaciko.utils.UriHelper
import androidx.appcompat.app.AlertDialog


/**
 * * ...Interface listener to return image path from device
 */
class ImagePicker(listener: MyInterface) : DialogFragment() {
    private lateinit var listener: MyInterface
    private var camera: String = "Camera"
    private var gallery: String = "Gallery"
    private var cancel: String = "Cancel"
    private lateinit var typeface: Typeface


    /**
     * @param context
     * The context.
     * ...Interface listener to return image path from device
     */
    constructor(context: Context, listener: MyInterface) : this(listener) {
        this.listener = listener
        typeface = Typeface.createFromAsset(context.assets, "roboto.ttf")
    }

    /**
     * @param context
     * The context.
     * * ...Interface listener to return image path from device
     * ... Naming set language wise
     */
    constructor(
        context: Context,
        listener: MyInterface, camera: String,
        gallery: String, cancel: String
    ) : this(listener) {
        this.listener = listener
        this.camera = camera
        this.gallery = gallery
        this.cancel = cancel
        typeface = Typeface.createFromAsset(context.assets, "roboto.ttf")
    }

    /**
     * @param context
     * The context.
     * ...Interface listener to return image path from device
     * ...custom font style
     * */
    constructor(listener: MyInterface, typeface: Typeface) : this(listener) {
        this.listener = listener
        this.typeface = typeface
    }

    /**
     * @param context
     * The context.
     * ...Interface listener to return image path from device
     * ...Custom font style
     * ...Naming set language wise
     * */
    constructor(
        listener: MyInterface,
        typeface: Typeface,
        camera: String,
        gallery: String,
        cancel: String
    ) : this(listener) {
        this.listener = listener
        this.typeface = typeface
        this.camera = camera
        this.gallery = gallery
        this.cancel = cancel
    }


    private var image_uri: Uri? = null

    interface MyInterface {
        fun getPath(path: String)
    }

    companion object {
        val TAG: String = ImagePicker::class.java.simpleName
        var PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                showPickerDialog()
            }
        }

    private fun takePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            showPickerDialog()
        }
        activity?.let {
            if (hasPermissions(activity as Context, PERMISSIONS)) {
                showPickerDialog()
            } else {
                permReqLauncher.launch(
                    PERMISSIONS
                )
            }
        }
    }

    private var _binding: LayoutPickerviewBinding? = null

    // This property is only valid between onCreateDialog and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = LayoutPickerviewBinding.inflate(LayoutInflater.from(context))
        initView()
        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() {

        binding.tvCamera.text = camera
        binding.tvGallery.text = gallery
        binding.tvCancel.text = cancel

        /*set font style*/
        binding.tvCamera.typeface = typeface
        binding.tvGallery.typeface = typeface
        binding.tvCancel.typeface = typeface

        takePermission()
    }

    private fun showPickerDialog() {
        binding.rlMain.visibility = View.VISIBLE

        binding.tvCancel.setOnClickListener {
            dismiss()
        }

        binding.tvCamera.setOnClickListener {
            openCamera()
        }

        binding.tvGallery.setOnClickListener {
            openGallery()
        }
    }


    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")

        image_uri = requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )!!
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        resultLauncher.launch(cameraIntent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes

                if (image_uri != null) {
                    UriHelper.getPath(requireContext(), image_uri!!).toString()
                    listener.getPath(UriHelper.getPath(requireContext(), image_uri!!).toString())

                    Log.d(
                        TAG,
                        "Camera Path: " + UriHelper.getPath(requireContext(), image_uri!!)
                            .toString()
                    )
                    dismiss()
                }
            }
        }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        intent.action = Intent.ACTION_GET_CONTENT
        galleryLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    private var galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                if (data?.clipData != null) {
                    val imageURL: Uri? = data.data
                    Log.e("Selected image URL", imageURL.toString())
                    if (imageURL != null) {
                        UriHelper.getPath(requireContext(), imageURL).toString()
                        listener.getPath(
                            UriHelper.getPath(requireContext(), image_uri!!).toString()
                        )
                        Log.d(TAG, ": " + UriHelper.getPath(requireContext(), imageURL).toString())
                    }
                    dismiss()


                } else if (data != null) {
                    if (data.data != null) {
                        val imagePath = data.data
                        Log.e("Selected Single Image", imagePath.toString())
                        if (imagePath != null) {
                            UriHelper.getPath(requireContext(), imagePath).toString()
                            listener.getPath(
                                UriHelper.getPath(requireContext(), image_uri!!).toString()
                            )
                            Log.d(
                                TAG,
                                ": " + UriHelper.getPath(requireContext(), imagePath).toString()
                            )
                        }
                        dismiss()
                    }
                }
            }
        }
}