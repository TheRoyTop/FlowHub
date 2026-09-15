package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GmailAccountDao {
    @Query("SELECT * FROM gmail_accounts ORDER BY orderIndex ASC, id ASC")
    fun getAllAccounts(): Flow<List<GmailAccount>>

    @Query("SELECT * FROM gmail_accounts WHERE isActive = 1 LIMIT 1")
    fun getActiveAccount(): Flow<GmailAccount?>

    @Query("SELECT * FROM gmail_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): GmailAccount?

    @Query("SELECT COUNT(*) FROM gmail_accounts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: GmailAccount): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<GmailAccount>)

    @Update
    suspend fun updateAccount(account: GmailAccount)

    @Delete
    suspend fun deleteAccount(account: GmailAccount)

    @Query("UPDATE gmail_accounts SET isActive = 0")
    suspend fun clearAllActive()

    @Query("UPDATE gmail_accounts SET isActive = 1, lastUsedTimestamp = :timestamp WHERE id = :id")
    suspend fun markActive(id: Long, timestamp: Long = System.currentTimeMillis())

    @Transaction
    suspend fun setActiveAccount(id: Long) {
        clearAllActive()
        markActive(id)
    }
}
