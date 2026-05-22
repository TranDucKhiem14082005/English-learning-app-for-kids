from PIL import Image
import os

# Define the PNG files to create
png_files = {
    r'd:\TinyEnglishApp\app\src\main\res\mipmap-hdpi\color_q7.png': (255, 0, 0),      # Red
    r'd:\TinyEnglishApp\app\src\main\res\mipmap-hdpi\color_q12.png': (0, 128, 128),   # Teal
    r'd:\TinyEnglishApp\app\src\main\res\mipmap-hdpi\color_q13.png': (255, 255, 0),   # Yellow
}

# Create 192x192 PNG files with solid colors
for filepath, color in png_files.items():
    # Ensure directory exists
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    img = Image.new('RGB', (192, 192), color)
    img.save(filepath)
    print(f'Created: {filepath}')

print('All PNG files created successfully!')
