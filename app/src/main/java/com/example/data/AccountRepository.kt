package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AccountRepository(private val dao: GmailAccountDao) {

    val allAccounts: Flow<List<GmailAccount>> = dao.getAllAccounts()
    val activeAccount: Flow<GmailAccount?> = dao.getActiveAccount()

    suspend fun seedDefaultsIfEmpty() {
        if (dao.count() == 0) {
            val defaults = listOf(
                GmailAccount(
                    email = "royharpertop@gmail.com",
                    displayName = "Roy Harper",
                    label = "Principal (Lumina)",
                    avatarColorHex = "#8083FF",
                    isActive = true,
                    orderIndex = 0
                ),
                GmailAccount(
                    email = "flow.creator.ai@gmail.com",
                    displayName = "Flow Creator",
                    label = "Workspace Studio",
                    avatarColorHex = "#4EDEA3",
                    isActive = false,
                    orderIndex = 1
                ),
                GmailAccount(
                    email = "notebook.research@gmail.com",
                    displayName = "NotebookLM Pro",
                    label = "Investigación & Docs",
                    avatarColorHex = "#DDB7FF",
                    isActive = false,
                    orderIndex = 2
                ),
                GmailAccount(
                    email = "pipeline.render408@gmail.com",
                    displayName = "Render Pipeline",
                    label = "Producción 4K",
                    avatarColorHex = "#FFB4AB",
                    isActive = false,
                    orderIndex = 3
                )
            )
            dao.insertAccounts(defaults)
        }
    }

    suspend fun cycleToNextAccount(): GmailAccount? {
        val accounts = allAccounts.firstOrNull() ?: emptyList()
        if (accounts.isEmpty()) return null
        if (accounts.size == 1) {
            dao.setActiveAccount(accounts[0].id)
            return accounts[0]
        }

        val currentIndex = accounts.indexOfFirst { it.isActive }
        val nextIndex = if (currentIndex == -1 || currentIndex >= accounts.size - 1) 0 else currentIndex + 1
        val nextAccount = accounts[nextIndex]
        dao.setActiveAccount(nextAccount.id)
        return nextAccount
    }

    suspend fun setActive(id: Long) {
        dao.setActiveAccount(id)
    }

    suspend fun addAccount(
        email: String,
        displayName: String,
        label: String,
        avatarColorHex: String
    ): Long {
        val accounts = allAccounts.firstOrNull() ?: emptyList()
        val nextOrder = accounts.size
        val newAccount = GmailAccount(
            email = email.trim(),
            displayName = displayName.trim().ifEmpty { email.substringBefore("@") },
            label = label.trim().ifEmpty { "General" },
            avatarColorHex = avatarColorHex,
            isActive = accounts.isEmpty(),
            orderIndex = nextOrder
        )
        return dao.insertAccount(newAccount)
    }

    suspend fun deleteAccount(account: GmailAccount) {
        dao.deleteAccount(account)
        // If deleted account was active, activate another if available
        if (account.isActive) {
            val remaining = allAccounts.firstOrNull() ?: emptyList()
            if (remaining.isNotEmpty()) {
                dao.setActiveAccount(remaining.first().id)
            }
        }
    }
}
