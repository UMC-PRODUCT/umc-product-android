package com.umc.data.dto.response

import com.umc.domain.model.Model

interface Response {
    fun toModel(): Model
}
