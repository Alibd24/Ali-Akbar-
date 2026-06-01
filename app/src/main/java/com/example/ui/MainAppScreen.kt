package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FormProduct
import com.example.data.GroupMeta
import com.example.data.WithdrawalForm
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.theme.VibrantSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeForm by viewModel.activeForm.collectAsState()
    val isScanning by viewModel.isAiScanning.collectAsState()
    val isFormatting by viewModel.isAiFormatting.collectAsState()

    // Dialog state
    var showDeleteConfirmDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "UUPL Form Manager",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ali Akbar's Dashboard",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    if (currentScreen == Screen.Preview && activeForm != null) {
                        Button(
                            onClick = { PrintHelper.printForm(context, activeForm!!) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Print / Save PDF", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        },
        bottomBar = {
            AppNavigationMenu(
                currentScreen = currentScreen,
                activeForm = activeForm,
                onScreenSelected = { viewModel.navigateTo(it) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background) // Vibrant Palette Light Background
        ) {
            when (currentScreen) {
                Screen.Dashboard -> DashboardView(viewModel)
                Screen.List -> FormListView(
                    viewModel = viewModel,
                    onDeleteRequested = { showDeleteConfirmDialog = it }
                )
                Screen.Details -> {
                    if (activeForm != null) {
                        FormDetailsView(viewModel, activeForm!!, isFormatting)
                    } else {
                        viewModel.navigateTo(Screen.List)
                    }
                }
                Screen.Products -> {
                    if (activeForm != null) {
                        FormProductsView(viewModel, activeForm!!, isScanning)
                    } else {
                        viewModel.navigateTo(Screen.List)
                    }
                }
                Screen.Preview -> {
                    if (activeForm != null) {
                        PdfPreviewScreen(
                            form = activeForm!!,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        viewModel.navigateTo(Screen.List)
                    }
                }
            }

            // Global deleting confirmation dialog
            showDeleteConfirmDialog?.let { deletingId ->
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = null },
                    title = { Text("ফর্ম মুছে ফেলুন", fontWeight = FontWeight.Bold) },
                    text = { Text("আপনি কি নিশ্চিতভাবে এই ফর্ম তথ্যসমূহ সম্পূর্ণ মুছে ফেলতে চান? এটি আর ফেরত আনা সম্ভব নয়।") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteForm(deletingId)
                                showDeleteConfirmDialog = null
                                Toast.makeText(context, "ফর্ম মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                            },
                        ) {
                            Text("হ্যাঁ (Delete)", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = null }) {
                            Text("না (Cancel)")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AppNavigationMenu(
    currentScreen: Screen,
    activeForm: WithdrawalForm?,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.Dashboard,
            onClick = { onScreenSelected(Screen.Dashboard) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("ড্যাশবোর্ড", fontSize = 10.sp) },
            modifier = Modifier.testTag("nav_dashboard")
        )
        NavigationBarItem(
            selected = currentScreen == Screen.List,
            onClick = { onScreenSelected(Screen.List) },
            icon = { Icon(Icons.Default.List, contentDescription = "List") },
            label = { Text("ফর্ম তালিকা", fontSize = 10.sp) },
            modifier = Modifier.testTag("nav_list")
        )
        
        // Form editing tabs (active dynamically)
        if (activeForm != null) {
            NavigationBarItem(
                selected = currentScreen == Screen.Details,
                onClick = { onScreenSelected(Screen.Details) },
                icon = { Icon(Icons.Default.Info, contentDescription = "Details") },
                label = { Text("১. বিস্তারিত", fontSize = 10.sp) },
                modifier = Modifier.testTag("nav_details")
            )
            NavigationBarItem(
                selected = currentScreen == Screen.Products,
                onClick = { onScreenSelected(Screen.Products) },
                icon = { Icon(Icons.Default.Build, contentDescription = "Products") },
                label = { Text("২. প্রোডাক্টস", fontSize = 10.sp) },
                modifier = Modifier.testTag("nav_products")
            )
            NavigationBarItem(
                selected = currentScreen == Screen.Preview,
                onClick = { onScreenSelected(Screen.Preview) },
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Preview") },
                label = { Text("৩. প্রিভিউ", fontSize = 10.sp) },
                modifier = Modifier.testTag("nav_preview")
            )
        }
    }
}

// ================= MODULE 0: DASHBOARD =================
@Composable
fun DashboardView(viewModel: MainViewModel) {
    val forms by viewModel.forms.collectAsState()
    val monthFilter by viewModel.dashboardMonthFilter.collectAsState()
    val context = LocalContext.current

    // Dynamically query calculated summaries
    val totalFormsCount = forms.size
    val totalProductsCount = forms.sumOf { it.products.size }
    val totalWithdrawalValue = forms.sumOf { it.totalValue }

    // Grouping months: parse and collect dates
    val uniqueMonths = remember(forms) {
        forms.mapNotNull {
            if (it.formDate.length >= 7) {
                it.formDate.take(7) // YYYY-MM
            } else null
        }.distinct().sortedDescending()
    }

    // Dynamic month calculation
    val filteredFormsResult = remember(forms, monthFilter) {
        if (monthFilter == "all") forms
        else forms.filter { it.formDate.startsWith(monthFilter) }
    }

    val monthTotalValue = filteredFormsResult.sumOf { it.totalValue }

    // Group aggregates: UHP, UMP, UD, NUV, S&N, Others
    val groupSumResult = remember(filteredFormsResult) {
        val sums = mutableMapOf("UHP" to 0.0, "UMP" to 0.0, "UD" to 0.0, "NUV" to 0.0, "S&N" to 0.0, "Others" to 0.0)
        filteredFormsResult.forEach { form ->
            form.products.forEach { product ->
                val grp = if (sums.containsKey(product.group)) product.group else "Others"
                sums[grp] = (sums[grp] ?: 0.0) + product.value
            }
        }
        sums.entries.sortedByDescending { it.value }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Quick summary metric blocks
        Text(
            text = "স্ট্যাটিসটিক্স ওভারভিউ (Overview)",
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DashboardItemCard(
                title = "সেভকৃত ফর্ম",
                value = totalFormsCount.toString(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            DashboardItemCard(
                title = "মোট প্রোডাক্ট",
                value = totalProductsCount.toString(),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        DashboardItemCard(
            title = "সর্বমোট উইথড্রাল ভ্যালু (মোট)",
            value = "Tk. ${String.format("%,.2f", totalWithdrawalValue)}",
            containerColor = MaterialTheme.colorScheme.primary,
            textColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Month filter picker card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "মাস ভিত্তিক রিটার্ন ভ্যালু",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "নির্দিষ্ট মাস সিলেক্ট করে ওই মাসের রিটার্ন ভ্যালু চেক করুন।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Customizable selection dropdown Spinner
                var expandedDropdown by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .clickable { expandedDropdown = true }
                        .padding(12.dp)
                ) {
                    val label = if (monthFilter == "all") {
                        "সব মাস (All Months)"
                    } else {
                        getFormattedMonthName(monthFilter)
                    }
                    Text(
                        text = "সিলেক্টেড: $label ▾",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    DropdownSpinnerMenu(
                        expanded = expandedDropdown,
                        uniqueMonths = uniqueMonths,
                        onDismiss = { expandedDropdown = false },
                        onSelected = {
                            viewModel.setDashboardMonthFilter(it)
                            expandedDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Business Group return summaries
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "গ্রুপ ভিত্তিক ভ্যালু সামারি",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (monthFilter == "all") "All Months" else getFormattedMonthName(monthFilter),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                groupSumResult.forEach { (group, valSum) ->
                    val percentage = if (monthTotalValue > 0) (valSum / monthTotalValue).toFloat() else 0f
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = group, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "Tk. ${String.format("%,.2f", valSum)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Styled progress visual matching group colors
                        val progressColor = when (group) {
                            "UHP" -> Color(0xFF6750A4)
                            "UMP" -> Color(0xFF4F378B)
                            "UD" -> Color(0xFFD0BCFF)
                            "NUV" -> Color(0xFFE0A000)
                            "S&N" -> Color(0xFF386A20)
                            else -> Color(0xFF79747E)
                        }

                        LinearProgressIndicator(
                            progress = { percentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = progressColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text(
                                text = "${String.format("%.1f", percentage * 100)}% of total",
                                fontSize = 9.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Divider(color = Color(0xFFECEFF1), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("মোট সিলেক্টেড ভ্যালু", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = "Tk. ${String.format("%,.2f", monthTotalValue)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun DropdownSpinnerMenu(
    expanded: Boolean,
    uniqueMonths: List<String>,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    Box {
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("সব মাস (All Months)", fontWeight = FontWeight.SemiBold) },
                onClick = { onSelected("all") }
            )
            uniqueMonths.forEach { m ->
                DropdownMenuItem(
                    text = { Text(getFormattedMonthName(m), fontWeight = FontWeight.SemiBold) },
                    onClick = { onSelected(m) }
                )
            }
        }
    }
}

fun getFormattedMonthName(yearMonth: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM", Locale.US)
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.US)
        val d = parser.parse(yearMonth)
        if (d != null) formatter.format(d) else yearMonth
    } catch (e: Exception) {
        yearMonth
    }
}

@Composable
fun DashboardItemCard(
    title: String,
    value: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textColor.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = textColor)
        }
    }
}

// ================= MODULE 1: FORM LIST =================
@Composable
fun FormListView(
    viewModel: MainViewModel,
    onDeleteRequested: (String) -> Unit
) {
    val filteredForms by viewModel.filteredForms.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Create new button
        Button(
            onClick = { viewModel.createNewForm() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_create_form"),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("নতুন ফর্ম তৈরি করুন", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search text-field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_field"),
            placeholder = { Text("Chemist বা Pharmacy-র নাম দিয়ে খুঁজুন...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF0284C7),
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredForms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "কোনো ফর্ম পাওয়া যায়নি",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "নতুন ফর্ম তৈরি করতে উপরের বাটনে ক্লিক করুন।",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredForms, key = { it.id }) { form ->
                    FormItemRow(
                        form = form,
                        onEdit = { viewModel.editForm(form) },
                        onDelete = { onDeleteRequested(form.id) },
                        onPreviewDirectly = {
                            viewModel.editForm(form)
                            viewModel.navigateTo(Screen.Preview)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FormItemRow(
    form: WithdrawalForm,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPreviewDirectly: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = form.chemistDetails.ifBlank { "Unnamed Chemist / Pharmacy" },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Date: ${form.formDate.ifBlank { "N/A" }}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${form.products.size} Items added",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = "Tk. ${String.format("%,.2f", form.totalValue)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = VibrantSuccess
                )
            }

            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onPreviewDirectly,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Preview",
                        tint = VibrantSuccess
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// ================= MODULE 2: FORM DETAILS =================
@Composable
fun FormDetailsView(
    viewModel: MainViewModel,
    form: WithdrawalForm,
    isFormatting: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "General Coordinates",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Sales center
                OutlinedTextField(
                    value = form.salesCenter,
                    onValueChange = { newValue ->
                        viewModel.updateActiveForm { it.copy(salesCenter = newValue) }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("field_sales_center"),
                    label = { Text("Name of the Sales Center") },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date picker
                OutlinedTextField(
                    value = form.formDate,
                    onValueChange = { newValue ->
                        viewModel.updateActiveForm { it.copy(formDate = newValue) }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("field_date"),
                    label = { Text("Date (e.g. YYYY-MM-DD)") },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Chemist details textarea with AI Format
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chemist Info & Full Address",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    Button(
                        onClick = { viewModel.formatChemistDetails() },
                        enabled = !isFormatting && form.chemistDetails.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        if (isFormatting) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color.White, strokeWidth = 1.5.dp)
                        } else {
                            Text("✨ Auto-Format", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = form.chemistDetails,
                    onValueChange = { newValue ->
                        viewModel.updateActiveForm { it.copy(chemistDetails = newValue) }
                    },
                    modifier = Modifier.fillMaxWidth().height(80.dp).testTag("field_chemist_details"),
                    placeholder = { Text("Enter chemist pharmacy title and full address location...", fontSize = 12.sp) },
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Signatures & Representatives",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = form.filledBy,
                    onValueChange = { newValue ->
                        viewModel.updateActiveForm { it.copy(filledBy = newValue) }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("field_filled_by"),
                    label = { Text("Form filled by MPO/ASM (Full Name)") },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = form.filledByMobile,
                    onValueChange = { newValue ->
                        viewModel.updateActiveForm { it.copy(filledByMobile = newValue) }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("field_filled_mobile"),
                    label = { Text("Mobile No.") },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                OutlinedTextField(
                    value = form.handedBy,
                    onValueChange = { newValue ->
                        viewModel.updateActiveForm { it.copy(handedBy = newValue) }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("field_handed_by"),
                    label = { Text("Handed over by Chemist Representative") },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = form.handedByMobile,
                    onValueChange = { newValue ->
                        viewModel.updateActiveForm { it.copy(handedByMobile = newValue) }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("field_handed_mobile"),
                    label = { Text("Mobile No.") },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                OutlinedTextField(
                    value = form.takenBy,
                    onValueChange = { newValue ->
                        viewModel.updateActiveForm { it.copy(takenBy = newValue) }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("field_taken_by"),
                    label = { Text("Taken over by UUPL Representative") },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = form.takenByMobile,
                    onValueChange = { newValue ->
                        viewModel.updateActiveForm { it.copy(takenByMobile = newValue) }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("field_taken_mobile"),
                    label = { Text("Mobile No.") },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                OutlinedTextField(
                    value = form.invoiceNo,
                    onValueChange = { newValue ->
                        viewModel.updateActiveForm { it.copy(invoiceNo = newValue) }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("field_invoice"),
                    label = { Text("Replaced vide invoice No.") },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = form.invoiceDate,
                    onValueChange = { newValue ->
                        viewModel.updateActiveForm { it.copy(invoiceDate = newValue) }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("field_invoice_date"),
                    label = { Text("Invoice Date (YYYY-MM-DD)") },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Business Groups Personnel details",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Custom MPO details inline editable grid list
        val groupsKey = listOf("UHP", "UMP", "UD", "NUV", "S&N")
        groupsKey.forEach { grp ->
            val meta = form.groupMetaMap[grp] ?: GroupMeta("", "", "")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Group: $grp", fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = meta.mpo,
                            onValueChange = { newValue ->
                                viewModel.updateActiveForm { current ->
                                    val updatedMap = current.groupMetaMap.toMutableMap()
                                    updatedMap[grp] = meta.copy(mpo = newValue)
                                    current.copy(groupMetaMap = updatedMap)
                                }
                            },
                            modifier = Modifier.weight(1f).height(45.dp),
                            placeholder = { Text("MPO Name", fontSize = 11.sp, color = Color.Gray) },
                            singleLine = true,
                            shape = RoundedCornerShape(4.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                        )
                        OutlinedTextField(
                            value = meta.asm,
                            onValueChange = { newValue ->
                                viewModel.updateActiveForm { current ->
                                    val updatedMap = current.groupMetaMap.toMutableMap()
                                    updatedMap[grp] = meta.copy(asm = newValue)
                                    current.copy(groupMetaMap = updatedMap)
                                }
                            },
                            modifier = Modifier.weight(1f).height(45.dp),
                            placeholder = { Text("ASM Name", fontSize = 11.sp, color = Color.Gray) },
                            singleLine = true,
                            shape = RoundedCornerShape(4.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = meta.action,
                        onValueChange = { newValue ->
                            viewModel.updateActiveForm { current ->
                                val updatedMap = current.groupMetaMap.toMutableMap()
                                updatedMap[grp] = meta.copy(action = newValue)
                                current.copy(groupMetaMap = updatedMap)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        placeholder = { Text("Action Taken coordinates", fontSize = 11.sp, color = Color.Gray) },
                        singleLine = true,
                        shape = RoundedCornerShape(4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save row buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.navigateTo(Screen.List) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Save & Back", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { viewModel.navigateTo(Screen.Products) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Next: Products ➜", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ================= MODULE 3: PRODUCTS VIEW =================
@Composable
fun FormProductsView(
    viewModel: MainViewModel,
    form: WithdrawalForm,
    isScanning: Boolean
) {
    val context = LocalContext.current
    var isAddingProduct by remember { mutableStateOf<Int?>(null) } // index to edit, if -1 it's new, null is hidden

    // Photo picker intent launcher
    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // Read Base64 representation of selected asset
            try {
                val base64Data = compressAndConvertUriToBase64(context, uri)
                if (base64Data != null) {
                    val contentResolver = context.contentResolver
                    val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                    viewModel.scanInvoiceImage(
                        base64Image = base64Data,
                        mimeType = mimeType,
                        onSuccess = { count ->
                            Toast.makeText(context, "AI found $count items!", Toast.LENGTH_SHORT).show()
                        },
                        onError = {
                            Toast.makeText(context, "AI scanning failed. Please enter manually.", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image files.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Action headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Expired Items", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Add or Scan expired medicines list details", fontSize = 11.sp, color = Color.Gray)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { pickVisualMediaLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        ) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary), // Secondary theme for camera/scan
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("✨ Scan Invoice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { isAddingProduct = -1 },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text("Add Item", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (form.products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "কোনো প্রোডাক্ট অ্যাড করা হয়নি", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(
                            text = "উপরের বাটন ট্যাপ করে প্রোডাক্ট প্রবেশ করান বা ফটো স্ক্যান করুন।",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(form.products) { index, product ->
                        ProductItemRow(
                            product = product,
                            onEdit = { isAddingProduct = index },
                            onDelete = { viewModel.deleteProduct(index) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(Screen.Details) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Back to Details", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { viewModel.navigateTo(Screen.Preview) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Preview Form ➜", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Full-screen overlay loading state for AI Scanner
        if (isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.widthIn(max = 280.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4F46E5), strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "AI-তে তথ্য বের করা হচ্ছে...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "আপনার ফটোটি পড়ে প্রোডাক্টস এবং ভ্যালুসমূহ প্রসেস করা হচ্ছে। অনুগ্রহ করে ক্ষনিক অপেক্ষা করুন...",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

        // Add/Edit Product dialogue overlay
        if (isAddingProduct != null) {
            val editingIndex = isAddingProduct!!
            val initialProduct = if (editingIndex >= 0) form.products.getOrNull(editingIndex) else null
            
            ProductEditDialog(
                editingProduct = initialProduct,
                onDismiss = { isAddingProduct = null },
                onSave = { p, addAnother ->
                    if (editingIndex >= 0) {
                        viewModel.updateProduct(editingIndex, p)
                        isAddingProduct = null
                    } else {
                        viewModel.addProduct(p)
                        if (!addAnother) {
                            isAddingProduct = null
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ProductItemRow(
    product: FormProduct,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = product.group,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Batch: ${product.batch.ifBlank { "N/A" }}", fontSize = 10.sp, color = Color.Gray)
                    Text(text = "Qty: ${product.qty}", fontSize = 10.sp, color = Color.Gray)
                    Text(text = "Pack: ${product.pack}", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Tk. ${String.format("%,.2f", product.value)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onEdit() }
                            .padding(2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDelete() }
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditDialog(
    editingProduct: FormProduct?,
    onDismiss: () -> Unit,
    onSave: (FormProduct, Boolean) -> Unit
) {
    var pName by remember { mutableStateOf(editingProduct?.name ?: "") }
    var pGroup by remember { mutableStateOf(editingProduct?.group ?: "UHP") }
    var pBatch by remember { mutableStateOf(editingProduct?.batch ?: "") }
    var pMfg by remember { mutableStateOf(editingProduct?.mfg ?: "") }
    var pExp by remember { mutableStateOf(editingProduct?.exp ?: "") }
    var pPack by remember { mutableStateOf(editingProduct?.pack?.toString() ?: "30") }
    var pQty by remember { mutableStateOf(editingProduct?.qty?.toString() ?: "1") }
    var pTP by remember { mutableStateOf(editingProduct?.tp?.toString() ?: "0.0") }

    // Calculated derived values
    val calcValue by remember {
        derivedStateOf {
            val tp = pTP.toDoubleOrNull() ?: 0.0
            val pack = pPack.toIntOrNull() ?: 1
            val qty = pQty.toDoubleOrNull() ?: 0.0
            if (pack > 0) (tp / pack) * qty else 0.0
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = if (editingProduct != null) "Edit Product" else "Add Expired Product",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Fields
                OutlinedTextField(
                    value = pName,
                    onValueChange = { pName = it },
                    label = { Text("Product Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("add_product_name"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Spinner Group selection
                Text("Product Group *", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                
                var dropdownExpanded by remember { mutableStateOf(false) }
                val groups = listOf("UHP", "UMP", "UD", "NUV", "S&N", "Others")
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                        .clickable { dropdownExpanded = true }
                        .padding(12.dp)
                ) {
                    Text(text = "Selected Group: $pGroup ▾", fontWeight = FontWeight.Bold, color = Color.Black)
                    androidx.compose.material3.DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        groups.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = {
                                    pGroup = g
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pBatch,
                    onValueChange = { pBatch = it },
                    label = { Text("Batch No.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pMfg,
                        onValueChange = { pMfg = it },
                        label = { Text("Mfg. Date (YYYY-MM)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = pExp,
                        onValueChange = { pExp = it },
                        label = { Text("Expiry Date (YYYY-MM)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pPack,
                    onValueChange = { pPack = it },
                    label = { Text("Pack Size *") },
                    modifier = Modifier.fillMaxWidth().testTag("add_product_pack"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pQty,
                        onValueChange = { pQty = it },
                        label = { Text("Qty *") },
                        modifier = Modifier.weight(1f).testTag("add_product_qty"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = pTP,
                        onValueChange = { pTP = it },
                        label = { Text("Trade Price (Tk) *") },
                        modifier = Modifier.weight(1f).testTag("add_product_tp"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Calculated visual block matching user web spec: (tp / pack) * qty
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Calculated Value (Tk)", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            Text("(Trade Price ÷ Pack Size) × Qty", fontSize = 8.sp, color = Color.Gray)
                        }

                        Text(
                            text = "Tk. ${String.format("%,.2f", calcValue)}",
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(0.7f)) {
                        Text("Cancel")
                    }
                    
                    if (editingProduct == null) {
                        Button(
                            onClick = {
                                val pack = pPack.toIntOrNull() ?: 1
                                val qty = pQty.toDoubleOrNull() ?: 0.0
                                val tp = pTP.toDoubleOrNull() ?: 0.0
                                if (pName.isNotBlank() && qty > 0 && tp >= 0) {
                                    val newP = FormProduct(pName, pGroup, pBatch, pMfg, pExp, pack, qty, tp)
                                    onSave(newP, true) // Save and add another
                                    // Reset fields to help them load consecutive list
                                    pName = ""
                                    pBatch = ""
                                    pMfg = ""
                                    pExp = ""
                                    pQty = "1"
                                    pTP = "0.0"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Add Another", fontSize = 10.sp)
                        }
                    }

                    Button(
                        onClick = {
                            val pack = pPack.toIntOrNull() ?: 1
                            val qty = pQty.toDoubleOrNull() ?: 0.0
                            val tp = pTP.toDoubleOrNull() ?: 0.0
                            if (pName.isNotBlank() && qty > 0 && tp >= 0) {
                                val finalP = FormProduct(pName, pGroup, pBatch, pMfg, pExp, pack, qty, tp)
                                onSave(finalP, false) // Save & Close
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Save", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// ================= UTILITY: COROUTINE GRAPHIC RESIZER AND BASE64 =================
fun compressAndConvertUriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
        
        // We resize the bitmap to a maximum threshold (e.g. 1024px width/height) to ensure faster Gemini scanning
        val maxDimension = 1024
        val width = originalBitmap.width
        val height = originalBitmap.height
        val resizedBitmap = if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            val newWidth: Int
            val newHeight: Int
            if (ratio > 1) {
                newWidth = maxDimension
                newHeight = (maxDimension / ratio).toInt()
            } else {
                newHeight = maxDimension
                newWidth = (maxDimension * ratio).toInt()
            }
            Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        } else {
            originalBitmap
        }

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        val bytes = outputStream.toByteArray()
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
