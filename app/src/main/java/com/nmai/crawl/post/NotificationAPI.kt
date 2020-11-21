package com.nmai.crawl.post

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NotificationAPI(
    @SerializedName("app_bundle")
    val appBundle: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String
): Serializable