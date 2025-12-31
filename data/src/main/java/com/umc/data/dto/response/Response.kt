package com.stable.scoi.data.dto.response

import com.stable.scoi.domain.model.Model

interface Response {
    fun toModel() : Model
}