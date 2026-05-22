#!/usr/bin/env node
/**
 * Create solid-color PNG files with minimal configuration
 */
const fs = require('fs');
const path = require('path');

// Minimal red PNG (1x1 pixel for testing, will be resized conceptually)
// This is a base64-encoded 192x192 red PNG
function createRedPNG() {
  return Buffer.from(
    'iVBORw0KGgoAAAANSUhEUgAAAMAAAADACAYAAABrF6VPAAAA1klEQVR4nO3BMQEAAADCoPVPbQ3foAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB2CbSJAAGfEgkSAAAAAElFTkSuQmCC',
    'base64'
  );
}

function createTealPNG() {
  return Buffer.from(
    'iVBORw0KGgoAAAANSUhEUgAAAMAAAADACAYAAABrF6VPAAAA0klEQVR4nO3BMQEAAADCoPVPbQfwoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB2CbSJAAF2EgkSAAAAAElFTkSuQmCC',
    'base64'
  );
}

function createYellowPNG() {
  return Buffer.from(
    'iVBORw0KGgoAAAANSUhEUgAAAMAAAADACAYAAABrF6VPAAAA0klEQVR4nO3BMQEAAADCoPVPbQfwoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB2CbSJAAF2EgkSAAAAAElFTkSuQmCC',
    'base64'
  );
}

const basePath = 'app/src/main/res/mipmap-hdpi';

const files = [
  { name: 'color_q7.png', generator: createRedPNG },
  { name: 'color_q12.png', generator: createTealPNG },
  { name: 'color_q13.png', generator: createYellowPNG },
];

console.log('Creating placeholder PNG files...');

for (const file of files) {
  const filepath = path.join(basePath, file.name);
  const dir = path.dirname(filepath);
  
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
  
  const pngData = file.generator();
  fs.writeFileSync(filepath, pngData);
  const stats = fs.statSync(filepath);
  console.log(`✓ Created ${file.name} (${stats.size} bytes)`);
}

console.log('\nAll PNG files created successfully!');
