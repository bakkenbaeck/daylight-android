package com.bakkenbaeck.sol.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bakkenbaeck.sol.R
import kotlinx.android.synthetic.main.fragment_start.*

class StartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        initView()
    }

    private fun initView() {
        infoMessage.text = getColoredMessage()
    }

    private fun getColoredMessage(): String {
        val message = getString(R.string.start__permission_description)
        val context = context ?: return message
        val color = ContextCompat.getColor(context, R.color.daylight_text2).toString()
        return message.replace("{color}", color)
    }
}