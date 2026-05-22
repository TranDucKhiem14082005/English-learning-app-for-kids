#!/usr/bin/env python3
"""
Emergency PNG fixer - paste this directly into Python shell if scripts fail
Run with: python3 -c "..." < paste this code
"""

import struct, zlib, os
from pathlib import Path

def png(fp, rgb, sz=192):
    r,g,b = rgb
    w=h=sz
    # PNG sig
    d = b'\x89PNG\r\n\x1a\n'
    # IHDR
    ihdr = struct.pack('>IIBBBBB', w, h, 8, 2, 0, 0, 0)
    crc = struct.pack('>I', zlib.crc32(b'IHDR' + ihdr) & 0xffffffff)
    d += struct.pack('>I', 13) + b'IHDR' + ihdr + crc
    # IDAT
    raw = b''.join(b'\x00' + bytes([r,g,b])*w for _ in range(h))
    comp = zlib.compress(raw)
    crc = struct.pack('>I', zlib.crc32(b'IDAT' + comp) & 0xffffffff)
    d += struct.pack('>I', len(comp)) + b'IDAT' + comp + crc
    # IEND
    crc = struct.pack('>I', zlib.crc32(b'IEND') & 0xffffffff)
    d += struct.pack('>I', 0) + b'IEND' + crc
    # Write
    os.makedirs(os.path.dirname(fp), exist_ok=True)
    with open(fp, 'wb') as f: f.write(d)
    return os.path.getsize(fp)

# Create the 3 PNGs
b = Path('app/src/main/res/mipmap-hdpi')
for nm, col in [('color_q7.png', (255,107,107)), ('color_q12.png', (78,205,196)), ('color_q13.png', (255,230,109))]:
    sz = png(str(b/nm), col)
    print(f"✓ {nm}: {sz} bytes")

print("\nNow run: gradlew.bat assembleRelease")
