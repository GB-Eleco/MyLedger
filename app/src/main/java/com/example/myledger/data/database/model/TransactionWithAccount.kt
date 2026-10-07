package com.example.myledger.data.database.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myledger.data.database.entity.AccountEntity
import com.example.myledger.data.database.entity.TransactionEntity

data class TransactionWithAccount(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "accountId",
        entityColumn = "id"
    )
    val account: AccountEntity? = null
)
