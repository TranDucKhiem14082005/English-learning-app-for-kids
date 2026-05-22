const fs = require('fs');
const path = require('path');

// Create a solid-color PNG programmatically using pure JS
// Reference: https://en.wikipedia.org/wiki/Portable_Network_Graphics

function createSolidPNG(color_r, color_g, color_b, width = 192, height = 192) {
  const buf = [];
  
  // PNG signature
  buf.push(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
  
  // Helper function to add CRC-32
  const crc32 = (buf) => {
    let crc = 0 ^ (-1);
    for (let i = 0; i < buf.length; i++) {
      crc = (crc >>> 8) ^ ((crc ^ buf[i]) & 0xFF);
      for (let j = 0; j < 8; j++) {
        crc = (crc >>> 1) ^ ((crc & 1) ? 0xEDB88320 : 0);
      }
    }
    return (crc ^ (-1)) >>> 0;
  };
  
  // Helper to write 32-bit big-endian
  const write32BE = (v) => [
    (v >>> 24) & 0xFF,
    (v >>> 16) & 0xFF,
    (v >>> 8) & 0xFF,
    v & 0xFF
  ];
  
  // IHDR chunk
  const ihdr_type = [0x49, 0x48, 0x44, 0x52]; // "IHDR"
  const ihdr_data = [
    ...write32BE(width),
    ...write32BE(height),
    0x08, // bit depth
    0x02, // color type RGB
    0x00, // compression
    0x00, // filter
    0x00  // interlace
  ];
  const ihdr_crc_buf = [...ihdr_type, ...ihdr_data];
  const ihdr_crc = crc32(ihdr_crc_buf);
  buf.push(...write32BE(ihdr_data.length), ...ihdr_type, ...ihdr_data, ...write32BE(ihdr_crc));
  
  // IDAT chunk - compressed image data
  const zlib = require('zlib');
  let raw_data = [];
  
  // Build raw scanline data
  for (let y = 0; y < height; y++) {
    raw_data.push(0); // filter type for scanline
    for (let x = 0; x < width; x++) {
      raw_data.push(color_r, color_g, color_b);
    }
  }
  
  const compressed = zlib.deflateSync(Buffer.from(raw_data));
  const idat_type = [0x49, 0x44, 0x41, 0x54]; // "IDAT"
  const idat_crc_buf = [...idat_type, ...compressed];
  const idat_crc = crc32(idat_crc_buf);
  buf.push(...write32BE(compressed.length), ...idat_type, ...Array.from(compressed), ...write32BE(idat_crc));
  
  // IEND chunk
  const iend_type = [0x49, 0x45, 0x4E, 0x44]; // "IEND"
  const iend_crc = crc32(iend_type);
  buf.push(...write32BE(0), ...iend_type, ...write32BE(iend_crc));
  
  return Buffer.from(buf);
}

function main() {
  const basePath = 'app/src/main/res/mipmap-hdpi';
  
  const colors = [
    { name: 'color_q7.png', r: 255, g: 107, b: 107 },    // Red
    { name: 'color_q12.png', r: 78, g: 205, b: 196 },    // Teal
    { name: 'color_q13.png', r: 255, g: 230, b: 109 },   // Yellow
  ];
  
  console.log('Creating placeholder PNG files...\n');
  
  for (const color of colors) {
    const filepath = path.join(basePath, color.name);
    const dir = path.dirname(filepath);
    
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
    }
    
    const pngBuffer = createSolidPNG(color.r, color.g, color.b);
    fs.writeFileSync(filepath, pngBuffer);
    
    const stats = fs.statSync(filepath);
    console.log(`✓ Created ${color.name}`);
    console.log(`  Path: ${filepath}`);
    console.log(`  Size: ${stats.size} bytes`);
    console.log(`  Color: RGB(${color.r}, ${color.g}, ${color.b})\n`);
  }
  
  console.log('All PNG files created successfully!');
}

if (require.main === module) {
  main();
}

module.exports = { createSolidPNG };
