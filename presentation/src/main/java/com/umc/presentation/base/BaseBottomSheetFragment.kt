package com.umc.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseBottomSheetFragment<B : ViewDataBinding, STATE : UiState, EVENT : UiEvent, VM : BaseViewModel<STATE, EVENT>>(
    private val inflater: (LayoutInflater, ViewGroup?, Boolean) -> B,
) : BottomSheetDialogFragment() {

    protected abstract val viewModel: VM

    private var _binding: B? = null
    protected val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = inflater(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        initView()
        initStates()
    }

    protected abstract fun initView()

    protected open fun initStates() {
        repeatOnStarted(viewLifecycleOwner) {

        }
    }

    protected fun LifecycleOwner.repeatOnStarted(viewLifecycleOwner: LifecycleOwner, block: suspend CoroutineScope.() -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED, block)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}