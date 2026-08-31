with open('app/src/main/java/com/example/blankapp/screens/admin/AdminFinancePaymentScreen.kt', 'r') as f:
    content = f.read()

# Find the mock proof preview section and replace it
start_marker = '// Proof of Payment Preview Dialog'
end_marker = '    // Record Cash Dialog'

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx != -1 and end_idx != -1:
    new_section = '''    // Proof of Payment Preview Dialog
    if (showProofDialog && selectedPayment != null) {
        AlertDialog(
            onDismissRequest = { showProofDialog = false },
            title = {
                Text("Proof of Payment", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    if (selectedPayment?.proofUrl != null) {
                        Text("Payment proof:", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = selectedPayment!!.proofUrl!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Amount: R${selectedPayment!!.amount.toInt()}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(selectedPayment!!.proofUrl!!))
                                } catch (e: Exception) {
                                    // Handle error
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Open in Browser")
                        }
                    } else {
                        Text("No proof of payment uploaded.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Reference: ${selectedPayment!!.reference}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "Uploaded: ${selectedPayment!!.date}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProofDialog = false
                        showVerifyDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProofDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

'''
    content = content[:start_idx] + new_section + content[end_idx:]
    with open('app/src/main/java/com/example/blankapp/screens/admin/AdminFinancePaymentScreen.kt', 'w') as f:
        f.write(content)
    print("Done! Replaced mock proof preview with real POP viewer")
else:
    print(f"Could not find markers: start={start_idx}, end={end_idx}")
