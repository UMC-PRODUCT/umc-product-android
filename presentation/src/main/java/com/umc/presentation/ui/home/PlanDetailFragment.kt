package com.umc.presentation.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentPlanDetailBinding
import com.umc.presentation.util.UToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.net.URLEncoder

@AndroidEntryPoint

class PlanDetailFragment : BaseFragment<FragmentPlanDetailBinding, PlanDetailFragmentUiState, PlanDetailFragmentEvent, PlanDetailViewModel>(
    FragmentPlanDetailBinding::inflate,
) {
    override val viewModel: PlanDetailViewModel by viewModels()

    //인자로 받은 ID
    private val args: PlanDetailFragmentArgs by navArgs()

    override fun initView() {
        binding.apply {
            vm = viewModel

            plandetailTvLocationMap.setOnClickListener {
                val address = plandetailTvLocationPlace.text.toString()

                openNaverMap(address)
            }

        }

        //일정 화면에서 일정 ID 가져오기
        val scheduleId = args.scheduleId
        //일정 화면에서 plusDay 가져오기 (02.06-02.08 일정에 대해 plusDay == 1 이면 02.07에 대한 출력)
        val plusDay = args.plusDay
        if (scheduleId != -1L) {
            viewModel.getScheduleDetail(scheduleId, plusDay)
        }


    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner){
            launch {
                viewModel.uiState.collect { state ->
                    handleKebabAnimation(state.isMenuVisible)
                }
            }

            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }


    //실질적인 로직 수행 이벤트 맞춰서
    override fun handleEvent(event: PlanDetailFragmentEvent){
        when (event){
            is PlanDetailFragmentEvent.TouchConfirmAttention -> clickConfirmAttention()

            is PlanDetailFragmentEvent.MoveBackPressedEvent -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            else -> {}
        }
    }

    //탭 애니메이션으로 쫘라락
    private fun handleKebabAnimation(isVisible: Boolean) {
        binding.plandetailLayoutKebabMenu.apply {
            if (isVisible) {
                //위에서 아래로 내려오며 나타남
                visibility = View.VISIBLE
                alpha = 0f
                translationY = -30f
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(250L)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .start()
            } else {
                //위로 올라가며 사라짐
                animate()
                    .alpha(0f)
                    .translationY(-30f)
                    .setDuration(200L)
                    .withEndAction { visibility = View.GONE }
                    .start()
            }
        }
    }



    //네이버 지도 or 웹 열기
    private fun openNaverMap(placeName: String) {
        try {

            val latitude = viewModel.uiState.value.latitude
            val longitude = viewModel.uiState.value.longitude

            // 네이버 지도 앱에 접근을 시도해보기
            val appUri = Uri.parse("nmap://map?lat=$latitude&lng=$longitude&zoom=15&title=${URLEncoder.encode(placeName, "UTF-8")}&appname=${requireContext().packageName}")
            val appIntent = Intent(Intent.ACTION_VIEW, appUri)

            // 앱이 설치되어 있는지 확인 (Manifest의 <queries> 설정)
            // 앱이 있으면 intent로 보내주기
            if (appIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(appIntent)
            } else {
                // 앱이 없으면 웹 브라우저용 URL 실행
                val webUrl = "https://m.map.naver.com/map.naver?pinId=&pinType=site&lat=$latitude&lng=$longitude&dlevel=11&enc=utf8"
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