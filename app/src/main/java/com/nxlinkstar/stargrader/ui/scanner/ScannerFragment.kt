package com.nxlinkstar.stargrader.ui.scanner

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.sidesheet.SideSheetDialog
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.CameraFragment
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.callback.ICaptureCallBack
import com.jiangdg.ausbc.callback.IPreviewDataCallBack
import com.jiangdg.ausbc.utils.ToastUtils
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.jiangdg.ausbc.widget.CaptureMediaView
import com.jiangdg.ausbc.widget.IAspectRatio
import com.nxlinkstar.stargrader.R
import com.nxlinkstar.stargrader.databinding.FragmentScannerBinding
import com.nxlinkstar.stargrader.databinding.FragmentScannerScannedListItemBinding
import com.nxlinkstar.stargrader.utils.ImageFileUtil
import com.nxlinkstar.stargrader.utils.YuvUtils
//import com.serenegiant.usb.IFrameCallback
//import com.serenegiant.usb.USBMonitor
//import com.serenegiant.usb.UVCCameraUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream

class ScannerFragment : CameraFragment(), CaptureMediaView.OnViewClickListener {

    companion object {
        fun newInstance() = ScannerFragment()
    }

    private val viewModel: ScannerViewModel by viewModels<ScannerViewModel>()

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!


    private lateinit var sideSheetDialog: SideSheetDialog

//    private var mUSBMonitor: USBMonitor? = null

    // TODO: get resolution size from settings
    private var width = 640
    private var height = 480


    private var isSave = false

