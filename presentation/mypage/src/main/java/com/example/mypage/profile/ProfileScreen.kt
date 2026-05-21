package com.example.mypage.profile

import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mypage.mypage.SocialBadge
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral700
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary100
import com.umc.component.theme.primary500
import com.umc.domain.model.enums.LoginType
import com.umc.domain.model.enums.UploadFileCategory
import com.umc.domain.model.mypage.UserActiveItem
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
            //프로필 이미지
            item {
                ProfileImageSection(
                    imageUri = uiState.userProfileImageUri,
                    defaultUrl = uiState.userInfo.profileImageLink,
                    onImageClick = onProfileImageClick
                )
            }

            //연동 정보 (고정형)
            item {
                ProfileInfoSection(
                    title = "연동된 계정",
                    content = "로그인 제공자",
                    platforms = uiState.linkedPlatforms
                )
            }

            //이름/닉네임 (고정형)
            item {
                ProfileInfoSection(
                    title = "닉네임/이름",
                    content = "${uiState.userInfo.nickname} / ${uiState.userInfo.name}"
                )
            }

            //학교 정보 (고정형)
            item {
                ProfileInfoSection(
                    title = "학교",
                    content = uiState.userInfo.schoolName
                )
            }

            //활동 이력 리스트
            item {
                UText(text = "활동 이력", style = UmcTypographyTokens.HeadlineBold)
                Card(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, neutral300()),
                    colors = CardDefaults.cardColors(containerColor = neutral000())
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                    ) {
                        uiState.myActiveHistory.forEach { history ->
                            ActiveHistoryItem(history)
                        }
                    }
                }
            }

            // 대외 링크 입력 (UTextField 적용)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    LinkInputSection("GitHub URL", githubText, onGithubChange)
                    LinkInputSection("LinkedIn URL", linkedinText, onLinkedinChange)
                    LinkInputSection("블로그 URL", blogText, onBlogChange)
                }
            }

            item {
                Spacer(
                    modifier = Modifier
                        .height(64.dp)
                )
            }
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

/**프로필 이미지 섹션 부분**/
@Composable
fun ProfileImageSection(imageUri: Uri, defaultUrl: String, onImageClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        //이미지
        Box(
            modifier = Modifier
                .size(120.dp)
                .clickable { onImageClick() }
        ) {
            AsyncImage(


                model = if (imageUri != Uri.EMPTY) imageUri
                        else if(defaultUrl != "") defaultUrl
                        else R.drawable.ic_profile_default,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(1.dp, neutral200(), CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_profile_default)
            )
            //카메라 아이콘 배지
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd),
                shape = CircleShape,
                color = neutral000(),
                border = BorderStroke(1.dp, neutral200())
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp),
                    tint = neutral800()
                )
            }
        }
    }
}

/**각 정보 섹션(고정된 부분)**/
@Composable
fun ProfileInfoSection(title: String, content: String, platforms: List<LoginType> = emptyList()) {
    Column {
        UText(text = title, style = UmcTypographyTokens.HeadlineBold)
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .background(neutral100(), RoundedCornerShape(8.dp))
                .border(1.dp, neutral300(), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UText(
                    text = content, 
                    style = UmcTypographyTokens.Callout, 
                    color = neutral600(), 
                    modifier = Modifier.weight(1f))
                // 소셜 배지 표시 로직(SocialBadge = Mypage꺼 재사용)
                platforms.forEach { platform ->
                    SocialBadge(platform = platform) 
                }
            }
        }
    }
}

/**내 활동 섹션(여러 개)**/
@Composable
fun ActiveHistoryItem(
    history: UserActiveItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //1. 기수 정보 (ex. 10기)
        UButton(
            text = history.generation,
            onClick = {},
            modifier = Modifier
                .height(24.dp),
            backgroundColor = neutral000(),
            borderColor = neutral200(),
            borderWidth = 1.dp,
            textColor = neutral600(),
            textStyle = UmcTypographyTokens.FootnoteBold
        )


        Spacer(
            modifier = Modifier
            .width(16.dp)
        )


        // 2. 활동 내용 (ex. Android Part 챌린저)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            UText(
                text = history.partName, // "Android Part 챌린저" 등 합성된 문자열
                style = UmcTypographyTokens.Subheadline,
                color = neutral600(),
                modifier = Modifier
                    .weight(1f)
            )

            UButton(
                text = history.position,
                onClick = {},
                modifier = Modifier
                    .height(24.dp),
                backgroundColor = primary100(),
                borderWidth = 0.dp,
                textColor = primary500(),
                textStyle = UmcTypographyTokens.FootnoteBold
            )
        }



    }
}


/**링크 넣는 섹션(입력 가능)**/
@Composable
fun LinkInputSection(title: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        UText(
            text = title,
            style = UmcTypographyTokens.HeadlineBold
        )
        UTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "http://-",
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        )
    }
}