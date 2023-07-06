package com.nxlinkstar.stargrader.ui.scanner

import android.content.Context
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.nxlinkstar.stargrader.R
import com.nxlinkstar.stargrader.databinding.FragmentHomeBinding
import com.nxlinkstar.stargrader.databinding.FragmentScannerBinding
import com.nxlinkstar.stargrader.utils.ImageFileUtil
import com.nxlinkstar.stargrader.utils.YuvUtils
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.UVCCameraUtil
import com.serenegiant.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScannerFragment : Fragment() {

    companion object {
        fun newInstance() = ScannerFragment()
    }

    private lateinit var viewModel: ScannerViewModel

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!

    private lateinit var mUSBMonitor: USBMonitor

    // TODO: get resolution size from settings
    private var width = 640
    private var height = 480


    private var isSave = false

    private fun intiCamera() {
        mUSBMonitor =
            UVCCameraUtil.initUSBMonitor(
                requireActivity(),
                object : UVCCameraUtil.OnMyDevConnectListener {
                    override fun onConnectDev(
                        device: UsbDevice,
                        ctrlBlock: USBMonitor.UsbControlBlock
                    ) {
                        val pid = String.format("%x", device.productId)
//                        LogUtils.d("pid", "connect: $pid")
                        openRgb(pid, ctrlBlock)
//                        isOpen()

                    }

                })
    }

    private fun openRgb(pid: String, ctrlBlock: USBMonitor.UsbControlBlock) {
        UVCCameraUtil.openRGBCamera(
            pid,
            4,
            width,
            height,
            binding.cameraView,
            ctrlBlock,
            object : UVCCameraUtil.FrameDataCallBack {
                override fun onFrame(data: ByteArray) {
//                    isLook(data)
                    saveBitmap(data)
                }
            })
    }

    private fun saveBitmap(frame: ByteArray?) {
        if (isSave) {
            isSave = false
            val bitmap = getBitmap(frame)
            val savePath = ImageFileUtil.saveBitmap(
                bitmap,
                "height",
                1,
                System.currentTimeMillis().toString(),
                80
            )
            lifecycleScope.launch(Dispatchers.Main) {
//                JUtils.ToastLong(savePath)
            }
        }
    }

    private fun getBitmap(frame: ByteArray?): Bitmap {
       return YuvUtils.nv21ToBitmap(frame, width, height)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ScannerViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intiCamera()

//        binding.buttonToScanner.setOnClickListener {
//            findNavController().navigate(R.id.action_ScannerFragment_to_HomeFragment)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onStart() {
        super.onStart()

        mUSBMonitor.register()
        UVCCameraUtil.requestRGBCameraPermission(requireActivity(), 300, mUSBMonitor)
    }

    override fun onStop() {
        super.onStop()
//        LogUtils.d(TAG, "onStop")
        UVCCameraUtil.releaseRGBCamera()
        mUSBMonitor.unregister()
    }


}