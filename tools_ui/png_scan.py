import struct, zlib, sys
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

def read_png(path):
    data = open(path, 'rb').read()
    assert data[:8] == b'\x89PNG\r\n\x1a\n', 'not a png'
    pos = 8
    width = height = None
    idat = b''
    while pos < len(data):
        length = struct.unpack('>I', data[pos:pos+4])[0]
        ctype = data[pos+4:pos+8]
        chunk = data[pos+8:pos+8+length]
        if ctype == b'IHDR':
            width, height, bitdepth, colortype = struct.unpack('>IIBB', chunk[:10])
            assert bitdepth == 8 and colortype in (2, 6), f'unsupported png: depth={bitdepth} color={colortype}'
        elif ctype == b'IDAT':
            idat += chunk
        pos += 8 + length + 4
    raw = zlib.decompress(idat)
    bpp = 4 if colortype == 6 else 3
    stride = width * bpp
    # unfilter
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

path = sys.argv[1] if len(sys.argv) > 1 else r'C:/Users/USER-PC/AppData/Local/Temp/shot.png'
w, h, bpp, px = read_png(path)

# Sample x=540 column; report runs of identical color (rounded to reduce noise)
prev = None
start = 0
for y in range(h):
    i = (y * w + 540) * bpp
    c = (px[i] // 8 * 8, px[i+1] // 8 * 8, px[i+2] // 8 * 8)
    if c != prev:
        if prev is not None and y - start > 12:
            print(f'rows {start}-{y-1} ({y-start}px): color={prev}')
        prev = c
        start = y
print(f'rows {start}-{h-1}: color={prev}')
