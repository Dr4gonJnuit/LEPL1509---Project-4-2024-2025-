import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobswype.R
import com.example.jobswype.databinding.UserItemLayoutBinding
import com.example.jobswype.model.UserModel

class MessageUserAdapter(private val dataList: ArrayList<UserModel>) : RecyclerView.Adapter<MessageUserAdapter.ViewHolderClass>() {

    class ViewHolderClass(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userItemUsername = itemView.findViewById<TextView>(R.id.user_item_username)
        val profilePic = itemView.findViewById<ImageView>(R.id.user_item_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_item_layout, parent, false)
        return ViewHolderClass(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        holder.userItemUsername.text = currentItem.title
        if (currentItem.imageUrl == "" || currentItem.imageUrl == "none"){
            holder.profilePic.setImageResource(R.drawable.default_pdp)
        } else{
            Glide.with(holder.itemView.context).load(currentItem.imageUrl).into(holder.profilePic)
        }
    }
}
