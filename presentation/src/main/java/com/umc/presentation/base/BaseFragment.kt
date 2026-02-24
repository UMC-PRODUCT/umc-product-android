package com.umc.presentation.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.umc.presentation.component.ULoadingDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseFragment<B : ViewDataBinding, STATE : UiState, EVENT : UiEvent, VM : BaseViewModel<STATE, EVENT>>(
    private val inflater: (LayoutInflater, ViewGroup?, Boolean) -> B,
) : Fragment() {
    private var backPressedOnce = false
    private lateinit var navController: NavController
    protected abstract val viewModel: VM

    private var _binding: B? = null
    protected val binding
        get() = _binding!!

    private var loadingDialog: ULoadingDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = inflater(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        navController = findNavController()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
        initView()
        initStates()
        initLoadingDialog()
    }

    private fun initLoadingDialog() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    showLoadingDialog()
                } else {
                    dismissLoadingDialog()
                }
            }
        }
    }

    private fun showLoadingDialog() {
        if (loadingDialog == null || loadingDialog?.isAdded == false) {
            loadingDialog = ULoadingDialog.newInstance()
            loadingDialog?.show(parentFragmentManager, ULoadingDialog.TAG)
        }
    }

    private fun dismissLoadingDialog() {
        loadingDialog?.let {
            if (it.isAdded) {
                it.dismiss()
            }
        }
        loadingDialog = null
    }

    private val onBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id != navController.graph.startDestinationId) {
                    navController.popBackStack()
                    return
                }
                if (backPressedOnce) {
                    requireActivity().finish()
                    return
                }
                backPressedOnce = true
                Handler(Looper.getMainLooper()).postDelayed({ backPressedOnce = false }, 2000)
            }
        }

    protected abstract fun initView()

    protected open fun initStates() {
        repeatOnStarted(viewLifecycleOwner) {
            // TODO 공통으로 처리할 로직
        }
    }

    protected open fun handleEvent(event: EVENT) {}

    protected fun LifecycleOwner.repeatOnStarted(
        viewLifecycleOwner: LifecycleOwner,
        block: suspend CoroutineScope.() -> Unit,
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED, block)
        }
    }

    override fun onDestroyView() {
        loadingDialog?.dismiss()
        loadingDialog = null
        _binding = null
        super.onDestroyView()
    }
}
