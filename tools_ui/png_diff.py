import struct, zlib, sys
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

def read_png(path):
    data = open(path, 'rb').read()
    assert data[:8] == b'\x89PNG\r\n\x1a\n', 'not a png'
    pos = 8
    width = height = None
    colortype = None
    idat = b''
    while pos < len(data):
        length = struct.unpack('>I', data[pos:pos+4])[0]
        ctype = data[pos+4:pos+8]
        chunk = data[pos+8:pos+8+length]
        if ctype == b'IHDR':
            width, height, bitdepth, colortype = struct.unpack('>IIBB', chunk[:10])
        elif ctype == b'IDAT':
            idat += chunk
        pos += 8 + length + 4
    raw = zlib.decompress(idat)
    bpp = 4 if colortype == 6 else 3
    stride = width * bpp
    out = bytearray()
    prev = bytearray(stride)
    p = 0
    for y in range(height):
        f = raw[p]; p += 1
        line = bytearray(raw[p:p+stride]); p += stride
        if f == 1:
            for i in range(bpp, stride):
                line[i] = (line[i] + line[i-bpp]) & 0xFF
        elif f == 2:
            for i in range(stride):
                line[i] = (line[i] + prev[i]) & 0xFF
        elif f == 3:
            for i in range(stride):
                a = line[i-bpp] if i >= bpp else 0
                line[i] = (line[i] + ((a + prev[i]) >> 1)) & 0xFF
        elif f == 4:
            for i in range(stride):
                a = line[i-bpp] if i >= bpp else 0
                b = prev[i]
                c = prev[i-bpp] if i >= bpp else 0
                pa = abs(b - c); pb = abs(a - c); pc = abs(a + b - 2*c)
                pr = a if pa <= pb and pa <= pc else (b if pb <= pc else c)
                line[i] = (line[i] + pr) & 0xFF
        out += line
        prev = line
    return width, height, bpp, out

a = read_png(sys.argv[1])
b = read_png(sys.argv[2])
w, h, bpp, pa = a
_, _, _, pb = b
print(f'{w}x{h}')
runs = []
start = None
for y in range(h):
    ra = pa[y*w*bpp:(y+1)*w*bpp]
    rb = pb[y*w*bpp:(y+1)*w*bpp]
    diff = ra != rb
    if diff and start is None:
        start = y
    elif not diff and start is not None:
        runs.append((start, y-1))
        start = None
if start is not None:
    runs.append((start, h-1))
print('differing row ranges (popup regions):')
for s, e in runs:
    if e - s > 3:
        # find horizontal extent in first differing row
        y = s
        xs = [x for x in range(w) if pa[(y*w+x)*bpp:(y*w+x)*bpp+3] != pb[(y*w+x)*bpp:(y*w+x)*bpp+3]]
        print(f'  rows {s}-{e}  x-extent {min(xs)}-{max(xs)}' if xs else f'  rows {s}-{e}')
