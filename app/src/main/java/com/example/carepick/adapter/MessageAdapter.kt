package com.example.carepick.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carepick.R
import com.example.carepick.model.ChatMessage

class MessageAdapter(
    private val items: MutableList<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_BOT = 2
        private const val TYPE_TYPING = 3
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ChatMessage.User -> TYPE_USER
        is ChatMessage.Bot -> TYPE_BOT
        is ChatMessage.Typing -> TYPE_TYPING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_USER -> UserVH(inf.inflate(R.layout.item_message_user, parent, false))
            TYPE_BOT -> BotVH(inf.inflate(R.layout.item_message_bot, parent, false))
            else -> TypingVH(inf.inflate(R.layout.item_message_typing, parent, false))
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserVH -> holder.bind((items[position] as ChatMessage.User).text)
            is BotVH -> holder.bind((items[position] as ChatMessage.Bot).text)
            is TypingVH -> holder.bind()
        }
    }

    fun add(message: ChatMessage) {
        items.add(message)
        notifyItemInserted(items.size - 1)
    }

    fun removeLastIfTyping() {
        if (items.isNotEmpty() && items.last() is ChatMessage.Typing) {
            val idx = items.size - 1
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }

    class UserVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tv: TextView = v.findViewById(R.id.messageText)
        fun bind(text: String) { tv.text = text }
    }

    class BotVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tv: TextView = v.findViewById(R.id.messageText)
        fun bind(text: String) { tv.text = text }
    }

    class TypingVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tv: TextView = v.findViewById(R.id.typingText)
        private var running = false

        fun bind() {
            if (running) return
            running = true
            // 간단한 '입력 중' 점점점 애니메이션
            tv.post(object : Runnable {
                var i = 0
                override fun run() {
                    val dots = when (i % 4) {
                        0 -> ""
                        1 -> "."
                        2 -> ".."
                        else -> "..."
                    }
                    tv.text = "입력 중 $dots"
                    i++
                    // View가 아직 화면에 있으면 400ms 주기로 계속
                    if (tv.isAttachedToWindow) tv.postDelayed(this, 400)
                }
            })
        }
    }
}