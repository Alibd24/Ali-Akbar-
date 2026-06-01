package com.example.ui

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.*

object PrintHelper {

    fun generateHtmlForForm(form: WithdrawalForm): String {
        val totalProducts = form.products.size
        val itemsPerPage = 10
        val totalPages = Math.max(1, Math.ceil(totalProducts.toDouble() / itemsPerPage).toInt())

        val groups = listOf("UHP", "UMP", "UD", "NUV", "S&N")

        var fullHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    @page { size: A4; margin: 0; }
                    body { font-family: 'Arial', sans-serif; color: black; margin: 0; padding: 0; font-size: 11px; line-height: 1.3; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
                    .print-page {
                        width: 210mm; height: 297mm; padding: 10mm; box-sizing: border-box;
                        display: flex; flex-direction: column; justify-content: space-between;
                        page-break-after: always; background: white;
                    }
                    .print-page:last-child { page-break-after: auto; }
                    .dotted-line { border-bottom: 1px dotted black; display: inline-block; min-width: 50px; }
                    table { width: 100%; border-collapse: collapse; margin-bottom: 8px; }
                    th, td { border: 1px solid black; padding: 4px; text-align: left; }
                </style>
            </head>
            <body>
        """.trimIndent()

        for (pageIndex in 0 until totalPages) {
            val startIndex = pageIndex * itemsPerPage
            val endIndex = Math.min(startIndex + itemsPerPage, totalProducts)
            val chunk = form.products.subList(startIndex, endIndex)

            // Compute page/chunk values
            val pageGroupSums = groups.associateWith { 0.0 }.toMutableMap()
            var pageTotal = 0.0
            
            chunk.forEach { p ->
                val v = p.value
                if (pageGroupSums.containsKey(p.group)) {
                    pageGroupSums[p.group] = pageGroupSums[p.group]!! + v
                }
                pageTotal += v
            }

            var tableRowsHtml = ""
            for (i in 0 until 10) {
                if (i < chunk.size) {
                    val p = chunk[i]
                    val mfgFormatted = p.mfg.replace("-", "/")
                    val expFormatted = p.exp.replace("-", "/")
                    tableRowsHtml += """
                        <tr>
                            <td style="border: 1px solid black; padding: 4px; text-align: left;">${p.name}</td>
                            <td style="border: 1px solid black; padding: 4px; text-align: center;">${p.pack}</td>
                            <td style="border: 1px solid black; padding: 4px; text-align: center;">${p.batch}</td>
                            <td style="border: 1px solid black; padding: 4px; text-align: center;">$mfgFormatted</td>
                            <td style="border: 1px solid black; padding: 4px; text-align: center;">$expFormatted</td>
                            <td style="border: 1px solid black; padding: 4px; text-align: center;">${p.qty}</td>
                            <td style="border: 1px solid black; padding: 4px; text-align: right;">${String.format("%.2f", p.tp)}</td>
                            <td style="border: 1px solid black; padding: 4px; text-align: right; padding-right: 6px;">${String.format("%.2f", p.value)}</td>
                        </tr>
                    """.trimIndent()
                } else {
                    tableRowsHtml += """
                        <tr>
                            <td style="border: 1px solid black; padding: 4px; height: 18px;">&nbsp;</td>
                            <td style="border: 1px solid black; padding: 4px;"></td>
                            <td style="border: 1px solid black; padding: 4px;"></td>
                            <td style="border: 1px solid black; padding: 4px;"></td>
                            <td style="border: 1px solid black; padding: 4px;"></td>
                            <td style="border: 1px solid black; padding: 4px;"></td>
                            <td style="border: 1px solid black; padding: 4px;"></td>
                            <td style="border: 1px solid black; padding: 4px;"></td>
                        </tr>
                    """.trimIndent()
                }
            }

            var valueTableRows = ""
            groups.forEach { g ->
                val sum = pageGroupSums[g] ?: 0.0
                val meta = form.groupMetaMap[g] ?: GroupMeta("", "", "")
                val valStr = if (sum > 0) String.format("%.2f", sum) else ""
                
                valueTableRows += """
                    <tr>
                        <td style="padding: 4px 0; border: none; vertical-align: bottom;">$g Tk.: <span class="dotted-line" style="width: 60%; font-weight: bold;">$valStr</span></td>
                        <td style="padding: 4px 0; border: none; vertical-align: bottom;"><span class="dotted-line" style="width: 90%;">${meta.mpo}</span></td>
                        <td style="padding: 4px 0; border: none; vertical-align: bottom;"><span class="dotted-line" style="width: 90%;">${meta.asm}</span></td>
                        <td style="padding: 4px 0; border: none; vertical-align: bottom;"><span class="dotted-line" style="width: 90%;">${meta.action}</span></td>
                    </tr>
                """.trimIndent()
            }

            val grandTotalStr = if (pageTotal > 0) String.format("%.2f", pageTotal) else ""
            val formDateFormatted = form.formDate.replace("-", "/")
            val invoiceDateFormatted = form.invoiceDate.replace("-", "/")

            fullHtml += """
            <div class="print-page">
                <div>
                    <!-- Header layout -->
                    <div style="display: grid; grid-template-columns: 20% 55% 25%; border: 1.5px solid black; margin-bottom: 12px; box-sizing: border-box;">
                        <div style="border-right: 1.5px solid black; padding: 5px; text-align: center; display: flex; flex-direction: column; justify-content: center; align-items: center;">
                            <svg viewBox="0 0 100 120" style="width: 32px; height: auto; margin-bottom: 4px;" xmlns="http://www.w3.org/2000/svg">
                                <rect x="8" y="8" width="34" height="34" rx="4" stroke="black" stroke-width="8" fill="none" />
                                <rect x="58" y="8" width="34" height="34" rx="4" stroke="black" stroke-width="8" fill="none" />
                                <path d="M 8,52 L 32,52 L 32,85 L 44,85 L 44,65 L 56,65 L 56,85 L 68,85 L 68,52 L 92,52 L 92,92 A 20,20 0 0 1 72,112 L 28,112 A 20,20 0 0 1 8,92 Z" stroke="black" stroke-width="8" stroke-linejoin="round" stroke-linecap="round" fill="none" />
                            </svg>
                            <div style="font-weight: bold; font-size: 10px; line-height: 1.1;">UniMed UniHealth</div>
                            <div style="font-size: 8px;">Pharmaceuticals Ltd.</div>
                        </div>
                        <div style="border-right: 1.5px solid black; padding: 5px; text-align: center; display: flex; flex-direction: column; justify-content: center;">
                            <div style="font-weight: bold; font-size: 15px;">EXPIRY PRODUCT WITHDRAWAL FORM</div>
                        </div>
                        <div style="padding: 0; display: flex; flex-direction: column; font-size: 9px;">
                            <div style="border-bottom: 1.5px solid black; padding: 3px 5px;">F-MRK-8.2.1-006</div>
                            <div style="border-bottom: 1.5px solid black; padding: 3px 5px;">Revision: See QISS</div>
                            <div style="border-bottom: 1.5px solid black; padding: 3px 5px;">Issue Date: See QISS</div>
                            <div style="padding: 3px 5px;">Page ${pageIndex + 1} of $totalPages</div>
                        </div>
                    </div>

                    <div style="text-align: center; font-weight: bold; margin-bottom: 8px; font-size: 10px;">
                        *no replacement will be made on expired imported range of products*
                    </div>

                    <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                        <div style="flex-grow: 1;">
                            Name of the Sales Center <span class="dotted-line" style="width: 60%; font-weight: bold;">${form.salesCenter}</span>
                        </div>
                        <div style="min-width: 150px; text-align: right;">
                            Date: <span class="dotted-line" style="width: 100px; font-weight: bold;">$formDateFormatted</span>
                        </div>
                    </div>

                    <div style="margin-bottom: 5px; font-weight: bold;">List of expired products:</div>

                    <table style="width: 100%; border-collapse: collapse; margin-bottom: 8px;">
                        <thead>
                            <tr style="background-color: #f2f2f2;">
                                <th style="width: 35%; text-align: left; font-weight: bold;">Products Name</th>
                                <th style="width: 8%; text-align: center; font-weight: bold;">Pack Size</th>
                                <th style="width: 10%; text-align: center; font-weight: bold;">Batch No</th>
                                <th style="width: 10%; text-align: center; font-weight: bold;">Mfg. Date</th>
                                <th style="width: 10%; text-align: center; font-weight: bold;">Expiry Date</th>
                                <th style="width: 8%; text-align: center; font-weight: bold;">Quantity</th>
                                <th style="width: 9%; text-align: right; font-weight: bold;">Trade Price</th>
                                <th style="width: 10%; text-align: right; font-weight: bold;">Value</th>
                            </tr>
                        </thead>
                        <tbody>
                            $tableRowsHtml
                        </tbody>
                    </table>

                    <div style="margin-bottom: 5px; font-size: 9px; font-style: italic;">Note: Loose strip and sample pack will not be accepted.</div>
                    <div style="margin-bottom: 8px;">
                        Expired products received from the Chemist: Name & Address: <span class="dotted-line" style="width: 65%; font-weight: bold;">${form.chemistDetails}</span>
                    </div>

                    <!-- Representatives block -->
                    <div style="display: grid; grid-template-columns: 1.5fr 1fr; gap: 8px; margin-bottom: 10px; line-height: 1.6;">
                        <div>Form filled by: <span class="dotted-line" style="width: 55%; font-weight: bold;">${form.filledBy}</span> (Full Name of MPO/ASM)</div>
                        <div>Mobile No.: <span class="dotted-line" style="width: 70%; font-weight: bold;">${form.filledByMobile}</span></div>
                        <div>Handed over by: <span class="dotted-line" style="width: 60%; font-weight: bold;">${form.handedBy}</span></div>
                        <div>Taken over by: <span class="dotted-line" style="width: 60%; font-weight: bold;">${form.takenBy}</span></div>
                        <div style="padding-left: 10px; font-size: 9px; color: #444;">On behalf of Chemist with Mobile No. <span class="dotted-line" style="width: 40%; font-weight: bold;">${form.handedByMobile}</span></div>
                        <div>On behalf of UUPL Mobile: <span class="dotted-line" style="width: 60%; font-weight: bold;">${form.takenByMobile}</span></div>
                    </div>

                    <!-- Valuation section -->
                    <table style="width: 100%; border-collapse: collapse; margin-top: 5px; margin-bottom: 10px; border: none;">
                        <thead>
                            <tr style="border: none;">
                                <th style="text-align: left; width: 30%; border: none; font-weight: bold; padding-bottom: 4px;">Products Value (Tk)</th>
                                <th style="text-align: left; width: 25%; border: none; font-weight: bold; padding-bottom: 4px;">Full Name of MPO</th>
                                <th style="text-align: left; width: 25%; border: none; font-weight: bold; padding-bottom: 4px;">Full Name of ASM</th>
                                <th style="text-align: left; width: 20%; border: none; font-weight: bold; padding-bottom: 4px;">Action Taken</th>
                            </tr>
                        </thead>
                        <tbody style="border: none;">
                            $valueTableRows
                            <tr style="font-weight: bold; font-size: 11px; border: none;">
                                <td style="padding-top: 8px; border: none;">
                                    Total Tk.: <span class="dotted-line" style="width: 65%; border-bottom: 2px solid black; font-weight: bold;">$grandTotalStr</span>
                                </td>
                                <td style="border: none;"></td>
                                <td style="border: none;"></td>
                                <td style="border: none;"></td>
                            </tr>
                        </tbody>
                    </table>

                    <div style="margin-bottom: 10px; font-size: 9.5px;">
                        To<br>Sales Center In-charge.<br>
                        Please arrange to collect the expired products from the chemist as per the list mentioned in this prescribed form and ensure replacement by any product of equivalent value as per business group.
                    </div>
                </div>

                <div>
                    <!-- Footer block -->
                    <div style="display: flex; justify-content: flex-end; margin-bottom: 12px; margin-right: 15px;">
                        <div style="text-align: center; border-top: 1.3px solid black; padding-top: 4px; width: 220px; font-size: 9.5px;">
                            Sr. Sales Team Member of<br>the concerned territory
                        </div>
                    </div>

                    <div style="display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 8px;">
                        <div style="flex-grow: 1;">
                            Received expired products have been replaced vide invoice No. <span class="dotted-line" style="width: 30%; font-weight: bold;">${form.invoiceNo}</span> Dated <span class="dotted-line" style="width: 20%; font-weight: bold;">$invoiceDateFormatted</span>
                        </div>
                        <div style="text-align: center; border-top: 1.3px solid black; padding-top: 4px; width: 180px; font-size: 9.5px; margin-right: 15px;">
                            Signature of SCM with date
                        </div>
                    </div>

                    <div style="display: grid; grid-template-columns: 20% 60% 20%; border: 1.3px solid black; margin-top: 10px; font-size: 8.5px; box-sizing: border-box;">
                        <div style="border-right: 1.3px solid black; padding: 4px;">
                            <div>Reviewed by:</div><br><br><div>SIT QSS</div>
                        </div>
                        <div style="border-right: 1.3px solid black; padding: 3px 8px; display: flex; align-items: center; text-align: center; color: #333;">
                            If QISS cover sheet is not attached, then printed copies of this document are uncontrolled. Controlled copies must include the QISS cover sheet for revision information and stamped with "CONTROLLED COPY" in red with signature of the authorized person.
                        </div>
                        <div style="padding: 4px;">
                            <div>Approved By:</div><br><br><div>SEE QSS</div>
                        </div>
                    </div>
                </div>
            </div>
            """.trimIndent()
        }

        fullHtml += """
            </body>
            </html>
        """.trimIndent()

        return fullHtml
    }

    fun printForm(context: Context, form: WithdrawalForm) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "Expiry-Withdrawal-Form-${form.id.take(6)}"

        // We run a hidden webview to handle printing the dynamic HTML gracefully
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // Return printed output via dynamic PDF conversion document adapter
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager.print(
                    jobName,
                    printAdapter,
                    PrintAttributes.Builder().build()
                )
            }
        }

        val htmlContent = generateHtmlForForm(form)
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
}
