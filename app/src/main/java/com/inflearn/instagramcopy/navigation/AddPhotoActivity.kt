package com.inflearn.instagramcopy.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.inflearn.instagramcopy.R
import com.inflearn.instagramcopy.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {

    var PICK_IMAGE_FROM_ALBUM = 0

    var storage: FirebaseStorage? = null

    var photoUri: Uri? = null

    var auth: FirebaseAuth? = null

    var firestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

//        Initiate storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

//        Open the album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

//        add image upload event
        addPhoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
//                This is path to the selected image
                photoUri = data?.data
                addPhoto_image.setImageURI(photoUri)
            } else {
//                Exit the addPhotoActivity fi you leave the album without selecting it
                finish()
            }
        }
    }

    fun contentUpload() {
//        Make Filename
        var timestamp = SimpleDateFormat("yyyyMMdd_HH,,ss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

//        Promise Method
        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

//              Insert downloadUrl of image
            contentDTO.imageUrl = uri.toString()

//                Insert uid of user
            contentDTO.uid = auth?.currentUser?.uid

//                Insert userId
            contentDTO.userId = auth?.currentUser?.email

//                Insert explain of content
            contentDTO.explain = addPhoto_edit_explain.text.toString()

//                Insert timestamp
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK)

            finish()
        }

/* //        Callback Method
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var contentDTO = ContentDTO()

//                Insert downloadUrl of image
                contentDTO.imageUrl = uri.toString()

//                Insert uid of user
                contentDTO.uid = auth?.currentUser?.uid

//                Insert userId
                contentDTO.userId = auth?.currentUser?.email

//                Insert explain of content
                contentDTO.explain = addPhoto_edit_explain.text.toString()

//                Insert timestamp
                contentDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDTO)

                setResult(Activity.RESULT_OK)

                finish()
            }
        } */
    }

}