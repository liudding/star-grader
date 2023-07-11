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
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.sidesheet.SideSheetDialog
import com.nxlinkstar.stargrader.ItemListDialogFragment
import com.nxlinkstar.stargrader.R
import com.nxlinkstar.stargrader.data.SUBJECTS
import com.nxlinkstar.stargrader.databinding.FragmentHomeBinding
import com.nxlinkstar.stargrader.databinding.FragmentScannerBinding
import com.nxlinkstar.stargrader.databinding.FragmentScannerScannedListItemBinding
import com.nxlinkstar.stargrader.databinding.ScanTargetBinding
import com.nxlinkstar.stargrader.ui.login.LoginViewModel
import com.nxlinkstar.stargrader.ui.login.LoginViewModelFactory
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

    private val viewModel: ScannerViewModel by viewModels<ScannerViewModel>()

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!


    private lateinit var sideSheetDialog: SideSheetDialog

    private var mUSBMonitor: USBMonitor? = null

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

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

//        intiCamera()

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

//        mUSBMonitor.register()
//        UVCCameraUtil.requestRGBCameraPermission(requireActivity(), 300, mUSBMonitor!!)
    }

    override fun onStop() {
        super.onStop()
//        LogUtils.d(TAG, "onStop")
//        UVCCameraUtil.releaseRGBCamera()
//        mUSBMonitor.unregister()
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


}