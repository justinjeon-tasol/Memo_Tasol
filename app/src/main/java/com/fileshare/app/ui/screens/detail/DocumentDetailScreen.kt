package com.fileshare.app.ui.screens.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fileshare.app.domain.model.Document
import com.fileshare.app.ui.components.ZoomableImageDialog
import com.fileshare.app.util.FileUtils
import com.fileshare.app.viewmodel.CategoryViewModel
import com.fileshare.app.viewmodel.DocumentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DocumentDetailScreen(
    documentId: String,
    documentViewModel: DocumentViewModel,
    categoryViewModel: CategoryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val document by documentViewModel.getDocumentById(documentId).collectAsState(initial = null)
    val categories by categoryViewModel.categories.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()) }

    document?.let { doc ->
        val category = categories.find { it.id == doc.categoryId }
        val pagerState = rememberPagerState(pageCount = { doc.imageUris.size })
        var showImageDialog by remember { mutableStateOf(false) }
        var selectedImageIndex by remember { mutableStateOf(0) }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("문서 상세") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, "뒤로")
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            // 편집 화면으로 가기 전에 캐시 비우기 - 수정 후 돌아왔을 때 새 데이터 로드
                            documentViewModel.clearDocumentCache(documentId)
                            onNavigateToEdit(documentId) 
                        }) {
                            Icon(Icons.Default.Edit, "수정")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "삭제")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Pager
                if (doc.imageUris.isNotEmpty()) {
                    Box {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                        ) { page ->
                            val fileUri = doc.imageUris[page]
                            val isPdf = fileUri.endsWith(".pdf", ignoreCase = true)
                            val isRemoteUrl = fileUri.startsWith("http://") || fileUri.startsWith("https://")
                            
                            // 원격 URL이거나 로컬 파일이 존재하는 경우 표시
                            if (isRemoteUrl || FileUtils.fileExists(fileUri)) {
                                if (isPdf) {
                                    // PDF Icon
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp)
                                            .clickable {
                                                if (!isRemoteUrl) {
                                                    FileUtils.openFile(context, fileUri)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PictureAsPdf,
                                                contentDescription = "PDF",
                                                modifier = Modifier.size(100.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                            Text(
                                                "PDF 파일",
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                            Text(
                                                "클릭하여 보기",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                } else {
                                    // Image - URL인 경우 URL 문자열로, 로컬 파일인 경우 File 객체로 로드
                                    AsyncImage(
                                        model = if (isRemoteUrl) fileUri else java.io.File(fileUri),
                                        contentDescription = "${doc.title} - 이미지 ${page + 1}",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedImageIndex = page
                                                showImageDialog = true
                                            },
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BrokenImage,
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                        
                        // Page indicator
                        if (doc.imageUris.size > 1) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "${pagerState.currentPage + 1} / ${doc.imageUris.size}",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text("이미지 없음", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                HorizontalDivider()

                // Document Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    // Category
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Category, null, modifier = Modifier.size(20.dp))
                        Text(
                            text = category?.name ?: "미분류",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // Memo
                    if (!doc.memo.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Notes, null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("메모", style = MaterialTheme.typography.titleSmall)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = doc.memo,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Metadata
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetadataRow("이미지 개수", "${doc.imageUris.size}개")
                        MetadataRow("생성일", dateFormatter.format(Date(doc.createdAt)))
                        MetadataRow("수정일", dateFormatter.format(Date(doc.updatedAt)))
                        if (doc.shareCount > 0) {
                            MetadataRow("공유 횟수", "${doc.shareCount}회")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Share Button
                    Button(
                        onClick = {
                            if (doc.imageUris.isNotEmpty()) {
                                FileUtils.shareMultipleFiles(context, doc.imageUris)
                                documentViewModel.incrementShareCount(documentId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = doc.imageUris.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("공유하기")
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = { Icon(Icons.Default.Warning, null) },
                title = { Text("이 문서를 삭제하시겠습니까?") },
                text = { Text("삭제된 문서는 복구할 수 없습니다.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            documentViewModel.deleteDocument(doc)
                            showDeleteDialog = false
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("삭제")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("취소")
                    }
                }
            )
        }
        
        // Zoomable Image Dialog
        if (showImageDialog && doc.imageUris.isNotEmpty()) {
            val imageUri = doc.imageUris[selectedImageIndex]
            val isRemoteUrl = imageUri.startsWith("http://") || imageUri.startsWith("https://")
            if (isRemoteUrl || FileUtils.fileExists(imageUri)) {
                ZoomableImageDialog(
                    imageData = if (isRemoteUrl) imageUri else java.io.File(imageUri),
                    onDismiss = { showImageDialog = false }
                )
            }
        }
    } ?: run {
        // Loading or not found
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
