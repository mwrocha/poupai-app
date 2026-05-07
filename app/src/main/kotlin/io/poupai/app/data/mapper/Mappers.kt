package io.poupai.app.data.mapper

import io.poupai.app.data.local.entity.TransactionEntity
import io.poupai.app.data.local.entity.UserEntity
import io.poupai.app.data.remote.dto.InvestmentDto
import io.poupai.app.data.remote.dto.TransactionDto
import io.poupai.app.data.remote.dto.UserDto
import io.poupai.app.domain.model.Investment
import io.poupai.app.domain.model.InvestmentType
import io.poupai.app.domain.model.Transaction
import io.poupai.app.domain.model.TransactionType
import io.poupai.app.domain.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── User Mappers ───

fun UserDto.toDomain(): User = User(
    id = id.orEmpty(),
    username = username.orEmpty(),
    firstName = firstName.orEmpty(),
    lastName = lastName.orEmpty(),
    email = email.orEmpty(),
    birthDate = birthDate?.toDate(),
    profileImageUrl = profileImageUrl,
)

fun UserEntity.toDomain(): User = User(
    id = id,
    username = username,
    firstName = firstName,
    lastName = lastName,
    email = email,
    birthDate = birthDate?.let { Date(it) },
    profileImageUrl = profileImageUrl,
)

// ─── Transaction Mappers ───

fun TransactionDto.toDomain(): Transaction = Transaction(
    id = id,
    title = title,
    amount = amount,
    type = TransactionType.valueOf(type.uppercase()),
    category = category,
    date = date.toDate() ?: Date(),
    tagId = tagId,
)

fun TransactionDto.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    title = title,
    amount = amount,
    type = type.uppercase(),
    category = category,
    date = date.toDate()?.time ?: System.currentTimeMillis(),
    tagId = tagId,
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    title = title,
    amount = amount,
    type = TransactionType.valueOf(type),
    category = category,
    date = Date(date),
    tagId = tagId,
)

// ─── Transaction domain → Entity (usado ao salvar resposta da API no Room) ───
fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    title = title,
    amount = amount,
    type = type.name,
    category = category,
    date = date.time,
    tagId = tagId,
)

// ─── Investment Mappers ───

fun InvestmentDto.toDomain(): Investment = Investment(
    id = id.orEmpty(),
    name = name.orEmpty(),
    type = when (type?.uppercase()) {
        "RENDA_VARIAVEL" -> InvestmentType.RENDA_VARIAVEL
        "RENDA_FIXA" -> InvestmentType.RENDA_FIXA
        "CRIPTOMOEDAS" -> InvestmentType.CRIPTOMOEDAS
        else -> InvestmentType.RENDA_FIXA
    },
    currentValue = currentValue,
    investedValue = investedValue,
    profitability = profitability,
)

// ─── Util ───

private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

private fun String.toDate(): Date? = try {
    dateFormat.parse(this)
} catch (e: Exception) {
    null
}