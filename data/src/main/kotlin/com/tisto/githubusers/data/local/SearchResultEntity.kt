package com.tisto.githubusers.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "search_results",
    primaryKeys = ["query", "user_id"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("user_id")],
)
data class SearchResultEntity(
    val query: String,
    @ColumnInfo(name = "user_id") val userId: Long,
    val position: Int,
)
