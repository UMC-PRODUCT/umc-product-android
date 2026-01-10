package com.umc.presentation.ui.home

import android.view.LayoutInflater
import androidx.databinding.BindingAdapter
import com.google.android.flexbox.FlexboxLayout
import com.umc.presentation.databinding.ItemHomeTagBinding

object HomeTagBindingAdapters {
    @JvmStatic
    @BindingAdapter("app:tagItems", requireAll = false)
    fun setTagItems(
        flexboxLayout: FlexboxLayout,
        items: List<String>?,
    ) {
        // 기존에 추가된 뷰들 제거 (데이터 갱신 시 중복 방지)
        flexboxLayout.removeAllViews()

        items?.forEach { tagText ->
            // DataBinding을 사용하여 item_tag.xml 인플레이트
            val binding = ItemHomeTagBinding.inflate(
                LayoutInflater.from(flexboxLayout.context),
                flexboxLayout,
                false
            )

            // UButton 데이터 및 속성 설정
            binding.itemHomeTag.setText(tagText)

            // FlexboxLayout에 추가
            flexboxLayout.addView(binding.root)
        }
    }
}