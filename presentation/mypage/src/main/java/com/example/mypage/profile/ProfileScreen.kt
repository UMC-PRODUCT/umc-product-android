package com.example.mypage.profile

import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary500
import com.umc.domain.model.enums.UploadFileCategory
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = hiltViewModel(),
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    //텍스트 상태 추적
    var githubText by remember(uiState.githubLink) { mutableStateOf(uiState.githubLink) }
    var linkedinText by remember(uiState.linkedinLink) { mutableStateOf(uiState.linkedinLink) }
    var blogText by remember(uiState.blogLink) { mutableStateOf(uiState.blogLink) }

    //이미지 picker 세팅
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {

            //ContentResolver를 이용해 메타 정보 추출
            val fileSize =
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
            //fileType은 MINETYPE -> image/jpg 이런 형식
            val fileType = context.contentResolver.getType(uri) ?: ""
            //extenstion은 확장자 -> JPG
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType) ?: ""
            val category = UploadFileCategory.PROFILE_IMAGE

            when {
                category.alloweType.isNotEmpty() && !category.alloweType.contains(extension.lowercase()) -> {
                    Toast.makeText(context, "${category.label}에 허용되지 않는 형식입니다.", Toast.LENGTH_SHORT)
                        .show()
                }

                fileSize > category.maxSizeBytes -> {
                    val maxSizeMb = category.maxSizeBytes / (1024 * 1024)
                    Toast.makeText(
                        context,
                        "${category.label}은 최대 ${maxSizeMb}MB까지 가능합니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> viewModel.settingImage(uri)
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ProfileEvent.ClickProfileImage -> {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                is ProfileEvent.ClickComplete -> {
                    viewModel.saveUserOutLink(githubText, linkedinText, blogText)
                }
                is ProfileEvent.ClickBackPressed -> {
                    /**TODO: 차후 로직 처리*/
                }
                is ProfileEvent.MakeToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    ProfileScreen(
        uiState = uiState,
        githubText = githubText,
        onGithubChange = { githubText = it },
        linkedinText = linkedinText,
        onLinkedinChange = { linkedinText = it },
        blogText = blogText,
        onBlogChange = { blogText = it },
        onBackPressed = viewModel::onClickBackPressed,
        onCompleteClick = viewModel::onClickComplete,
        onProfileImageClick = viewModel::onClickProfileImage
    )

    
    
}


@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    githubText: String,
    onGithubChange: (String) -> Unit,
    linkedinText: String,
    onLinkedinChange: (String) -> Unit,
    blogText: String,
    onBlogChange: (String) -> Unit,
    onBackPressed: () -> Unit,
    onCompleteClick: () -> Unit,
    onProfileImageClick: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral000())
    ) {

        //상단 바
        ProfileTopbar(
            onBackClick = onBackPressed,
            onCompleteClick = onCompleteClick

        )

        //각 아이템 당 자동 24dp 패딩
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
        }

    }

}


/**상단 top bar**/
@Composable
fun ProfileTopbar(
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit
){

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                painter = painterResource(id=R.drawable.ic_back),
                contentDescription = null,
                tint = neutral800(),
                modifier = Modifier
                    .clickable { onBackClick() }
                    .padding(end = 16.dp)
                    .clip(CircleShape)
            )

            UText(
                text = AppStrings.MYPAGE_MODIFY_PROFILE,
                style = UmcTypographyTokens.Title2Bold,
                color = neutral800(),
                modifier = Modifier.padding(start = 16.dp)
            )
        }


        UText(
            text = AppStrings.COMPLETE,
            style = UmcTypographyTokens.CalloutBold,
            color = primary500(),
            modifier = Modifier
                .clickable { onCompleteClick() }
        )

    }
}