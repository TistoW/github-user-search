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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.tisto.githubusers.R
import com.tisto.githubusers.databinding.FragmentUsersBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UsersFragment : Fragment(R.layout.fragment_users) {

    private val viewModel: UsersViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentUsersBinding.bind(view)

        // The list scrolls under the navigation bar, so only its bottom padding follows the inset.
        ViewCompat.setOnApplyWindowInsetsListener(binding.list) { list, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            list.setPadding(list.paddingLeft, list.paddingTop, list.paddingRight, bottom)
            insets
        }

        val adapter = UserAdapter { /* detail navigation arrives with the detail screen */ }
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.adapter = adapter
        binding.list.addItemDecoration(
            MaterialDividerItemDecoration(requireContext(), MaterialDividerItemDecoration.VERTICAL).apply {
                dividerInsetStart = resources.getDimensionPixelSize(R.dimen.divider_inset)
                dividerInsetEnd = resources.getDimensionPixelSize(R.dimen.space_l)
                isLastItemDecorated = false
            },
        )

        binding.searchInput.setText(viewModel.query.value)
        binding.searchInput.doAfterTextChanged { viewModel.onQueryChanged(it.toString()) }
        binding.searchInput.setOnEditorActionListener { input, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(input.windowToken, 0)
                input.clearFocus()
                true
            } else {
                false
            }
        }

        viewModel.users.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
            val queryBlank = viewModel.query.value.isNullOrBlank()
            binding.recentHeader.isVisible = queryBlank && users.isNotEmpty()
            binding.emptyState.isVisible = queryBlank && users.isEmpty()
        }
    }
}
