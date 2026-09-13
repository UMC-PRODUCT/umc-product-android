package com.umc.presentation.remotenotice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.remoteconfig.RemoteNotice
import com.umc.domain.usecase.remoteconfig.GetRemoteNoticesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 원격 설정의 화면별 안내를 받아두고, 이미 닫은 안내를 기억한다
 */
@HiltViewModel
class RemoteNoticeViewModel @Inject constructor(
    private val getRemoteNoticesUseCase: GetRemoteNoticesUseCase,
) : ViewModel() {

    private val _notices = MutableStateFlow<List<RemoteNotice>>(emptyList())
    val notices: StateFlow<List<RemoteNotice>> = _notices.asStateFlow()

    // 이번 실행 중에 닫은 안내. 같은 화면에 다시 들어와도 또 띄우지 않는다
    private val _dismissed = MutableStateFlow<Set<String>>(emptySet())
    val dismissed: StateFlow<Set<String>> = _dismissed.asStateFlow()

    /**
     * 앱이 화면에 올라올 때마다 부른다
     *
     * 클라이언트 캐시 덕분에 10분 안에는 네트워크를 쓰지 않는다.
     * 받아오지 못하면 마지막으로 받은 목록을 그대로 둔다.
     */
    fun refresh() {
        viewModelScope.launch {
            val result = getRemoteNoticesUseCase()
            if (result is ApiState.Success) {
                _notices.value = result.data
            }
        }
    }

    fun dismiss(notice: RemoteNotice) {
        _dismissed.update { it + notice.key }
    }
}

/** 안내를 구분하는 값. 운영진이 문구를 고치면 다른 안내로 보고 다시 띄운다 */
internal val RemoteNotice.key: String
    get() = "$screen|$title|$body"

/**
 * route 문자열에서 경로 이름만 꺼낸다
 *
 * `com.umc.presentation.MainDestination.NoticeDetail/{noticeId}` → `NoticeDetail`
 *
 * 클래스 이름(`::class.simpleName`)은 release 빌드에서 R8 이 바꿔버리지만,
 * route 는 직렬화 이름에서 만들어지므로 난독화돼도 원래 이름이 남는다.
 */
internal fun String.toScreenName(): String =
    substringBefore('/').substringBefore('?').substringAfterLast('.')
