package com.inflearn.instagramcopy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.inflearn.instagramcopy.navigation.CommentActivity
import com.inflearn.instagramcopy.navigation.model.AlarmDTO
import com.inflearn.instagramcopy.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.fragment_detail_view.view.*
import kotlinx.android.synthetic.main.item_detail_view.view.*

class DetailViewFragment : Fragment() {

    var firestore: FirebaseFirestore? = null

    var uid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =
            LayoutInflater.from(activity).inflate(R.layout.fragment_detail_view, container, false)

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.detailViewFragment_recyclerView.adapter = DetailViewRecyclerViewAdapter()
        view.detailViewFragment_recyclerView.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidList.clear()
//                    Sometimes, This code return null of querySnapshot when it sign out
                    if (querySnapshot == null) return@addSnapshotListener
                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        if (item != null) {
                            contentDTOs.add(item)
                        }
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_detail_view, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            var viewholder = (holder as CustomViewHolder).itemView

//            UserId
            viewholder.detailViewItem_profile_textView.text = contentDTOs!![position].userId

//            Image
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl)
                .into(viewholder.detailViewItem_ImageView_content)

//            Explain of content
            viewholder.detailViewItem_explain_textView.text = contentDTOs!![position].explain

//            Likes
            viewholder.detailViewItem_favoriteCounter_textView.text =
                "Likes " + contentDTOs!![position].favoriteCount

//            Profile Image
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl)
                .into(viewholder.detailViewItem_profile_imageView)

//            This code is when the button is clicked
            viewholder.detailViewItem_favorite_imageView.setOnClickListener {
                favoriteEvent(position)
            }

//            This code is when the page is loaded
            if (contentDTOs!![position].favorites.containsKey(uid)) {
//                This is like status
                viewholder.detailViewItem_favorite_imageView.setImageResource(R.drawable.ic_favorite)
            } else {
//                This is unlike status
                viewholder.detailViewItem_favorite_imageView.setImageResource(R.drawable.ic_favorite_border)
            }

//            This code is when the profile image is clicked
            viewholder.detailViewItem_profile_imageView.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.main_content, fragment)?.commit()
            }

            viewholder.detailViewItem_comment_imageView.setOnClickListener { holder ->
                var intent = Intent(holder.context, CommentActivity::class.java)
                intent.putExtra("contentUid", contentUidList[position])
                intent.putExtra("destinationUid", contentDTOs[position].uid)
                startActivity(intent)
            }

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        fun favoriteEvent(position: Int) {
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->

                var uid = FirebaseAuth.getInstance().currentUser?.uid
                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if (contentDTO!!.favorites.containsKey(uid)) {
//                    When the button is clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid)
                    favoriteAlarm(contentDTOs[position].uid!!)
                } else {
//                    When the button is not clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                    contentDTO.favorites[uid!!] = true
                }
                transaction.set(tsDoc, contentDTO)
            }
        }

        fun favoriteAlarm(destinationUid: String) {
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        }

    }

}