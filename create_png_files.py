#!/usr/bin/env python3
"""
Create valid solid-color PNG files to replace corrupted ones.
This script uses only Python standard library (struct and zlib).
"""
import os
import struct
import zlib


def create_solid_color_png(filepath, rgb_tuple, size=(192, 192)):
    """
    Create a solid-color PNG file using raw PNG format.
    
    Args:
        filepath: Path where to save the PNG
        rgb_tuple: (R, G, B) color tuple (0-255)
        size: (width, height) in pixels
    """
    r, g, b = rgb_tuple
    width, height = size
    
    # PNG signature
    png_sig = b'\x89PNG\r\n\x1a\n'
    
    # IHDR chunk (image header)
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 2, 0, 0, 0)  # 8-bit RGB
    ihdr_crc = zlib.crc32(b'IHDR' + ihdr_data) & 0xffffffff
    ihdr_chunk = struct.pack('>I', 13) + b'IHDR' + ihdr_data + struct.pack('>I', ihdr_crc)
    
    # IDAT chunk (image data)
    raw_data = b''
    for y in range(height):
        raw_data += b'\x00'  # filter type for each scanline
        for x in range(width):
            raw_data += bytes([r, g, b])
    
    compressed_data = zlib.compress(raw_data)
    idat_crc = zlib.crc32(b'IDAT' + compressed_data) & 0xffffffff
    idat_chunk = struct.pack('>I', len(compressed_data)) + b'IDAT' + compressed_data + struct.pack('>I', idat_crc)
    
    # IEND chunk (end)
    iend_crc = zlib.crc32(b'IEND') & 0xffffffff
    iend_chunk = struct.pack('>I', 0) + b'IEND' + struct.pack('>I', iend_crc)
    
    # Write PNG file
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    with open(filepath, 'wb') as f:
        f.write(png_sig + ihdr_chunk + idat_chunk + iend_chunk)
    
    return os.path.getsize(filepath)


def main():
    """Create the three placeholder PNG files."""
    base_path = 'app/src/main/res/mipmap-hdpi'
    
    files_to_create = [
        ('color_q7.png', (255, 107, 107)),    # Red
        ('color_q12.png', (78, 205, 196)),    # Teal
        ('color_q13.png', (255, 230, 109)),   # Yellow
    ]
    
    print("Creating placeholder PNG files...")
    for filename, color_rgb in files_to_create:
        filepath = os.path.join(base_path, filename)
        size_bytes = create_solid_color_png(filepath, color_rgb)
        print(f"✓ Created {filename} ({size_bytes} bytes, RGB{color_rgb})")
    
    print("\nAll PNG files created successfully!")


if __name__ == '__main__':
    main()
