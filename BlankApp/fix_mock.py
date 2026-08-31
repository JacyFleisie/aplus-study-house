with open('app/src/main/java/com/example/blankapp/screens/admin/AdminFinancePaymentScreen.kt', 'r') as f:
    content = f.read()

# Find the duplicate mock section and remove it
start_marker = '        // Mock recent cash payments\n        val recentCashPayments = listOf('
end_marker = '        // Verify Dialog'

start_idx = content.find(start_marker)
end_idx = content.find(end_marker, start_idx)

if start_idx != -1 and end_idx != -1:
    # Find the end of the recentCashPayments block
    # Look for the closing brace after the forEach
    brace_count = 0
    i = start_idx
    while i < len(content):
        if content[i] == '{':
            brace_count += 1
        elif content[i] == '}':
            brace_count -= 1
            if brace_count == 0:
                # Found the end of the forEach block
                end_of_block = i + 1
                break
        i += 1
    
    # Remove the mock section
    content = content[:start_idx] + content[end_of_block:]
    with open('app/src/main/java/com/example/blankapp/screens/admin/AdminFinancePaymentScreen.kt', 'w') as f:
        f.write(content)
    print("Done! Removed duplicate mock recent cash payments")
else:
    print(f"Could not find markers: start={start_idx}, end={end_idx}")
