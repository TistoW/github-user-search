package com.tisto.githubusers.data.mapper

import com.tisto.githubusers.data.local.UserEntity
import com.tisto.githubusers.data.remote.dto.UserDetailDto
import com.tisto.githubusers.data.remote.dto.UserSummaryDto
import com.tisto.githubusers.domain.model.User
import com.tisto.githubusers.domain.model.UserDetail

fun UserSummaryDto.toEntity() = UserEntity(
    id = id,
    login = login,
    avatarUrl = avatarUrl,
    htmlUrl = htmlUrl,
    type = type,
)

fun UserDetailDto.toEntity(fetchedAt: Long) = UserEntity(
    id = id,
    login = login,
    avatarUrl = avatarUrl,
    htmlUrl = htmlUrl,
    type = type,
    name = name,
    bio = bio,
    company = company,
    location = location,
    blog = blog,
    twitterUsername = twitterUsername,
    email = email,
    followers = followers,
    following = following,
    publicRepos = publicRepos,
    publicGists = publicGists,
    createdAt = createdAt,
    detailFetchedAt = fetchedAt,
)

fun UserEntity.toUser() = User(id = id, login = login, avatarUrl = avatarUrl, type = type)

fun UserEntity.toUserDetail() = UserDetail(
    id = id,
    login = login,
    avatarUrl = avatarUrl,
    htmlUrl = htmlUrl,
    type = type,
    name = name,
    bio = bio,
    company = company,
    location = location,
    blog = blog,
    twitterUsername = twitterUsername,
    email = email,
    followers = followers,
    following = following,
    publicRepos = publicRepos,
    publicGists = publicGists,
    createdAt = createdAt,
)
