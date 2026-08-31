with open('app/src/main/java/com/example/blankapp/screens/admin/AdminFinancePaymentScreen.kt', 'r') as f:
    content = f.read()

# Find the second Record Cash Dialog section
first = content.find('// Record Cash Dialog')
second = content.find('// Record Cash Dialog', first + 1)

if second != -1:
    # Find the end of the second section
    # Look for the next @Composable or the end of the file
    end = content.find('@Composable', second)
    if end == -1:
        end = len(content)
    
    # Remove the second section
    content = content[:second] + content[end:]
    with open('app/src/main/java/com/example/blankapp/screens/admin/AdminFinancePaymentScreen.kt', 'w') as f:
        f.write(content)
    print("Done! Removed duplicate Record Cash Dialog")
else:
    print("Could not find duplicate")
