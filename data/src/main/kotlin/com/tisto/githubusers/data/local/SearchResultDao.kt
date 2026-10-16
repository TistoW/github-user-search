package com.tisto.githubusers.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SearchResultDao {

    @Insert
    abstract suspend fun insertAll(results: List<SearchResultEntity>)

    @Query("DELETE FROM search_results WHERE `query` = :query")
    abstract suspend fun deleteForQuery(query: String)

    // The referenced users must already be in the users table (foreign key).
    @Transaction
    open suspend fun replaceResults(query: String, results: List<SearchResultEntity>) {
        deleteForQuery(query)
        insertAll(results)
    }

    @Query(
        "SELECT users.* FROM users " +
            "INNER JOIN search_results ON users.id = search_results.user_id " +
            "WHERE search_results.`query` = :query " +
            "ORDER BY search_results.position",
    )
    abstract fun observeResults(query: String): Flow<List<UserEntity>>
}
