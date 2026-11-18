package com.umc.data.request.community

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.community.thread.CommunityMessageReportReason

data class ReportMessageRequest(@field:SerializedName("reasons") val reasons: List<CommunityMessageReportReason>)
