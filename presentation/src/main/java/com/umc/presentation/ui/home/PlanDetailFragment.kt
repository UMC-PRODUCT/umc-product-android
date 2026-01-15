package com.umc.presentation.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentPlanDetailBinding
import com.umc.presentation.util.UToast
import java.net.URLEncoder

class PlanDetailFragment : BaseFragment<FragmentPlanDetailBinding, PlanDetailFragmentUiState, PlanDetailFragmentEvent, PlanDetailViewModel>(
    FragmentPlanDetailBinding::inflate,
) {
    override val viewModel: PlanDetailViewModel by viewModels()

    override fun initView() {
        binding.apply {
            vm = viewModel

            plandetailTvLocationMap.setOnClickListener {
                val address = plandetailTvLocationPlace.text.toString()
                openNaverMap(address)
            }

        }

    }

    override fun initStates() {
        super.initStates()
    }


    private fun handleMoveEvent(event: PlanDetailFragmentEvent){
        when (event){
            is PlanDetailFragmentEvent.TouchConfirmAttention -> clickConfirmAttention()

            else -> {}
        }
    }

    //네이버 지도 or 웹 열기
    private fun openNaverMap(address: String) {
        try {
            
            // 입력한 주소를 URL 형태로 인코딩하기
            val encodedAddress = URLEncoder.encode(address, "UTF-8")

            // 네이버 지도 앱에 접근을 시도해보기
            val appUri = Uri.parse("nmap://search?query=$encodedAddress&appname=${requireContext().packageName}")
            val appIntent = Intent(Intent.ACTION_VIEW, appUri)

            // 앱이 설치되어 있는지 확인 (Manifest의 <queries> 설정)
            // 앱이 있으면 intent로 보내주기
            if (appIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(appIntent)
            } else {
                // 앱이 없으면 웹 브라우저용 URL 실행
                val webUrl = "https://m.map.naver.com/search2/search.naver?query=$encodedAddress"
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                startActivity(webIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            //UToast.createToast(requireContext(), "지도를 열 수 없습니다.", )
            Toast.makeText(requireContext(), "지도를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    //해당 탭을 닫고 이동하는 로직
    /**TODO 로직 작성 필요**/
    private fun clickConfirmAttention(){

    }


}