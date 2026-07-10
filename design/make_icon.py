"""Erzeugt aus design/icon-source.svg die Adaptive-Icon-Ebenen.

Erwartet ein randloses Icon-SVG: flaechige Hintergrundfarbe (das Blau) + weisses Motiv.
Android legt seine eigene Maske an, daher wird nur das Motiv uebernommen:
  - Hintergrundebene = die Hintergrundfarbe (als Farb-Ressource, siehe Ausgabe "BLAU")
  - Vordergrundebene = das weisse Motiv, zentriert, transparent freigestellt
  - Monochrom-Ebene  = dasselbe Motiv als Silhouette (fuer Themed Icons)

Freistellung: Der Hintergrund ist einfarbig, das Motiv weiss. Der Rot-Kanal trennt beides
sauber (Hintergrund-R niedrig, Weiss-R = 255) -> daraus die Alpha-Maske.

Voraussetzungen: rsvg-convert (brew install librsvg), Pillow.
Aufruf: python make_icon.py
"""
import subprocess
import tempfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
SVG = ROOT / "design" / "icon-source.svg"
RES = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"

RENDER_PX = 1024       # Zwischenrendern des SVG
CANVAS = 432           # 108dp @ xxxhdpi
GLYPH_CUTOFF = 128     # ab hier zaehlt ein Pixel als Motiv (fuer die Bbox)
MAX_GLYPH_RATIO = 0.52 # Motivgroesse relativ zur Leinwand; klein genug fuer die Kreis-Maske


def render_svg() -> Image.Image:
    with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as tmp:
        subprocess.run(
            ["rsvg-convert", "-w", str(RENDER_PX), "-h", str(RENDER_PX), str(SVG), "-o", tmp.name],
            check=True,
        )
        return Image.open(tmp.name).convert("RGB")


def whiteness(img: Image.Image, bg) -> Image.Image:
    """Alpha-Maske: 255 = weisses Motiv, 0 = Hintergrund. Rot-Kanal trennt am besten."""
    span = max(1, 255 - bg[0])
    return img.getchannel("R").point(
        lambda r: max(0, min(255, round((r - bg[0]) * 255 / span)))
    )


def layer(alpha: Image.Image, target: int, color) -> Image.Image:
    """Motiv in [color] einfaerben, auf [target] skalieren und auf CANVAS zentrieren."""
    w, h = alpha.size
    scale = target / max(w, h)
    scaled = alpha.resize((max(1, round(w * scale)), max(1, round(h * scale))), Image.LANCZOS)

    out = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    tinted = Image.new("RGBA", scaled.size, tuple(color) + (0,))
    tinted.putalpha(scaled)
    out.alpha_composite(tinted, ((CANVAS - scaled.width) // 2, (CANVAS - scaled.height) // 2))
    return out


def main() -> None:
    img = render_svg()
    bg = img.getpixel((2, 2))  # randlos -> Ecke ist die Hintergrundfarbe

    alpha = whiteness(img, bg)
    glyph_box = alpha.point(lambda a: 255 if a >= GLYPH_CUTOFF else 0).getbbox()
    glyph = alpha.crop(glyph_box)

    ratio = max(glyph.size) / RENDER_PX
    target = round(min(ratio, MAX_GLYPH_RATIO) * CANVAS)

    RES.mkdir(parents=True, exist_ok=True)
    layer(glyph, target, (255, 255, 255)).save(RES / "ic_launcher_foreground.png")
    layer(glyph, target, (0, 0, 0)).save(RES / "ic_launcher_monochrome.png")

    # Kleines Voll-Logo (Hintergrund + Motiv) für die App-Leiste; runde Ecken macht die App.
    img.resize((192, 192), Image.LANCZOS).save(RES / "ic_logo.png")

    print("BLAU  = #%02X%02X%02X" % bg)
    print("Motiv = %.0f%% der Leinwand -> %dpx auf %dpx" % (ratio * 100, target, CANVAS))
    print("Geschrieben:", RES / "ic_launcher_foreground.png", "+ ic_launcher_monochrome.png")


if __name__ == "__main__":
    main()
