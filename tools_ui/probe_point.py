import re, sys
sys.stdout.reconfigure(encoding='utf-8', errors='replace')
path = r'C:/Users/USER-PC/AppData/Local/Temp/ui.xml'
x, y = int(sys.argv[1]), int(sys.argv[2])
xml = open(path, encoding='utf-8').read()

# Build a stack-based parse of nodes with bounds
tokens = re.finditer(r'<(/?)node\b([^>]*?)(/?)>', xml)
stack = []
hits = []
for tok in tokens:
    closing, attrs, selfclose = tok.group(1), tok.group(2), tok.group(3)
    if closing:
        if stack: stack.pop()
        continue
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', attrs)
    if b:
        x1, y1, x2, y2 = map(int, b.groups())
        inside = x1 <= x < x2 and y1 <= y < y2
    else:
        inside = False
    info = {
        'depth': len(stack),
        'inside': inside,
        'class': (re.search(r'class="([^"]*)"', attrs) or [None, '?'])[1],
        'text': (re.search(r'text="([^"]*)"', attrs) or [None, ''])[1],
        'clickable': 'clickable="true"' in attrs,
        'enabled': 'enabled="false"' not in attrs,
    }
    if inside:
        hits.append(info)
    if not selfclose:
        stack.append(info)

print(f'Nodes containing point ({x},{y}):')
for h in hits:
    print(f"  d{h['depth']} {h['class']} text={h['text']!r} clickable={h['clickable']} enabled={h['enabled']}")
