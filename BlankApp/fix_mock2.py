with open('app/src/main/java/com/example/blankapp/screens/admin/AdminFinancePaymentScreen.kt', 'r') as f:
    content = f.read()

# Find the mock section
start = content.find('// Mock recent cash payments')
# Find the end of the block - the line before @Composable PaymentCard
end = content.find('@Composable\nfun PaymentCard(')

if start != -1 and end != -1:
    # Remove the mock section
    content = content[:start] + content[end:]
    with open('app/src/main/java/com/example/blankapp/screens/admin/AdminFinancePaymentScreen.kt', 'w') as f:
        f.write(content)
    print("Done! Removed mock recent cash payments")
else:
    print(f"Could not find markers: start={start}, end={end}")
