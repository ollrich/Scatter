"""Erzeugt aus design/icon-source.svg die Adaptive-Icon-Ebenen.

Erwartet ein randloses Icon-SVG mit einer Hintergrundebene <rect id="background" fill="#RRGGBB">
und einem beliebig farbigen Motiv darueber. Android legt seine eigene Maske an, daher:
  - Hintergrundebene = die fill-Farbe von id="background" (schreibt values/ic_launcher_background.xml)
  - Vordergrundebene = das Motiv in Originalfarben, zentriert, transparent freigestellt
  - Monochrom-Ebene  = dasselbe Motiv als Silhouette (fuer Themed Icons)
  - ic_logo.png      = Voll-Logo (Hintergrund + Motiv) fuer App-Leiste, Drawer und About

Freistellung: Das SVG wird einmal ohne Hintergrundebene gerendert; der Alphakanal ist dann
direkt die Maske. So funktionieren auch farbige Motive (frueher: Rotkanal, nur fuer Weiss).

Voraussetzungen: rsvg-convert (brew install librsvg), Pillow.
Aufruf: python make_icon.py
"""
import re
import subprocess
from io import BytesIO
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
SVG = ROOT / "design" / "icon-source.svg"
RES = ROOT / "app" / "src" / "main" / "res"
DRAWABLE = RES / "drawable-nodpi"
BG_XML = RES / "values" / "ic_launcher_background.xml"

RENDER_PX = 1024       # Zwischenrendern des SVG
CANVAS = 432           # 108dp @ xxxhdpi
GLYPH_CUTOFF = 128     # ab hier zaehlt ein Pixel als Motiv (fuer die Bbox)
MAX_GLYPH_RATIO = 0.46 # Motivgroesse relativ zur Leinwand; haelt den weissen Punkt (Ecke) in der 66dp-Safe-Zone

BG_RECT = re.compile(r'<rect\b[^>]*\bid="background"[^>]*/>')


def render(svg_text: str, px: int = RENDER_PX) -> Image.Image:
    out = subprocess.run(
        ["rsvg-convert", "-w", str(px), "-h", str(px)],
        input=svg_text.encode("utf-8"), capture_output=True, check=True,
    ).stdout
    return Image.open(BytesIO(out)).convert("RGBA")


def background_color(svg_text: str) -> str:
    rect = BG_RECT.search(svg_text)
    if not rect:
        raise SystemExit('icon-source.svg braucht <rect id="background" fill="#RRGGBB" .../>')
    fill = re.search(r'fill="(#[0-9A-Fa-f]{6})"', rect.group(0))
    if not fill:
        raise SystemExit('id="background" braucht eine fill-Farbe als #RRGGBB')
    return fill.group(1).upper()


def place(glyph: Image.Image, target: int) -> Image.Image:
    """Motiv auf [target] skalieren und auf CANVAS zentrieren."""
    w, h = glyph.size
    scale = target / max(w, h)
    scaled = glyph.resize((max(1, round(w * scale)), max(1, round(h * scale))), Image.LANCZOS)
    out = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    out.alpha_composite(scaled, ((CANVAS - scaled.width) // 2, (CANVAS - scaled.height) // 2))
    return out


def silhouette(img: Image.Image) -> Image.Image:
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    out.putalpha(img.getchannel("A"))
    return out


def main() -> None:
    svg_text = SVG.read_text(encoding="utf-8")
    bg = background_color(svg_text)

    full = render(svg_text)
    motif = render(BG_RECT.sub("", svg_text, count=1))

    alpha = motif.getchannel("A")
    glyph_box = alpha.point(lambda a: 255 if a >= GLYPH_CUTOFF else 0).getbbox()
    glyph = motif.crop(glyph_box)

    ratio = max(glyph.size) / RENDER_PX
    target = round(min(ratio, MAX_GLYPH_RATIO) * CANVAS)

    DRAWABLE.mkdir(parents=True, exist_ok=True)
    fg = place(glyph, target)
    fg.save(DRAWABLE / "ic_launcher_foreground.png")
    silhouette(fg).save(DRAWABLE / "ic_launcher_monochrome.png")

    # Kleines Voll-Logo (Hintergrund + Motiv) fuer die App-Leiste; runde Ecken macht die App.
    full.resize((192, 192), Image.LANCZOS).save(DRAWABLE / "ic_logo.png")

    BG_XML.write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n'
        "<resources>\n"
        "    <!-- Generiert von design/make_icon.py aus id=\"background\" in design/icon-source.svg. -->\n"
        f'    <color name="ic_launcher_background">{bg}</color>\n'
        "</resources>\n",
        encoding="utf-8",
    )

    print("Hintergrund =", bg)
    print("Motiv = %.0f%% der Leinwand -> %dpx auf %dpx" % (ratio * 100, target, CANVAS))
    print("Geschrieben:", DRAWABLE, "(foreground, monochrome, ic_logo) +", BG_XML.name)


if __name__ == "__main__":
    main()
