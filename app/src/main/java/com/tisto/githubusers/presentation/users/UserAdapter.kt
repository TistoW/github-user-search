package com.tisto.githubusers.presentation.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.tisto.githubusers.R
import com.tisto.githubusers.databinding.ItemUserBinding
import com.tisto.githubusers.domain.model.User

class UserAdapter(private val onClick: (User) -> Unit) :
    ListAdapter<User, UserAdapter.ViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) = with(binding) {
            login.text = user.login
            type.setText(
                if (user.type.equals("Organization", ignoreCase = true)) R.string.user_type_organization
                else R.string.user_type_user,
            )
            avatar.contentDescription = root.context.getString(R.string.avatar_description, user.login)

            // GitHub avatars take an s= size, so ask for about 2x the displayed size instead of the full image.
            val sizePx = root.resources.getDimensionPixelSize(R.dimen.avatar_list) * 2
            Glide.with(avatar)
                .load(user.avatarUrl.withSize(sizePx))
                .override(sizePx)
                .placeholder(R.drawable.avatar_placeholder)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(avatar)

            root.setOnClickListener { onClick(user) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(old: User, new: User) = old.id == new.id
        override fun areContentsTheSame(old: User, new: User) = old == new
    }
}

private fun String.withSize(px: Int) = if (contains('?')) "$this&s=$px" else "$this?s=$px"
