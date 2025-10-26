package com.tuktak.carepick.ui.selfDiagnosis.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tuktak.carepick.R
import com.tuktak.carepick.ui.selfDiagnosis.model.ChatMessage

class MessageAdapter(
    private val items: MutableList<ChatMessage>,
    private val onSpecialtyClick: (specialty: String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_BOT = 2
        private const val TYPE_TYPING = 3
        private const val TYPE_BUTTONS = 4 // ✨ 버튼 타입 상수 추가
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ChatMessage.User -> TYPE_USER
        is ChatMessage.Bot -> TYPE_BOT
        is ChatMessage.Typing -> TYPE_TYPING
        is ChatMessage.SystemSpecialtyButtons -> TYPE_BUTTONS // ✨ 버튼 타입 반환
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_USER -> UserVH(inf.inflate(R.layout.item_message_user, parent, false))
            TYPE_BOT -> BotVH(inf.inflate(R.layout.item_message_bot, parent, false))
            TYPE_BUTTONS -> ButtonsVH(inf.inflate(R.layout.item_message_buttons, parent, false)) // ✨ 버튼 ViewHolder 생성
            else -> TypingVH(inf.inflate(R.layout.item_message_typing, parent, false))
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserVH -> holder.bind((items[position] as ChatMessage.User).text)
            is BotVH -> holder.bind((items[position] as ChatMessage.Bot).text)
            is ButtonsVH -> { // ✨ 버튼 ViewHolder에 데이터 바인딩
                val message = items[position] as ChatMessage.SystemSpecialtyButtons
                holder.bind(message.specialties, onSpecialtyClick)
            }
            is TypingVH -> holder.bind()
        }
    }

    fun updateMessages(newMessages: List<ChatMessage>) {
        items.clear()
        items.addAll(newMessages)
        notifyDataSetChanged() // Simple, but works. For better performance, use DiffUtil.
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
                    tv.text = "분석 중 $dots"
                    i++
                    // View가 아직 화면에 있으면 400ms 주기로 계속
                    if (tv.isAttachedToWindow) tv.postDelayed(this, 400)
                }
            })
        }
    }

    class ButtonsVH(v: View) : RecyclerView.ViewHolder(v) {
        private val button1: TextView = v.findViewById(R.id.specialtyButton1)
        private val button2: TextView = v.findViewById(R.id.specialtyButton2)
        private val button3: TextView = v.findViewById(R.id.specialtyButton3)
        private val allButtons = listOf(button1, button2, button3)

        fun bind(specialties: List<String>, onSpecialtyClick: (String) -> Unit) {
            // 모든 버튼을 일단 숨김 처리 (진료과가 3개 미만일 경우 대비)
            allButtons.forEach { it.visibility = View.GONE }

            specialties.forEachIndexed { index, specialty ->
                if (index < allButtons.size) {
                    val button = allButtons[index]
                    button.visibility = View.VISIBLE
                    button.text = "내 주변 '${specialty}' 검색하기"
                    button.setOnClickListener {
                        // 클릭 시 프래그먼트로 specialty 텍스트 전달
                        onSpecialtyClick(specialty)
                    }
                }
            }
        }
    }
}