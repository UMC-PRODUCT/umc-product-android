package com.umc.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseActivity<B : ViewDataBinding, STATE : UiState, EVENT : UiEvent, VM : BaseViewModel<STATE, EVENT>>(
    private val inflater: (LayoutInflater) -> B,
) : AppCompatActivity() {
    protected lateinit var binding: B
    protected abstract val viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = inflater(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this

        initView()
        initState()
    }

    protected abstract fun initView()

    protected abstract fun initState()

    protected fun LifecycleOwner.repeatOnStarted(block: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED, block)
        }
    }
}
