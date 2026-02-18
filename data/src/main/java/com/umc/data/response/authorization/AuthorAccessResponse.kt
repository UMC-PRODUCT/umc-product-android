package com.umc.data.response.authorization

import com.google.gson.annotations.SerializedName
import com.umc.data.response.authorization.PermissionItemResponse.Companion.toModel
import com.umc.domain.model.AuthorAccess
import com.umc.domain.model.PermissionItem
import com.umc.domain.model.enums.PermissionType
import com.umc.domain.model.enums.ResourceType

data class AuthorAccessResponse (
    @SerializedName("resourceType")
    val resourceType: String,
    @SerializedName("resourceId")
    val resourceId: Long,
    @SerializedName("permissions")
    val permissions: List<PermissionItemResponse>
) {
    companion object {
        fun AuthorAccessResponse.toModel(): AuthorAccess {
            return AuthorAccess(
                resourceType = runCatching { ResourceType.valueOf(this.resourceType) }
                    .getOrDefault(ResourceType.SCHEDULE),
                resourceId = this.resourceId,
                permissions = this.permissions.map { it.toModel() }
            )
        }
    }
}

data class PermissionItemResponse(
    @SerializedName("permissionType")
    val permissionType: String,
    @SerializedName("hasPermission")
    val hasPermission: Boolean
) {
    companion object {
        fun PermissionItemResponse.toModel(): PermissionItem {
            return PermissionItem(
                type = runCatching { PermissionType.valueOf(this.permissionType) }
                    .getOrDefault(PermissionType.READ),
                hasPermission = this.hasPermission
            )
        }
    }
}