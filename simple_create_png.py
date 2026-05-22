#!/usr/bin/env python3
"""
Simple PNG file creator - creates solid-color PNG placeholder files
This version uses minimal dependencies (only standard library)
"""

import struct
import zlib
import os
from pathlib import Path


def write_png(filepath, color_rgb, size=(192, 192)):
    """
    Write a solid-color PNG file.
    
    Args:
        filepath: Output file path
        color_rgb: Tuple of (R, G, B) values (0-255)
        size: Tuple of (width, height) in pixels
    """
    r, g, b = color_rgb
    width, height = size
    
    # Create PNG chunks
    chunks = []
    
    # PNG signature
    sig = b'\x89PNG\r\n\x1a\n'
    
    # IHDR chunk (image header)
    ihdr_data = struct.pack(
        '>IIBBBBB',
        width,           # width
        height,          # height
        8,               # bit depth (8 bits per channel)
        2,               # color type (2 = RGB)
        0,               # compression method
        0,               # filter method
        0                # interlace method
    )
    chunks.append(make_chunk(b'IHDR', ihdr_data))
    
    # IDAT chunk (image data)
    raw_data = b''
    for y in range(height):
        raw_data += b'\x00'  # filter type (None)
        for x in range(width):
            raw_data += bytes([r, g, b])
    
    compressed = zlib.compress(raw_data, 9)
    chunks.append(make_chunk(b'IDAT', compressed))
    
    # IEND chunk (end marker)
    chunks.append(make_chunk(b'IEND', b''))
    
    # Write file
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    with open(filepath, 'wb') as f:
        f.write(sig)
        for chunk in chunks:
            f.write(chunk)


def make_chunk(chunk_type, data):
    """Create a PNG chunk with CRC."""
    length = struct.pack('>I', len(data))
    crc_data = chunk_type + data
    crc = struct.pack('>I', zlib.crc32(crc_data) & 0xffffffff)
    return length + crc_data + crc


def main():
    """Create the placeholder PNG files."""
    base = Path('app/src/main/res/mipmap-hdpi')
    
    files_to_create = [
        ('color_q7.png', (255, 107, 107)),     # Red
        ('color_q12.png', (78, 205, 196)),     # Teal  
        ('color_q13.png', (255, 230, 109)),    # Yellow
    ]
    
    print("Creating placeholder PNG files...")
    print()
    
    for filename, color in files_to_create:
        filepath = base / filename
        write_png(str(filepath), color)
        size = filepath.stat().st_size
        print(f"✓ {filename}")
        print(f"  Path: {filepath}")
        print(f"  Size: {size} bytes")
        print(f"  Color: RGB{color}")
        print()
    
    print("All PNG files created successfully!")


if __name__ == '__main__':
    main()
