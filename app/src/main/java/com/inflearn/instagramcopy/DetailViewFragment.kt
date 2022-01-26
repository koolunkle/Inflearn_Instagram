package com.inflearn.instagramcopy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.inflearn.instagramcopy.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.fragment_detail_view.view.*
import kotlinx.android.synthetic.main.item_detail_view.view.*

class DetailViewFragment : Fragment() {

    var firestore: FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =
            LayoutInflater.from(activity).inflate(R.layout.fragment_detail_view, container, false)

        firestore = FirebaseFirestore.getInstance()

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

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }

}