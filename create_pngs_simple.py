#!/usr/bin/env python
import struct
import zlib
import os

def create_png(filename, width, height, r, g, b):
    """Create a simple PNG file with a solid color"""
    # PNG signature
    png_signature = b'\x89PNG\r\n\x1a\n'
    
    # IHDR chunk (image header)
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 2, 0, 0, 0)
    ihdr_crc = zlib.crc32(b'IHDR' + ihdr_data) & 0xffffffff
    ihdr_chunk = struct.pack('>I', 13) + b'IHDR' + ihdr_data + struct.pack('>I', ihdr_crc)
    
    # IDAT chunk (image data)
    # Create uncompressed image data: each row is a filter byte (0) + RGB pixels
    raw_data = b''
    for y in range(height):
        raw_data += b'\x00'  # Filter type: None
        for x in range(width):
            raw_data += struct.pack('BBB', r, g, b)
    
    compressed_data = zlib.compress(raw_data)
    idat_crc = zlib.crc32(b'IDAT' + compressed_data) & 0xffffffff
    idat_chunk = struct.pack('>I', len(compressed_data)) + b'IDAT' + compressed_data + struct.pack('>I', idat_crc)
    
    # IEND chunk (image end)
    iend_crc = zlib.crc32(b'IEND') & 0xffffffff
    iend_chunk = struct.pack('>I', 0) + b'IEND' + struct.pack('>I', iend_crc)
    
    # Write PNG file
    os.makedirs(os.path.dirname(filename), exist_ok=True)
    with open(filename, 'wb') as f:
        f.write(png_signature + ihdr_chunk + idat_chunk + iend_chunk)
    
    return filename

# Create the three PNG files
files_created = []

# Red (255, 0, 0)
f1 = create_png(r'd:\TinyEnglishApp\app\src\main\res\mipmap-hdpi\color_q7.png', 192, 192, 255, 0, 0)
files_created.append(f1)
print(f'Created: {f1}')

# Teal (0, 128, 128)
f2 = create_png(r'd:\TinyEnglishApp\app\src\main\res\mipmap-hdpi\color_q12.png', 192, 192, 0, 128, 128)
files_created.append(f2)
print(f'Created: {f2}')

# Yellow (255, 255, 0)
f3 = create_png(r'd:\TinyEnglishApp\app\src\main\res\mipmap-hdpi\color_q13.png', 192, 192, 255, 255, 0)
files_created.append(f3)
print(f'Created: {f3}')

print('\nAll PNG files created successfully!')
