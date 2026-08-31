with open('app/src/main/java/com/example/blankapp/screens/admin/AdminFinancePaymentScreen.kt', 'r') as f:
    content = f.read()

# 1. Add kotlinx.coroutines.launch import
if 'import kotlinx.coroutines.launch' not in content:
    content = content.replace(
        'import com.example.blankapp.ui.theme.*',
        'import com.example.blankapp.ui.theme.*\nimport kotlinx.coroutines.launch'
    )

# 2. Add scope variable
content = content.replace(
    'var rejectionReason by remember { mutableStateOf("") }\n    \n    // Mock cash payment data',
    'var rejectionReason by remember { mutableStateOf("") }\n    val scope = rememberCoroutineScope()\n    \n    // Cash payment data'
)

# 3. Fix Verify button
content = content.replace(
    'showVerifyDialog = false\n                        // TODO: Update payment status to verified',
    'showVerifyDialog = false\n                        scope.launch {\n                            selectedPayment?.let { payment ->\n                                SupabaseRepository.updatePaymentStatus(payment.id, "verified")\n                            }\n                        }'
)

# 4. Fix Reject button
content = content.replace(
    'showRejectDialog = false\n                        // TODO: Update payment status to rejected',
    'showRejectDialog = false\n                        scope.launch {\n                            selectedPayment?.let { payment ->\n                                SupabaseRepository.updatePaymentStatus(payment.id, "rejected")\n                            }\n                        }'
)

# 5. Fix Save cash payment
content = content.replace(
    'showRecordCashDialog = false\n                        cashStudentName = ""\n                        cashAmount = ""\n                        cashDescription = ""\n                        // TODO: Save cash payment',
    'showRecordCashDialog = false\n                        scope.launch {\n                            SupabaseRepository.saveCashPayment(cashStudentName, cashAmount, cashDescription)\n                            cashStudentName = ""\n                            cashAmount = ""\n                            cashDescription = ""\n                        }'
)

# 6. Remove mock recent cash payments
start = content.find('// Mock recent cash payments')
if start != -1:
    end = content.find('        // Verify Dialog', start)
    if end != -1:
        # Find the end of the forEach block
        brace_count = 0
        i = content.find('recentCashPayments.forEach', start)
        if i != -1:
            while i < len(content):
                if content[i] == '{':
                    brace_count += 1
                elif content[i] == '}':
                    brace_count -= 1
                    if brace_count == 0:
                        end_of_block = i + 1
                        break
                i += 1
            content = content[:start] + content[end_of_block:]

# 7. Replace mock proof preview
old_proof = '''// Mock proof preview (a stylized POP image placeholder)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(PrimaryContainer, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Receipt,
                                contentDescription = "POP Preview",
                                tint = Primary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "POP_${selectedPayment!!.reference}.jpg",
                                style = MaterialTheme.typography.bodySmall,
                                color = Primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }'''

new_proof = '''// Show real POP URL
                    if (selectedPayment?.proofUrl != null) {
                        Text("Payment proof:", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = selectedPayment!!.proofUrl!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { /* Open URL in browser */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Open in Browser")
                        }
                    } else {
                        Text("No proof of payment uploaded.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }'''

content = content.replace(old_proof, new_proof)

with open('app/src/main/java/com/example/blankapp/screens/admin/AdminFinancePaymentScreen.kt', 'w') as f:
    f.write(content)

print("Done! Applied all fixes to AdminFinancePaymentScreen")
