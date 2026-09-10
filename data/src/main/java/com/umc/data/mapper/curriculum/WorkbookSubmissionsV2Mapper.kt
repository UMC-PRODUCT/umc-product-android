package com.umc.data.mapper.curriculum

import com.umc.data.response.curriculum.StudyMemberSubmissionResponse
import com.umc.data.response.curriculum.StudyMemberSubmissionWeekResponse
import com.umc.data.response.curriculum.WorkbookSubmissionsV2Response
import com.umc.domain.model.curriculum.StudyMemberSubmission
import com.umc.domain.model.curriculum.StudyMemberSubmissionPage
import com.umc.domain.model.curriculum.StudyMemberSubmissionWeek
import com.umc.domain.model.enums.UserPart

fun WorkbookSubmissionsV2Response.toDomain(): StudyMemberSubmissionPage {
    return StudyMemberSubmissionPage(
        content = content.map { submission ->
            submission.toDomain()
        },
        nextCursor = nextCursor,
        hasNext = hasNext,
    )
}

fun StudyMemberSubmissionResponse.toDomain(): StudyMemberSubmission {
    return StudyMemberSubmission(
        studyGroupMemberId = studyGroupMemberId,
        memberId = memberId,
        memberName = memberName,
        nickname = nickname,
        schoolName = schoolName,
        profileImageUrl = profileImageUrl,
        studyGroupId = studyGroupId,
        studyGroupName = studyGroupName,
        part = UserPart.resolve(track, part),
        weeks = weeks.map { week ->
            week.toDomain()
        },
    )
}

fun StudyMemberSubmissionWeekResponse.toDomain():
        StudyMemberSubmissionWeek {
    return StudyMemberSubmissionWeek(
        weekNo = weekNo,
        weeklyCurriculumId = weeklyCurriculumId,
        challengerWorkbookId = challengerWorkbookId,
        status = status,
        isBest = isBest,
        content = content,
    )
}