package com.tisto.githubusers.presentation.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import com.tisto.githubusers.R
import com.tisto.githubusers.databinding.FragmentUserDetailBinding
import com.tisto.githubusers.domain.model.UserDetail
import com.tisto.githubusers.presentation.common.compactCount
import com.tisto.githubusers.presentation.common.errorMessage
import com.tisto.githubusers.presentation.common.formatJoinedDate
import com.tisto.githubusers.presentation.common.toWebUrl
import com.tisto.githubusers.presentation.common.withAvatarSize
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserDetailFragment : Fragment(R.layout.fragment_user_detail) {

    private val viewModel: UserDetailViewModel by viewModels()
    private val args: UserDetailFragmentArgs by navArgs()

    // True while the refresh was started by a pull, so the pull spinner and the top bar never show together.
    private var pulling = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentUserDetailBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            binding.scroll.setPadding(0, 0, 0, bottom)
            insets
        }

        val goBack = { findNavController().popBackStack(); Unit }
        binding.toolbar.title = args.login
        binding.toolbar.setNavigationOnClickListener { goBack() }
        binding.goBack.setOnClickListener { goBack() }

        binding.swipe.setColorSchemeResources(R.color.primary)
        binding.swipe.setProgressBackgroundColorSchemeResource(R.color.surface_container_high)
        binding.swipe.setOnRefreshListener {
            pulling = true
            viewModel.refresh()
        }

        loadAvatar(binding, args.avatarUrl)
        binding.name.text = args.login

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (!state.isRefreshing) pulling = false
            binding.swipe.isRefreshing = state.isRefreshing && pulling
            binding.progress.visibility = if (state.isRefreshing && !pulling) View.VISIBLE else View.INVISIBLE
            binding.notFound.isVisible = state.notFound
            binding.swipe.isVisible = !state.notFound
            state.user?.let { bind(binding, it) }

            state.error?.let { error ->
                viewModel.onErrorShown()
                Snackbar.make(binding.root, requireContext().errorMessage(error, hasCachedResults = state.user != null, isSearch = false), Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { viewModel.refresh() }
                    .show()
            }
        }
    }

    private fun bind(binding: FragmentUserDetailBinding, user: UserDetail) = with(binding) {
        loadAvatar(binding, user.avatarUrl)
        avatar.contentDescription = getString(R.string.avatar_description, user.login)

        // Without a display name the login is the title, so the @handle line would only repeat it.
        name.text = user.name?.takeIf { it.isNotBlank() } ?: user.login
        handle.text = getString(R.string.handle, user.login)
        handle.isVisible = !user.name.isNullOrBlank()
        typeChip.isVisible = user.type.equals("Organization", ignoreCase = true)

        bio.text = user.bio?.trim()
        bio.isVisible = !user.bio.isNullOrBlank()

        // A user known only from a search result has no counts yet.
        val followers = user.followers
        val following = user.following
        val repos = user.publicRepos
        stats.isVisible = followers != null && following != null && repos != null
        if (followers != null && following != null && repos != null) {
            statFollowers.text = compactCount(followers)
            statFollowing.text = compactCount(following)
            statRepos.text = compactCount(repos)
        }

        showRow(rowCompany, textCompany, user.company)
        showRow(rowLocation, textLocation, user.location)
        showRow(rowBlog, textBlog, user.blog, link = true)
        showRow(rowTwitter, textTwitter, user.twitterUsername?.let { "@$it" }, link = true)
        showRow(rowEmail, textEmail, user.email, link = true)
        showRow(rowJoined, textJoined, formatJoinedDate(user.createdAt)?.let { getString(R.string.detail_joined, it) })

        rowBlog.setOnClickListener { open(Intent.ACTION_VIEW, user.blog?.toWebUrl()) }
        rowTwitter.setOnClickListener { open(Intent.ACTION_VIEW, "https://x.com/${user.twitterUsername}") }
        rowEmail.setOnClickListener { open(Intent.ACTION_SENDTO, "mailto:${user.email}") }
        viewOnGithub.setOnClickListener { open(Intent.ACTION_VIEW, user.htmlUrl) }
    }

    private fun showRow(row: View, text: TextView, value: String?, link: Boolean = false) {
        row.isVisible = !value.isNullOrBlank()
        text.text = value
        if (link) text.setTextColor(requireContext().getColorStateList(R.color.tertiary))
    }

    private fun loadAvatar(binding: FragmentUserDetailBinding, url: String) {
        val sizePx = resources.getDimensionPixelSize(R.dimen.avatar_detail) * 2
        Glide.with(binding.avatar)
            .load(url.withAvatarSize(sizePx))
            .override(sizePx)
            .placeholder(R.drawable.avatar_placeholder)
            .circleCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.avatar)
    }

    private fun open(action: String, uri: String?) {
        if (uri == null) return
        runCatching { startActivity(Intent(action, Uri.parse(uri))) }
    }
}
