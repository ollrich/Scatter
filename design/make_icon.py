"""Erzeugt aus design/icon-source.svg die Adaptive-Icon-Ebenen.

Das Quell-SVG ist ein fertiges quadratisches Icon (blaues Squircle + weisses Motiv).
Android legt seine eigene Maske an, daher wird der Rahmen NICHT uebernommen:
  - Hintergrundebene = das Blau (als Farb-Ressource, siehe Ausgabe "BLAU")
  - Vordergrundebene = nur das weisse Motiv, zentriert, transparent freigestellt
  - Monochrom-Ebene  = dasselbe Motiv als Silhouette (fuer Themed Icons)

Trick zur Freistellung: Der weisse Seitenhintergrund und das weisse Motiv sind farblich
identisch. Also wird die Aussenflaeche von (0,0) aus mit Blau geflutet - das Motiv bleibt
verschont, weil es rundum vom blauen Squircle eingeschlossen ist.

Voraussetzungen: rsvg-convert (brew install librsvg), Pillow.
Aufruf: python make_icon.py
"""
import subprocess
import tempfile
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
SVG = ROOT / "design" / "icon-source.svg"
RES = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"

RENDER_PX = 1024        # Zwischenrendern des SVG
CANVAS = 432            # 108dp @ xxxhdpi
WHITE_THRESHOLD = 24    # "nahezu weiss" vom Seitenhintergrund trennen
PAGE_FLOOD_THRESH = 300 # Toleranz (Summe ueber RGB) beim Fluten der Aussenflaeche
GLYPH_CUTOFF = 128      # ab hier zaehlt ein Pixel als Motiv (Rest = Antialias-Ring)
MAX_GLYPH_RATIO = 0.52  # Motivgroesse relativ zum Quadrat; klein genug fuer die Kreis-Maske


def render_svg() -> Image.Image:
    with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as tmp:
        subprocess.run(
            ["rsvg-convert", "-w", str(RENDER_PX), "-h", str(RENDER_PX), str(SVG), "-o", tmp.name],
            check=True,
        )
        return Image.open(tmp.name).convert("RGB")


def square_bbox(img: Image.Image):
    """Bbox des farbigen Icon-Quadrats (alles, was vom Seitenhintergrund abweicht)."""
    page = Image.new("RGB", img.size, img.getpixel((2, 2)))
    diff = ImageChops.difference(img, page).convert("L")
    return diff.point(lambda p: 255 if p > WHITE_THRESHOLD else 0).getbbox()


def whiteness(img: Image.Image, blue) -> Image.Image:
    """Alpha-Maske: 255 = weiss (Motiv), 0 = blau. Kanal R trennt am besten."""
    span = 255 - blue[0]
    return img.getchannel("R").point(
        lambda r: max(0, min(255, round((r - blue[0]) * 255 / span)))
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
    box = square_bbox(img)
    side = box[2] - box[0]

    blue = img.getpixel((box[0] + round(side * 0.10), box[1] + round(side * 0.10)))

    # Aussenflaeche auf Blau fluten -> danach ist nur noch das Motiv weiss.
    ImageDraw.floodfill(img, (0, 0), blue, thresh=PAGE_FLOOD_THRESH)

    alpha = whiteness(img, blue)
    glyph_box = alpha.point(lambda a: 255 if a >= GLYPH_CUTOFF else 0).getbbox()
    glyph = alpha.crop(glyph_box)

    ratio = max(glyph.size) / side
    target = round(min(ratio, MAX_GLYPH_RATIO) * CANVAS)

    RES.mkdir(parents=True, exist_ok=True)
    layer(glyph, target, (255, 255, 255)).save(RES / "ic_launcher_foreground.png")
    layer(glyph, target, (0, 0, 0)).save(RES / "ic_launcher_monochrome.png")

    print("BLAU  = #%02X%02X%02X" % blue)
    print("Motiv = %.0f%% des Quadrats -> %dpx auf %dpx Leinwand" % (ratio * 100, target, CANVAS))
    print("Geschrieben:", RES / "ic_launcher_foreground.png", "+ ic_launcher_monochrome.png")


if __name__ == "__main__":
    main()
