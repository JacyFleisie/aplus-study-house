import re, sys
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

path = r'C:/Users/USER-PC/AppData/Local/Temp/ui.xml'
xml = open(path, encoding='utf-8').read()

mode = sys.argv[1] if len(sys.argv) > 1 else 'text'

if mode == 'text':
    for m in re.finditer(r'text="([^"]*)"[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml):
        t = m.group(1).strip()
        if t:
            x1, y1, x2, y2 = map(int, m.group(2, 3, 4, 5))
            print(f'{t!r}: center=({(x1+x2)//2},{(y1+y2)//2})')
elif mode == 'inputs':
    for m in re.finditer(r'<node[^>]*class="android.widget.EditText"[^>]*>', xml):
        n = m.group(0)
        t = re.search(r'text="([^"]*)"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        x1, y1, x2, y2 = map(int, b.groups())
        print('EDIT:', repr(t.group(1)) if t else '', f'center=({(x1+x2)//2},{(y1+y2)//2})')
    for m in re.finditer(r'<node[^>]*checkable="true"[^>]*>', xml):
        n = m.group(0)
        c = re.search(r'checked="([^"]*)"', n)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', n)
        x1, y1, x2, y2 = map(int, b.groups())
        print('CHECKBOX checked=', c.group(1) if c else '', f'center=({(x1+x2)//2},{(y1+y2)//2})')
