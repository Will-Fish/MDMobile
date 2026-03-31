#!/usr/bin/env python3
"""
Generate placeholder launcher icons for MDMobile app.
Creates PNG files in various mipmap directories.
"""

from PIL import Image, ImageDraw, ImageFont
import os

# Configuration
APP_NAME = "MDMobile"
BG_COLOR = (98, 0, 238)  # purple_500 #6200EE
FG_COLOR = (255, 255, 255)  # white

# Sizes for different densities (in pixels)
SIZES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

# Paths
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
RES_DIR = os.path.join(BASE_DIR, "app", "src", "main", "res")

def generate_icon(size, density_name):
    """Generate a single icon with given size."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Draw background (rounded rectangle with 20% corner radius)
    corner_radius = size // 5
    draw.rounded_rectangle([(0, 0), (size-1, size-1)],
                          radius=corner_radius,
                          fill=BG_COLOR)

    # Try to load a font, fallback to default
    try:
        # Try to use a system font
        font_path = "arial.ttf"
        font = ImageFont.truetype(font_path, size=size//2)
    except:
        # Fallback to default font
        font = ImageFont.load_default()

    # Draw "MD" text
    text = "MD"
    # Calculate text position (centered)
    # For default font, we need to adjust position
    if font == ImageFont.load_default():
        # Default font is small, use larger text
        text_bbox = draw.textbbox((0, 0), text, font=font)
        text_width = text_bbox[2] - text_bbox[0]
        text_height = text_bbox[3] - text_bbox[1]
        text_x = (size - text_width) // 2
        text_y = (size - text_height) // 2
    else:
        # Use textsize for newer PIL
        try:
            text_width, text_height = draw.textsize(text, font=font)
        except:
            text_bbox = draw.textbbox((0, 0), text, font=font)
            text_width = text_bbox[2] - text_bbox[0]
            text_height = text_bbox[3] - text_bbox[1]
        text_x = (size - text_width) // 2
        text_y = (size - text_height) // 2

    draw.text((text_x, text_y), text, fill=FG_COLOR, font=font)

    return img

def main():
    print(f"Generating icons for {APP_NAME}")
    print(f"Resource directory: {RES_DIR}")

    for density, size in SIZES.items():
        dir_path = os.path.join(RES_DIR, f"mipmap-{density}")
        os.makedirs(dir_path, exist_ok=True)

        # Generate icon
        img = generate_icon(size, density)

        # Save as ic_launcher.png
        output_path = os.path.join(dir_path, "ic_launcher.png")
        img.save(output_path, "PNG")
        print(f"  Generated {output_path} ({size}x{size})")

        # Also create ic_launcher_round.png (same image for now)
        round_output_path = os.path.join(dir_path, "ic_launcher_round.png")
        img.save(round_output_path, "PNG")
        print(f"  Generated {round_output_path} ({size}x{size})")

    print("Icon generation complete!")

if __name__ == "__main__":
    main()