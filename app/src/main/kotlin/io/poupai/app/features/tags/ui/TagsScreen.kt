package io.poupai.app.features.tags.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.poupai.app.core.theme.Purple40
import io.poupai.app.core.theme.PurpleDark
import io.poupai.app.core.theme.RedNegative
import io.poupai.app.core.util.toBRL
import io.poupai.app.domain.model.Tag
import io.poupai.app.domain.model.Transaction
import io.poupai.app.features.tags.viewmodel.TagsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TagsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val monthNames = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")

    if (uiState.selectedTag != null) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissDetail,
            sheetState = sheetState,
        ) {
            TagDetailSheet(
                tag = uiState.selectedTag!!,
                transactions = uiState.tagTransactions,
                isLoading = uiState.isLoadingDetail,
                totalSpent = uiState.totalSpent,
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F7)),
    ) {

        // ─── Header ───
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = listOf(PurpleDark, Purple40)))
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Text("Tags", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }
        }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Purple40)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f),
            ) {
                // ─── Card de resumo ───
                item {
                    SummaryCard(
                        totalSpent = uiState.totalSpent,
                        tagCount = uiState.filteredTags.size,
                        monthLabel = monthNames.getOrElse(uiState.selectedMonth - 1) { "" },
                        year = uiState.selectedYear,
                        onPreviousMonth = viewModel::onPreviousMonth,
                        onNextMonth = viewModel::onNextMonth,
                    )
                }

                // ─── Busca ───
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = { Text("Pesquisar categoria...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, "Pesquisar", tint = Color(0xFF9E9E9E)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple40,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                        ),
                    )
                }

                if (uiState.filteredTags.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(40.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    if (uiState.searchQuery.isNotBlank()) "Nenhuma categoria encontrada"
                                    else "Nenhuma despesa neste mês",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF9E9E9E),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                } else {
                    // ─── Título da seção ───
                    item {
                        Text(
                            "Por categoria",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1B1F),
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }

                    // ─── Lista de categorias com barra de progresso ───
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Column {
                                uiState.filteredTags.forEachIndexed { index, tag ->
                                    TagListItem(
                                        tag = tag,
                                        totalSpent = uiState.totalSpent,
                                        onClick = { viewModel.onTagSelected(tag) },
                                    )
                                    if (index < uiState.filteredTags.lastIndex) {
                                        HorizontalDivider(
                                            color = Color(0xFFF5F5F5),
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── CARD DE RESUMO ───

@Composable
private fun SummaryCard(
    totalSpent: Double,
    tagCount: Int,
    monthLabel: String,
    year: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.linearGradient(listOf(PurpleDark, Purple40)), shape = RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            Column {
                // Seletor de mês
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onPreviousMonth, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ArrowBackIosNew, "Mês anterior", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        "$monthLabel $year",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                    IconButton(onClick = onNextMonth, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ArrowForwardIos, "Próximo mês", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "Total gasto",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    totalSpent.toBRL(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.6f)),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "$tagCount categoria${if (tagCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                }
            }
        }
    }
}

// ─── ITEM DA LISTA DE CATEGORIAS ───

@Composable
private fun TagListItem(tag: Tag, totalSpent: Double, onClick: () -> Unit) {
    val percent = if (totalSpent > 0) (tag.totalSpent / totalSpent).toFloat() else 0f
    val percentInt = (percent * 100).toInt()
    val tagColor = try { Color(android.graphics.Color.parseColor(tag.color)) } catch (e: Exception) { Purple40 }

    var animationPlayed by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (animationPlayed) percent else 0f,
        animationSpec = tween(800),
        label = "tag_progress",
    )
    LaunchedEffect(tag.id) { animationPlayed = true }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar circular com inicial
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(tagColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                tag.name.take(2).uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = tagColor,
            )
        }

        Spacer(Modifier.width(12.dp))

        // Nome + barra + percentual
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    tag.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1B1F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "$percentInt%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tagColor,
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { animProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = tagColor,
                trackColor = tagColor.copy(alpha = 0.10f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                tag.totalSpent.toBRL(),
                fontSize = 11.sp,
                color = Color(0xFF6B6B6B),
            )
        }

        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(20.dp),
        )
    }
}

// ─── BOTTOM SHEET DE DETALHE ───

@Composable
private fun TagDetailSheet(
    tag: Tag,
    transactions: List<Transaction>,
    isLoading: Boolean,
    totalSpent: Double,
) {
    val tagColor = try { Color(android.graphics.Color.parseColor(tag.color)) } catch (e: Exception) { Purple40 }
    val percent = if (totalSpent > 0) (tag.totalSpent / totalSpent * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(tagColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    tag.name.take(2).uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = tagColor,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tag.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1B1F))
                Text("$percent% do total gasto", fontSize = 12.sp, color = Color(0xFF6B6B6B))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(tag.totalSpent.toBRL(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = tagColor)
                Text(
                    "${transactions.size} transaç${if (transactions.size == 1) "ão" else "ões"}",
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E),
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = Color(0xFFF0F0F0))
        Spacer(Modifier.height(8.dp))

        if (isLoading) {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = tagColor, modifier = Modifier.size(32.dp))
            }
        } else if (transactions.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Nenhuma transação encontrada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9E9E9E),
                )
            }
        } else {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                transactions.forEachIndexed { index, transaction ->
                    TagTransactionRow(transaction = transaction)
                    if (index < transactions.lastIndex) {
                        HorizontalDivider(
                            color = Color(0xFFF5F5F5),
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagTransactionRow(transaction: Transaction) {
    val dateFormatter = SimpleDateFormat("dd 'de' MMM", Locale("pt", "BR"))

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(RedNegative.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.TrendingDown, null, tint = RedNegative, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                transaction.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1B1F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                dateFormatter.format(transaction.date).replaceFirstChar { it.uppercase() },
                fontSize = 11.sp,
                color = Color(0xFF9E9E9E),
            )
        }
        Text(
            "- ${transaction.amount.toBRL()}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = RedNegative,
        )
    }
}