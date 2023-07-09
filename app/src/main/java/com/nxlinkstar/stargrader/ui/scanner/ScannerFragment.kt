package com.nxlinkstar.stargrader.ui.scanner

import android.content.Context
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.sidesheet.SideSheetDialog
import com.nxlinkstar.stargrader.ItemListDialogFragment
import com.nxlinkstar.stargrader.R
import com.nxlinkstar.stargrader.data.SUBJECTS
import com.nxlinkstar.stargrader.databinding.FragmentHomeBinding
import com.nxlinkstar.stargrader.databinding.FragmentScannerBinding
import com.nxlinkstar.stargrader.databinding.ScanTargetBinding
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

    private lateinit var scanTargetBinding: ScanTargetBinding

    private lateinit var sideSheetDialog: SideSheetDialog

    private  var mUSBMonitor: USBMonitor? = null

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)


        sideSheetDialog = SideSheetDialog(requireContext());
        sideSheetDialog.setContentView(R.layout.scanner_side_sheet)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[ScannerViewModel::class.java]
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.scanTarget.cellSubject.setOnClickListener{
            val list = listOf(
                PickerFragment.Item(SUBJECTS.CHINESE.label, SUBJECTS.CHINESE.code),
                PickerFragment.Item(SUBJECTS.MATH.label, SUBJECTS.MATH.code),
                PickerFragment.Item(SUBJECTS.ENGLISH.label, SUBJECTS.ENGLISH.code),
                PickerFragment.Item(SUBJECTS.PHYSICS.label, SUBJECTS.PHYSICS.code),
                PickerFragment.Item(SUBJECTS.CHEMISTRY.label, SUBJECTS.CHEMISTRY.code),
            )
            val dialog = PickerFragment.newInstance(list, "选择科目")
            dialog.setOnItemSelectedListener(object: PickerFragment.OnItemSelectedListener {
                override fun onItemSelectedListener(position: Int) {
                    val item = list[position]
                    binding.scanTarget.subject.text = item.label
                }

            })
            fragmentManager?.let { it1 -> dialog.show(it1, "SUBJECT_DIALOG") }
        }
//        binding.chan.setOnClickListener {
//            findNavController().navigate(R.id.action_HomeFragment_to_ScannerFragment)
//        }

//        sideSheetDialog.show()

//        val dd = PickerFragment.newInstance(, "11")
//        fragmentManager?.let { dd.show(it, "aa") }


//        val subjectCell = sideSheetDialog.findViewById<View>(R.id.subject)
//        val dialog = PickerFragment()
//        fragmentManager?.let { dialog.show(it, "DD") }
//        subjectCell?.setOnClickListener(
//
//        )

//        intiCamera()

    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_scanner, menu)
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_change_workbook ->  {
//                sideSheetDialog.show()
//                return false
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }


    /**
     * 这个方法会让 返回键失效
     */
    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_scanner, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onStart() {
        super.onStart()

//        mUSBMonitor.register()
//        UVCCameraUtil.requestRGBCameraPermission(requireActivity(), 300, mUSBMonitor!!)
    }

    override fun onStop() {
        super.onStop()
//        LogUtils.d(TAG, "onStop")
//        UVCCameraUtil.releaseRGBCamera()
//        mUSBMonitor.unregister()
    }


}