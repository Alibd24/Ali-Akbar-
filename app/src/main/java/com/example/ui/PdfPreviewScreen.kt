package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FormProduct
import com.example.data.GroupMeta
import com.example.data.WithdrawalForm

@Composable
fun PdfPreviewScreen(
    form: WithdrawalForm,
    modifier: Modifier = Modifier
) {
    val totalProducts = form.products.size
    val itemsPerPage = 10
    val totalPages = Math.max(1, Math.ceil(totalProducts.toDouble() / itemsPerPage).toInt())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Print Preview (Page 1 to $totalPages)",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Text(
            text = "Below is the rendered pagination. Tap 'Print / Save PDF' to export.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp),
            textAlign = TextAlign.Center
        )

        for (pageIndex in 0 until totalPages) {
            val startIndex = pageIndex * itemsPerPage
            val endIndex = Math.min(startIndex + itemsPerPage, totalProducts)
            val chunk = form.products.subList(startIndex, endIndex)

            A4SheetPreview(
                form = form,
                pageIndex = pageIndex,
                totalPages = totalPages,
                productsChunk = chunk
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun A4SheetPreview(
    form: WithdrawalForm,
    pageIndex: Int,
    totalPages: Int,
    productsChunk: List<FormProduct>
) {
    val groups = listOf("UHP", "UMP", "UD", "NUV", "S&N")
    
    // Compute group values specifically for this page chunk
    val pageGroupVal = groups.associateWith { 0.0 }.toMutableMap()
    var pageGrandTotal = 0.0
    
    productsChunk.forEach { p ->
        val v = p.value
        if (pageGroupVal.containsKey(p.group)) {
            pageGroupVal[p.group] = pageGroupVal[p.group]!! + v
        }
        pageGrandTotal += v
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .aspectRatio(0.707f) // International A4 ratio (1 : 1.414)
            .background(Color.White)
            .border(1.dp, Color.LightGray),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upper Letterhead Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Black)
                    .height(55.dp)
            ) {
                // Logo & Name Block
                Column(
                    modifier = Modifier
                        .weight(0.22f)
                        .padding(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("UniMed UniHealth", fontWeight = FontWeight.Bold, fontSize = 7.sp, color = Color.Black, textAlign = TextAlign.Center)
                    Text("Pharmaceuticals Ltd.", fontSize = 5.5.sp, color = Color.Black, textAlign = TextAlign.Center)
                }
                
                // Vertical divider
                Box(modifier = Modifier.width(1.dp).fillMaxSize().background(Color.Black))
                
                // Form title
                Column(
                    modifier = Modifier
                        .weight(0.53f)
                        .padding(2.dp)
                        .align(Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EXPIRY PRODUCT WITHDRAWAL FORM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
                
                // Vertical divider
                Box(modifier = Modifier.width(1.dp).fillMaxSize().background(Color.Black))
                
                // Form attributes block
                Column(
                    modifier = Modifier
                        .weight(0.25f)
                        .padding(2.dp)
                ) {
                    Text("F-MRK-8.2.1-006", fontSize = 6.5.sp, color = Color.Black)
                    Divider(color = Color.Black, thickness = 0.5.dp)
                    Text("Revision: See QISS", fontSize = 6.5.sp, color = Color.Black)
                    Divider(color = Color.Black, thickness = 0.5.dp)
                    Text("Issue Date: See QISS", fontSize = 6.5.sp, color = Color.Black)
                    Divider(color = Color.Black, thickness = 0.5.dp)
                    Text("Page ${pageIndex + 1} of $totalPages", fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "*no replacement will be made on expired imported range of products*",
                fontStyle = FontStyle.Italic,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Sales center info row
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text("Name of the Sales Center: ", fontSize = 8.sp, color = Color.Black)
                Text(
                    text = form.salesCenter.ifBlank { "..................................................." },
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Text("Date: ", fontSize = 8.sp, color = Color.Black)
                Text(
                    text = form.formDate.replace("-", "/").ifBlank { "........................" },
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("List of expired products:", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Main products Grid Table
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(0.5.dp, Color.Black)
            ) {
                // Table Headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF2F2F2))
                        .border(0.5.dp, Color.Black)
                ) {
                    TableCell("Product Name", weight = 0.35f, isHeader = true)
                    TableCell("Pack", weight = 0.08f, isHeader = true)
                    TableCell("Batch No", weight = 0.11f, isHeader = true)
                    TableCell("Mfg. Date", weight = 0.11f, isHeader = true)
                    TableCell("Exp. Date", weight = 0.11f, isHeader = true)
                    TableCell("Qty", weight = 0.08f, isHeader = true)
                    TableCell("TP", weight = 0.08f, isHeader = true)
                    TableCell("Value", weight = 0.08f, isHeader = true)
                }

                // Table Rows (10 fixed slots for consistency)
                for (i in 0 until 10) {
                    val p = productsChunk.getOrNull(i)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(0.2.dp, Color.Black)
                    ) {
                        TableCell(p?.name ?: "", weight = 0.35f)
                        TableCell(p?.pack?.toString() ?: "", weight = 0.08f)
                        TableCell(p?.batch ?: "", weight = 0.11f)
                        TableCell(p?.mfg?.replace("-", "/") ?: "", weight = 0.11f)
                        TableCell(p?.exp?.replace("-", "/") ?: "", weight = 0.11f)
                        TableCell(p?.qty?.toString() ?: "", weight = 0.08f)
                        TableCell(p?.let { String.format("%.2f", it.tp) } ?: "", weight = 0.08f)
                        TableCell(p?.let { String.format("%.2f", it.value) } ?: "", weight = 0.08f)
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Note: Loose strip and sample pack will not be accepted.", fontSize = 6.5.sp, color = Color.Black, fontStyle = FontStyle.Italic)
            }
            Spacer(modifier = Modifier.height(3.dp))

            // Chemist details
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text("Expired products received from the Chemist: Name & Address: ", fontSize = 7.5.sp, color = Color.Black)
                Text(
                    text = form.chemistDetails.ifBlank { "...................................................................................................................................." },
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Signatories Row
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Text("Form filled by: ", fontSize = 7.sp, color = Color.Black)
                    Text(form.filledBy.ifBlank { "......................................................" }, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1.5f))
                    Text("Mobile No. ", fontSize = 7.sp, color = Color.Black)
                    Text(form.filledByMobile.ifBlank { "................................." }, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Text("Handed over by: ", fontSize = 7.sp, color = Color.Black)
                    Text(form.handedBy.ifBlank { "......................................................" }, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1.5f))
                    Text("Taken over by: ", fontSize = 7.sp, color = Color.Black)
                    Text(form.takenBy.ifBlank { "......................................................" }, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1.5f))
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Text("Handed over Mobile: ", fontSize = 6.sp, color = Color.Gray)
                    Text(form.handedByMobile.ifBlank { "........................................." }, fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1.5f))
                    Text("Taken over Mobile: ", fontSize = 6.sp, color = Color.Gray)
                    Text(form.takenByMobile.ifBlank { "........................................." }, fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1.5f))
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Dynamic group summaries & MPO table in A4 style
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Color.Black)
            ) {
                // Table Row header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF2F2F2))
                        .padding(vertical = 1.dp)
                ) {
                    TableCell("Business Group Value (Tk)", weight = 0.3f, isHeader = true)
                    TableCell("Full Name of MPO", weight = 0.25f, isHeader = true)
                    TableCell("Full Name of ASM", weight = 0.25f, isHeader = true)
                    TableCell("Action Taken", weight = 0.2f, isHeader = true)
                }

                // Render groups UHP, UMP, UD, NUV, S&N
                groups.forEach { g ->
                    val sum = pageGroupVal[g] ?: 0.0
                    val meta = form.groupMetaMap[g] ?: GroupMeta("", "", "")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.1.dp, Color.Black)
                            .padding(vertical = 1.dp)
                    ) {
                        TableCell("$g Tk: ${if (sum > 0) String.format("%.2f", sum) else ""}", weight = 0.3f, isBold = true)
                        TableCell(meta.mpo, weight = 0.25f)
                        TableCell(meta.asm, weight = 0.25f)
                        TableCell(meta.action, weight = 0.2f)
                    }
                }

                // Render Grand total
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA))
                        .padding(vertical = 2.dp)
                ) {
                    TableCell("Total Tk: ${if (pageGrandTotal > 0) String.format("%.2f", pageGrandTotal) else ""}", weight = 0.3f, isBold = true)
                    TableCell("", weight = 0.25f)
                    TableCell("", weight = 0.25f)
                    TableCell("", weight = 0.2f)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "To Sales Center In-charge. Please arrange to collect expired products from chemist as list and ensure equivalent replacement as per group rules.",
                fontSize = 5.5.sp,
                color = Color.Black,
                textAlign = TextAlign.Justify
            )

            // Territory representative signature line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.width(130.dp).height(0.5.dp).background(Color.Black))
                    Text("Sr. Sales Team Member of concerned territory", fontSize = 5.5.sp, color = Color.Black)
                }
            }

            // Replacement confirmation line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text("Replaced vide Invoice No. ", fontSize = 6.5.sp, color = Color.Black)
                Text(form.invoiceNo.ifBlank { "......................" }, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(" Dated: ", fontSize = 6.5.sp, color = Color.Black)
                Text(form.invoiceDate.replace("-", "/").ifBlank { "..............." }, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                
                Spacer(modifier = Modifier.weight(1f))
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.width(110.dp).height(0.5.dp).background(Color.Black))
                    Text("Signature of SCM with date", fontSize = 5.5.sp, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Uncontrolled footer disclaimer box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Black)
                    .height(35.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.2f)
                        .padding(2.dp)
                ) {
                    Text("Reviewed by:", fontSize = 5.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("SIT QSS", fontSize = 5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Box(modifier = Modifier.width(1.dp).fillMaxSize().background(Color.Black))
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .padding(2.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = "If QISS cover sheet is not attached, then printed copies are uncontrolled. Controlled copies must include the cover sheet stamped with 'CONTROLLED COPY' in red with signature.",
                        fontSize = 4.5.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
                Box(modifier = Modifier.width(1.dp).fillMaxSize().background(Color.Black))
                Column(
                    modifier = Modifier
                        .weight(0.2f)
                        .padding(2.dp)
                ) {
                    Text("Approved by:", fontSize = 5.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("SEE QSS", fontSize = 5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    isBold: Boolean = false
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxSize()
            .padding(1.dp),
        contentAlignment = if (isHeader) Alignment.Center else Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = if (isHeader) 6.sp else 6.5.sp,
            fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
            color = Color.Black,
            textAlign = if (isHeader) TextAlign.Center else TextAlign.Start,
            maxLines = 2
        )
    }
}
