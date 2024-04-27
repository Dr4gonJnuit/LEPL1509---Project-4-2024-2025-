import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobswype.R
import com.example.jobswype.model.ChatModel

class ChatRecyclerAdapter(
    private val context: Context,
    private val messages: List <ChatModel>) : RecyclerView.Adapter <ChatRecyclerAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiverChatLayout : LinearLayout = itemView.findViewById(R.id.msg_receiver_layout)
        val senderChatLayout : LinearLayout = itemView.findViewById(R.id.msg_sender_layout)
        val receiverChatTextview : TextView = itemView.findViewById(R.id.message_receiver)
        val senderChatTextview : TextView = itemView.findViewById(R.id.message_sender)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        if(messages[position].currentUserID.equals(messages[position].sender)) {
            holder.senderChatLayout.visibility = View.VISIBLE
            holder.receiverChatLayout.visibility = View.GONE
            holder.senderChatTextview.text = messages[position].message
        } else {
            holder.receiverChatLayout.visibility = View.VISIBLE
            holder.senderChatLayout.visibility = View.GONE
            holder.receiverChatTextview.text = messages[position].message
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}