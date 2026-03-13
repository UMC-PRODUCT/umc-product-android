package com.umc.presentation.component

import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import com.umc.domain.model.mypage.UserActiveItem
import com.umc.presentation.databinding.ItemMypageActiveBinding

object LinearLayoutMypageBindingAdapter {
    @JvmStatic
    @BindingAdapter("myHistoryItems")
    fun setMyHistoryItems(view: LinearLayout, items: List<UserActiveItem>?) {
        // 1. 기존에 그려진 뷰 제거
        view.removeAllViews()
        if (items.isNullOrEmpty()) return

        items.forEach { history ->
            // 2. 데이터 바인딩 클래스 인플레이트
            val binding = ItemMypageActiveBinding.inflate(
                LayoutInflater.from(view.context), view, false
            )
            // 3. 데이터 주입
            binding.item = history
            // 4. 레이아웃에 추가
            view.addView(binding.root)
        }
    }
}