package com.alfred.kitabalhuda.network

import com.google.gson.annotations.SerializedName

data class DialogConfig(
    @SerializedName("dialogs") val dialogs: List<DialogDef>
)

data class DialogDef(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("buttons") val buttons: List<DialogButton>,
    @SerializedName("conditions") val conditions: DialogConditions?
)

data class DialogButton(
    @SerializedName("text") val text: String,
    @SerializedName("style") val style: String,
    @SerializedName("action") val action: DialogAction
)

data class DialogAction(
    @SerializedName("type") val type: String,
    @SerializedName("value") val value: String?
)

data class DialogConditions(
    @SerializedName("minVersionCode") val minVersionCode: Int?,
    @SerializedName("maxVersionCode") val maxVersionCode: Int?,
    @SerializedName("startDate") val startDate: String?,
    @SerializedName("endDate") val endDate: String?,
    @SerializedName("showOnce") val showOnce: Boolean = false
)
