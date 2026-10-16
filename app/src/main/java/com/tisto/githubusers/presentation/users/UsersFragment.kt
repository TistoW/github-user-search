package com.tisto.githubusers.presentation.users

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.tisto.githubusers.R
import com.google.android.material.snackbar.Snackbar
import com.tisto.githubusers.databinding.FragmentUsersBinding
import com.tisto.githubusers.presentation.common.errorMessage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UsersFragment : Fragment(R.layout.fragment_users) {

    private val viewModel: UsersViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentUsersBinding.bind(view)

        // The keyboard shrinks the screen so the empty state stays visible. Otherwise only the list's
        // bottom padding follows the navigation bar, which lets it scroll underneath.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, insets ->
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBar = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            root.setPadding(0, 0, 0, keyboard)
            binding.list.setPadding(0, 0, 0, if (keyboard > 0) 0 else navBar)
            insets
        }

        val adapter = UserAdapter { user ->
            findNavController().navigate(UsersFragmentDirections.actionUsersToDetail(user.login, user.avatarUrl))
        }
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = adapter
        binding.list.addItemDecoration(
            MaterialDividerItemDecoration(requireContext(), MaterialDividerItemDecoration.VERTICAL).apply {
                dividerInsetStart = resources.getDimensionPixelSize(R.dimen.divider_inset)
                dividerInsetEnd = resources.getDimensionPixelSize(R.dimen.space_l)
                isLastItemDecorated = false
            },
        )

        binding.searchInput.setText(viewModel.currentQuery)
        binding.searchInput.doAfterTextChanged { viewModel.onQueryChanged(it.toString()) }
        binding.searchInput.setOnEditorActionListener { input, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(input.windowToken, 0)
                input.clearFocus()
                viewModel.submit()
                true
            } else {
                false
            }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.users)
            render(binding, state)
            state.errorToShow?.let { error ->
                viewModel.onErrorShown()
                Snackbar.make(binding.root, requireContext().errorMessage(error, hasCachedResults = state.users.isNotEmpty()), Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { viewModel.submit() }
                    .show()
            }
        }
    }

    private fun render(binding: FragmentUsersBinding, state: UsersUiState) {
        binding.progress.visibility = if (state.isLoading) View.VISIBLE else View.INVISIBLE
        val recent = state.section == UsersSection.RecentlyViewed
        binding.recentHeader.isVisible = recent && state.users.isNotEmpty()
        binding.resultsCount.isVisible = !recent && state.users.isNotEmpty()
        binding.resultsCount.text =
            resources.getQuantityString(R.plurals.results_count, state.users.size, state.users.size)

        val showEmpty = state.users.isEmpty() && !state.isLoading
        binding.emptyState.isVisible = showEmpty
        if (!showEmpty) return
        val (icon, text) = when {
            recent -> R.drawable.ic_search to getString(R.string.empty_start)
            state.error != null -> R.drawable.ic_cloud_off to getString(R.string.empty_not_loaded)
            else -> R.drawable.ic_people to getString(R.string.empty_no_matches, state.query)
        }
        binding.emptyIcon.setImageResource(icon)
        binding.emptyText.text = text
    }
}
