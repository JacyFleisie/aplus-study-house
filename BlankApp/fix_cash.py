with open('app/src/main/java/com/example/blankapp/screens/admin/AdminFinancePaymentScreen.kt', 'r') as f:
    content = f.read()

# Find and remove the mock recent cash payments section
start_marker = '// Mock recent cash payments'
end_marker = '    // Verify Dialog'

start_idx = content.find(start_marker)
end_idx = content.find(end_marker)

if start_idx != -1 and end_idx != -1:
    # Replace with a comment indicating data comes from backend
    new_section = '''    // Recent cash payments are loaded from the backend (Supabase) via getAllPayments()
    // No mock data is used - all payment data comes from the database

'''
    content = content[:start_idx] + new_section + content[end_idx:]
    with open('app/src/main/java/com/example/blankapp/screens/admin/AdminFinancePaymentScreen.kt', 'w') as f:
        f.write(content)
    print("Done! Removed mock recent cash payments")
else:
    print(f"Could not find markers: start={start_idx}, end={end_idx}")
