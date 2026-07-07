"""Bereitet icon-source.png als zentrierten Adaptive-Icon-Vordergrund auf.
Crop auf Inhalt (nicht-weiss), zentriert und skaliert auf eine transparente 432er-Leinwand.
Der weisse Rest bleibt (Hintergrundebene ist ebenfalls weiss) -> nahtlos."""
from PIL import Image, ImageChops

SRC = "/Users/olli/Drive/github/Share2MastoSky/design/icon-source.png"
OUT = "/Users/olli/Drive/github/Share2MastoSky/app/src/main/res/drawable-nodpi/ic_launcher_foreground.png"

CANVAS = 432          # 108dp @ xxxhdpi
TARGET_MAX = 340      # Inhalt fuellt ~79 % -> Flieger klar sichtbar, Rand-Trim nur auf reinem Kreis

src = Image.open(SRC).convert("RGB")
bg = Image.new("RGB", src.size, (255, 255, 255))
# Schwellwert: nahezu-weisse Pixel (Artefakte) ignorieren, damit nur der echte Inhalt zaehlt.
diff = ImageChops.difference(src, bg).convert("L")
mask = diff.point(lambda p: 255 if p > 24 else 0)
bbox = mask.getbbox()
print("Inhalts-Bounding-Box:", bbox)

content = src.crop(bbox)
w, h = content.size
scale = TARGET_MAX / max(w, h)
nw, nh = round(w * scale), round(h * scale)
content = content.resize((nw, nh), Image.LANCZOS)

fg = Image.new("RGBA", (CANVAS, CANVAS), (255, 255, 255, 0))
fg.paste(content, ((CANVAS - nw) // 2, (CANVAS - nh) // 2))
fg.save(OUT)
print("Geschrieben:", OUT, fg.size)
