#!/usr/bin/env node
/**
 * Create valid solid-color PNG files to replace corrupted ones.
 * Uses Node.js built-in modules only.
 */
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

function createSolidColorPNG(filepath, rgbTuple, size = [192, 192]) {
  const [r, g, b] = rgbTuple;
  const [width, height] = size;
  
  // PNG signature
  const pngSig = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]);
  
  // IHDR chunk (image header)
  const ihdrData = Buffer.alloc(13);
  ihdrData.writeUInt32BE(width, 0);
  ihdrData.writeUInt32BE(height, 4);
  ihdrData[8] = 8;   // bit depth
  ihdrData[9] = 2;   // color type (RGB)
  ihdrData[10] = 0;  // compression method
  ihdrData[11] = 0;  // filter method
  ihdrData[12] = 0;  // interlace method
  
  const ihdrCrc = crc32(Buffer.concat([Buffer.from('IHDR'), ihdrData]));
  const ihdrChunk = Buffer.concat([
    uint32BE(13),
    Buffer.from('IHDR'),
    ihdrData,
    uint32BE(ihdrCrc)
  ]);
  
  // IDAT chunk (image data)
  let rawData = Buffer.alloc(0);
  for (let y = 0; y < height; y++) {
    const scanline = Buffer.alloc(width * 3 + 1);
    scanline[0] = 0;  // filter type
    for (let x = 0; x < width; x++) {
      scanline[1 + x * 3] = r;
      scanline[1 + x * 3 + 1] = g;
      scanline[1 + x * 3 + 2] = b;
    }
    rawData = Buffer.concat([rawData, scanline]);
  }
  
  const compressedData = zlib.deflateSync(rawData);
  const idatCrc = crc32(Buffer.concat([Buffer.from('IDAT'), compressedData]));
  const idatChunk = Buffer.concat([
    uint32BE(compressedData.length),
    Buffer.from('IDAT'),
    compressedData,
    uint32BE(idatCrc)
  ]);
  
  // IEND chunk (end)
  const iendCrc = crc32(Buffer.from('IEND'));
  const iendChunk = Buffer.concat([
    uint32BE(0),
    Buffer.from('IEND'),
    uint32BE(iendCrc)
  ]);
  
  // Write PNG file
  const dir = path.dirname(filepath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
  
  fs.writeFileSync(filepath, Buffer.concat([pngSig, ihdrChunk, idatChunk, iendChunk]));
  return fs.statSync(filepath).size;
}

function uint32BE(value) {
  const buf = Buffer.alloc(4);
  buf.writeUInt32BE(value, 0);
  return buf;
}

function crc32(buf) {
  let crc = 0xffffffff;
  for (let i = 0; i < buf.length; i++) {
    crc = crc ^ buf[i];
    for (let j = 0; j < 8; j++) {
      crc = (crc >>> 1) ^ ((crc & 1) ? 0xedb88320 : 0);
    }
  }
  return (crc ^ 0xffffffff) >>> 0;
}

function main() {
  const basePath = 'app/src/main/res/mipmap-hdpi';
  
  const filesToCreate = [
    ['color_q7.png', [255, 107, 107]],    // Red
    ['color_q12.png', [78, 205, 196]],    // Teal
    ['color_q13.png', [255, 230, 109]],   // Yellow
  ];
  
  console.log('Creating placeholder PNG files...');
  for (const [filename, colorRgb] of filesToCreate) {
    const filepath = path.join(basePath, filename);
    const sizeBytes = createSolidColorPNG(filepath, colorRgb);
    console.log(`✓ Created ${filename} (${sizeBytes} bytes, RGB${colorRgb})`);
  }
  
  console.log('\nAll PNG files created successfully!');
}

main();
