package com.fileshare.app.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fileshare.app.domain.model.Category
import com.fileshare.app.domain.model.Document
import com.fileshare.app.util.FileUtils
import com.fileshare.app.viewmodel.CategoryViewModel
import com.fileshare.app.viewmodel.DocumentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    documentViewModel: DocumentViewModel,
    categoryViewModel: CategoryViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val documents by documentViewModel.documents.collectAsState()
    val selectedCategoryId by documentViewModel.selectedCategoryId.collectAsState()
    val searchQuery by documentViewModel.searchQuery.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()
    val context = LocalContext.current
    var showSearchBar by remember { mutableStateOf(false) }
    
    var selectionMode by remember { mutableStateOf(false) }
    val selectedDocuments = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("파일저장") },
                actions = {
                    if (selectionMode) {
                        IconButton(onClick = {
                            if (selectedDocuments.isNotEmpty()) {
                                val filePaths = documents
                                    .filter { it.id in selectedDocuments }
                                    .flatMap { it.imageUris }
                                FileUtils.shareMultipleFiles(context, filePaths)
                            }
                            selectionMode = false
                            selectedDocuments.clear()
                        }) {
                            Icon(Icons.Default.Share, "공유하기")
                        }
                        IconButton(onClick = {
                            selectionMode = false
                            selectedDocuments.clear()
                        }) {
                            Icon(Icons.Default.Close, "취소")
                        }
                    } else {
                        IconButton(onClick = { showSearchBar = !showSearchBar }) {
                            Icon(Icons.Default.Search, "검색")
                        }
                        IconButton(onClick = { onNavigateToSettings() }) {
                            Icon(Icons.Default.Settings, "설정")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!selectionMode) {
                FloatingActionButton(onClick = onNavigateToAdd) {
                    Icon(Icons.Default.Add, "문서 추가")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            if (showSearchBar) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { documentViewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("제목 또는 메모 검색") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { documentViewModel.clearSearch() }) {
                                Icon(Icons.Default.Close, "검색 초기화")
                            }
                        }
                    },
                    singleLine = true
                )
            }
            
            // Category Filter
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { documentViewModel.selectCategory(null) },
                        label = { Text("전체") }
                    )
                }
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { documentViewModel.selectCategory(category.id) },
                        label = { Text(category.name) }
                    )
                }
            }

            HorizontalDivider()

            // Document List
            if (documents.isEmpty()) {
                EmptyState(onAddDocument = onNavigateToAdd)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        if (!selectionMode) {
                            TextButton(
                                onClick = { selectionMode = true }
                            ) {
                                Icon(Icons.Default.CheckCircle, "선택 모드", modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("여러 개 선택")
                            }
                        }
                    }
                    
                    items(documents, key = { it.id }) { document ->
                        val category = categories.find { it.id == document.categoryId }
                        DocumentCard(
                            document = document,
                            categoryName = category?.name,
                            selectionMode = selectionMode,
                            isSelected = document.id in selectedDocuments,
                            onClick = {
                                if (selectionMode) {
                                    if (document.id in selectedDocuments) {
                                        selectedDocuments.remove(document.id)
                                    } else {
                                        selectedDocuments.add(document.id)
                                    }
                                } else {
                                    onNavigateToDetail(document.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(onAddDocument: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "저장된 문서가 없습니다",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "통장사본, 사업자등록증 등을 저장하고\n언제든 빠르게 공유하세요",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAddDocument) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("첫 문서 추가하기")
        }
    }
}

@Composable
fun DocumentCard(
    document: Document,
    categoryName: String?,
    selectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            
            // Thumbnail
            val thumbnailUri = document.fileUri
            val isRemoteUrl = thumbnailUri.startsWith("http://") || thumbnailUri.startsWith("https://")
            
            if (thumbnailUri.isNotEmpty() && (isRemoteUrl || FileUtils.fileExists(thumbnailUri))) {
                Card(
                    modifier = Modifier.size(60.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    AsyncImage(
                        model = if (isRemoteUrl) thumbnailUri else java.io.File(thumbnailUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.width(12.dp))
            } else {
                Card(
                    modifier = Modifier.size(60.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
            }
            
            // Document Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (categoryName != null) {
                        AssistChip(
                            onClick = { },
                            label = { Text(categoryName, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = dateFormatter.format(Date(document.updatedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (!selectionMode) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