    private fun intiCamera() {
//        mUSBMonitor =
//            UVCCameraUtil.initUSBMonitor(
//                requireActivity(),
//                object : UVCCameraUtil.OnMyDevConnectListener {
//                    override fun onConnectDev(
//                        device: UsbDevice,
//                        ctrlBlock: USBMonitor.UsbControlBlock
//                    ) {
//                        val pid = String.format("%x", device.productId)
//                        Toast.makeText(requireContext(), "Connected: $pid", Toast.LENGTH_SHORT).show()
////                        LogUtils.d("pid", "connect: $pid")
////                        openRgb(pid, ctrlBlock)
//                        openHeightCamera(pid, ctrlBlock)
////                        isOpen()
//
//
//
//                    }
//
//                })
    }

//    private fun openHeightCamera(
//        pid: String,
//        ctrlBlock: USBMonitor.UsbControlBlock
//    ) {
//        UVCCameraUtil.openHeightCamera(
//            pid,
//            4,
//            width,
//            height,
//            binding.cameraView,
//            ctrlBlock,
//            IFrameCallback {
//
//            }
//        )
//
//    }


//    private fun openRgb(pid: String, ctrlBlock: USBMonitor.UsbControlBlock) {
//        UVCCameraUtil.openRGBCamera(
//            pid,
//            4,
//            width,
//            height,
//            binding.cameraView,
//            ctrlBlock,
//            object : UVCCameraUtil.FrameDataCallBack {
//                override fun onFrame(data: ByteArray) {
////                    isLook(data)
//                    saveBitmap(data)
//                }
//            })
//    }

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
    ): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)


        sideSheetDialog = SideSheetDialog(requireContext());
        sideSheetDialog.setContentView(R.layout.scanner_side_sheet)


        val recyclerView = binding.scannedList
        val adapter =  ScannedItemAdapter()
        recyclerView.adapter =  adapter
        viewModel.scannedList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        return binding.root
    }

    override fun getCameraView(): IAspectRatio? {
        return AspectRatioTextureView(requireContext())
    }

    override fun getCameraViewContainer(): ViewGroup? {
        return _binding?.cameraViewContainer
    }

    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View? {
        if (_binding == null) {
            _binding = FragmentScannerBinding.inflate(inflater, container, false)
        }
        return _binding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun onCameraState(self: MultiCameraClient.ICamera,
                               code: ICameraStateCallBack.State,
                               msg: String?) {
        ToastUtils.show("onCameraState: $code")
        when (code) {
            ICameraStateCallBack.State.OPENED -> handleCameraOpened()
            ICameraStateCallBack.State.CLOSED -> handleCameraClosed()
            ICameraStateCallBack.State.ERROR -> handleCameraError(null)
        }
    }

    private fun handleCameraError(msg: String?) {
//        mViewBinding.uvcLogoIv.visibility = View.VISIBLE
//        mViewBinding.frameRateTv.visibility = View.GONE
        ToastUtils.show("camera opened error: $msg")
    }

    private fun handleCameraClosed() {
//        mViewBinding.uvcLogoIv.visibility = View.VISIBLE
//        mViewBinding.frameRateTv.visibility = View.GONE
        ToastUtils.show("camera closed success")
    }

    private fun handleCameraOpened() {
//        binding.uvcLogoIv.visibility = View.GONE
//        binding.frameRateTv.visibility = View.VISIBLE
//        binding.brightnessSb.max = (getCurrentCamera() as? CameraUVC)?.getBrightnessMax() ?: 100
//        binding.brightnessSb.progress = (getCurrentCamera() as? CameraUVC)?.getBrightness() ?: 0
//        Logger.i(TAG, "max = ${mViewBinding.brightnessSb.max}, progress = ${mViewBinding.brightnessSb.progress}")
//        binding.brightnessSb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                (getCurrentCamera() as? CameraUVC)?.setBrightness(progress)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//
//            }
//        })
//        ToastUtils.show("camera opened success")

        getCurrentCamera()?.addPreviewDataCallBack( object : IPreviewDataCallBack {
            override fun onPreviewData(
                data: ByteArray?,
                width: Int,
                height: Int,
                format: IPreviewDataCallBack.DataFormat
            ) {
                Log.i("scanner", "onPreviewData")
//                ToastUtils.show("onPreviewData: $width, $height, " + data?.size )
            }
        })



    }

    override fun getGravity(): Int = Gravity.TOP


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.subjectState.observe(viewLifecycleOwner, Observer {
            binding.scanTarget.subject.text = it?.label ?: ""
        })
        viewModel.textbookState.observe(viewLifecycleOwner, Observer {
            binding.scanTarget.textbook.text = it?.name ?: ""
        })
        viewModel.workbookState.observe(viewLifecycleOwner, Observer {
            binding.scanTarget.workbook.text = it?.name ?: ""
        })



        binding.scanTarget.cellSubject.setOnClickListener {
            val list = viewModel.subjects.map {
                PickerFragment.Item(it.label, it.code)
            }

            val dialog = PickerFragment.newInstance(list, "选择科目")
            dialog.setOnItemSelectedListener(object : PickerFragment.OnItemSelectedListener {
                override fun onItemSelectedListener(position: Int) {
                    val item = list[position]
                    viewModel.subjectChanged(item.value)
                }

            })
            fragmentManager?.let { it1 -> dialog.show(it1, "SUBJECT_DIALOG") }


            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + File.separator + "test.png";
            captureImage(object : ICaptureCallBack {
                override fun onBegin() {

                }

                override fun onError(error: String?) {
                    ToastUtils.show(error ?: "未知异常")
                }

                override fun onComplete(path: String?) {
                    path?.let { ToastUtils.show(it) }
                }
            }, path)

            val allPreviewSizes = getAllPreviewSizes()

            writeTextToDcimDirectory("sizes",  allPreviewSizes.toString())
        }

        binding.scanTarget.cellTextbook.setOnClickListener {
            if (viewModel.textbooksState.value == null) {
                return@setOnClickListener
            }

            val list = viewModel.textbooksState.value!!.map {
                PickerFragment.Item(it.name, it.id)
            }

            val dialog = PickerFragment.newInstance(list, "选择教材")
            dialog.setOnItemSelectedListener(object : PickerFragment.OnItemSelectedListener {
                override fun onItemSelectedListener(position: Int) {
                    val item = list[position]
                    viewModel.textbookChanged(item.value)
                }

            })
            fragmentManager?.let { it1 -> dialog.show(it1, "TEXTBOOK_DIALOG") }
        }

        binding.scanTarget.cellWorkbook.setOnClickListener {
            if (viewModel.workbooksState.value == null) {
                return@setOnClickListener
            }

            val list = viewModel.workbooksState.value!!.map {
                PickerFragment.Item(it.name, it.id)
            }

            val dialog = PickerFragment.newInstance(list, "选择练习册")
            dialog.setOnItemSelectedListener(object : PickerFragment.OnItemSelectedListener {
                override fun onItemSelectedListener(position: Int) {
                    val item = list[position]
                    viewModel.workbookChanged(item.value)
                }

            })
            fragmentManager?.let { it1 -> dialog.show(it1, "WORKBOOK_DIALOG") }
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

        intiCamera()

    }

    fun writeTextToDcimDirectory(fileName: String, content: String) {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath;

//        val dcimDir = File(path, "MyApp")

        val txt = File(path, "$fileName.txt")

        try {
            FileOutputStream(txt).use { fileOutputStream ->
                BufferedWriter(fileOutputStream.writer()).use { bufferedWriter ->
                    bufferedWriter.write(content)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    @Suppress("DEPRECATION")
//    @Deprecated("Deprecated in Java")
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        inflater.inflate(R.menu.menu_scanner, menu)
//    }

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

//        filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED")

//        mUSBMonitor?.register()
////        UVCCameraUtil.requestRGBCameraPermission(requireActivity(), 300, mUSBMonitor!!)
//
//        val requestId = UVCCameraUtil.requestHeightCameraPermission(requireActivity(), 300, mUSBMonitor!!)
//        if (TextUtils.isEmpty(requestId)) {
//            val hint = "高拍仪没有找到或高拍仪没有加载成功"
//            Toast.makeText(requireContext(), hint, Toast.LENGTH_SHORT).show()
//        }
    }

    override fun onStop() {
        super.onStop()
//        LogUtils.d(TAG, "onStop")
//        UVCCameraUtil.releaseRGBCamera()
//        UVCCameraUtil.releaseHighCamera()
//        mUSBMonitor?.unregister()
    }

    class ScannedItemAdapter :
        ListAdapter<String, ScannedItemViewHolder>(object : DiffUtil.ItemCallback<String>() {

            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedItemViewHolder {
            val binding = FragmentScannerScannedListItemBinding.inflate(LayoutInflater.from(parent.context))
            return ScannedItemViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ScannedItemViewHolder, position: Int) {
            holder.textview.text = getItem(position)
        }

    }

    class ScannedItemViewHolder(binding: FragmentScannerScannedListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

            val textview = binding.textview
    }

    override fun onViewClick(mode: CaptureMediaView.CaptureMode?) {
        TODO("Not yet implemented")
    }


}

