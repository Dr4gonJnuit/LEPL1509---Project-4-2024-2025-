import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.jobswype.R
import com.example.jobswype.model.UserModel

class MessageUserAdapter(private val context: Context, private val contacts: List <UserModel>, private var onContactItemClickListener: OnContactItemClickListener) : RecyclerView.Adapter <MessageUserAdapter.UserModelViewHolder>() {
    inner class UserModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactLayout : LinearLayout = itemView.findViewById(R.id.contact_layout)
        val userName : TextView = itemView.findViewById(R.id.user_item_username)
        val profilePic : ImageView = itemView.findViewById(R.id.user_item_image)
        val phone : TextView = itemView.findViewById(R.id.user_item_phone)
        val eMail : TextView = itemView.findViewById(R.id.user_item_email)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserModelViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false)
        return UserModelViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserModelViewHolder, position: Int) {
        val contact = contacts[position]
        if (contact.username != "none") {
            holder.userName.text = contact.username
        } else {
            holder.userName.text = contact.email?.substring(0, contact.email.indexOf("@"))
        }
        if (contact.phone != "none") {
            holder.phone.text = contact.phone
        } else {
            holder.phone.visibility = View.GONE
        }
        holder.eMail.text = contact.email
        if (contact.profilePic != "none"){
            Glide.with(context)
                .load(contact.profilePic)
                .apply(
                    RequestOptions.bitmapTransform(
                        CircleCrop()
                    )
                ) // Apply a transform circle
                .placeholder(R.drawable.default_pdp) // Placeholder image while loading
                .error(R.drawable.default_pdp) // Image to show if loading fails
                .into(holder.profilePic)
        } else {
            holder.profilePic.setImageResource(R.drawable.default_pdp)
        }
        holder.contactLayout.setOnClickListener {
            onContactItemClickListener.onContactItemClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    fun setOnContactItemClickListener(listener: OnContactItemClickListener) {
        onContactItemClickListener = listener
    }
}
interface OnContactItemClickListener {
    fun onContactItemClicked(position: Int)
}