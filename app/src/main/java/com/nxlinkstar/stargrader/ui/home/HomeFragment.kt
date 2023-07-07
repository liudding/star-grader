package com.nxlinkstar.stargrader.ui.home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.nxlinkstar.stargrader.R
import com.nxlinkstar.stargrader.data.LoginDataSource
import com.nxlinkstar.stargrader.data.LoginRepository
import com.nxlinkstar.stargrader.databinding.FragmentHomeBinding
import com.nxlinkstar.stargrader.ui.login.LoginFragment
import com.nxlinkstar.stargrader.ui.login.LoginViewModel
import com.nxlinkstar.stargrader.ui.login.LoginViewModelFactory

class HomeFragment : Fragment() {

    companion object {

    }

    private lateinit var viewModel: HomeViewModel

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by activityViewModels<LoginViewModel>(factoryProducer = { LoginViewModelFactory() })

    private val loginRepository = LoginRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navController = findNavController()

//        val currentBackStackEntry = navController.currentBackStackEntry!!
//        val savedStateHandle = currentBackStackEntry.savedStateHandle
//        savedStateHandle.getLiveData<Boolean>(LoginFragment.LOGIN_SUCCESSFUL)
//            .observe(currentBackStackEntry, Observer { success ->
//                if (!success) {
//                    val startDestination = navController.graph.startDestination
//                    val navOptions = NavOptions.Builder()
//                        .setPopUpTo(startDestination, true)
//                        .build()
//                    navController.navigate(startDestination, null, navOptions)
//                }
//            })
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (!loginRepository.isLoggedIn) {
            val navController = findNavController()
            navController.navigate(R.id.LoginFragment)
        }


//        loginViewModel.loginResult.observe(viewLifecycleOwner, Observer { result ->
//            result ?: return@Observer
//
//            result.success?.let {
////                updateUiWithUser(it)
//            }
//
//            if (result != null) {
////                showWelcomeMessage()
//            } else {
////
//            }
//        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonToScanner.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_ScannerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}