package com.umc.presentation.ui.home

import android.view.LayoutInflater
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.flexbox.FlexboxLayout
import com.umc.presentation.BR


//FlexBoxLayout을 사용할 때, app:tagItems 및 app:tagLayoutId를 통해
//데이터 바인딩을 통해, 설정한 레이아웃을 추가할 수 있다.
/**
 * FlexBoxLayout 사용 시 app:tagItems와 app:tagLayoutId 속성을 추가해 사용
 * 대상 레이아웃의 <data> 섹션에 'tagText'라는 이름의 String 변수가 정의되어 있어야 합니다.
 * 사용 시, 필수적으로 tagItems와 tagLayoutId 속성을 추가해야 합니다.
 * onTagClick()으로 클릭 리스너를 정의할 수 있습니다.
 *
 * **/

object HomeTagBindingAdapters {
    @JvmStatic
    @BindingAdapter(value = ["app:tagItems", "app:tagLayoutId", "app:onTagClick"],
        requireAll = false)
    fun setTagItems(
        flexboxLayout: FlexboxLayout,
        items: List<String>?,
        layoutId: Int,
        onTagClick: (() -> Unit)?
    ) {
        // 기존에 추가된 뷰들 제거 (데이터 갱신 시 중복 방지)
        flexboxLayout.removeAllViews()
        if (items.isNullOrEmpty()) return

        items.forEach { tagText ->
            // DataBinding을 사용하여 item을 인플레이트
            val binding: ViewDataBinding = DataBindingUtil.inflate(
                LayoutInflater.from(flexboxLayout.context),
                layoutId,
                flexboxLayout,
                false
            )

            // 바인딩 데이터 및 속성 설정
            binding.setVariable(BR.tagText, tagText)
            binding.executePendingBindings()

            // FlexboxLayout에 추가
            flexboxLayout.addView(binding.root)
        }
    }
}