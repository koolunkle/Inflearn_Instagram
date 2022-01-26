package com.inflearn.instagramcopy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class GridFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =
            LayoutInflater.from(activity).inflate(R.layout.fragment_grid, container, false)
        return view
    }

}