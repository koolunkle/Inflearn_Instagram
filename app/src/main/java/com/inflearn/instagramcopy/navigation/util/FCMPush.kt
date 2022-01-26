package com.inflearn.instagramcopy.navigation.util

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.inflearn.instagramcopy.navigation.model.PushDTO
import com.squareup.okhttp.*
import java.io.IOException

class FCMPush {
    var JSON = MediaType.parse("application/json; charset=utf-8")
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverKey =
        "AAAAn1uB3Vs:APA91bHkEFj7jn_5jKtZ7vxzCwc_PNDlqRm0vmuLCayHXFJeyVo9q5kMUzSapLcZlUUYJaI8qS3je8FbSqBteeGLl-3T8LtmNy-WMR2v_ILuMOheNaeP4dLFCJvDmxs_w036OmDyRuzF"
    var gson: Gson? = null
    var okHttpClient: OkHttpClient? = null

    companion object {
        var instance = FCMPush()
    }

    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

    fun sendMessage(destinationUid: String, title: String, message: String) {
        FirebaseFirestore.getInstance().collection("pushTokens").document(destinationUid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var token = task?.result?.get("pushToken").toString()
                    var pushDTO = PushDTO()
                    pushDTO.to = token
                    pushDTO.notification.title = title
                    pushDTO.notification.body = message

                    var body = RequestBody.create(JSON, gson?.toJson(pushDTO))
                    var request = Request.Builder().addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "key=" + serverKey).url(url).post(body).build()
                    okHttpClient?.newCall(request)?.enqueue(object : Callback {

                        override fun onResponse(response: Response?) {
                            println(response?.body()?.string())
                        }

                        override fun onFailure(request: Request?, e: IOException?) {

                        }
                    })
                }
            }
    }
}