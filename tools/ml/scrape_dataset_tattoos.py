r"""
Scrapes tattoo art, body ink photos, flash designs, and linework
for all 1,025 Pokemon to improve model accuracy on body art and tattoos.

Output: Pokemon_ref_imgs/varaiants_tattoos/<pokemon_name>/NNNN.jpg (224x224 RGB JPEG q85, letterboxed)
Resumable: finished Pokemon get a `.done` marker and are skipped on re-run.

Usage:
  .venv_scraper\Scripts\python.exe scrape_dataset_tattoos.py            (scrape all 1,025)
  .venv_scraper\Scripts\python.exe scrape_dataset_tattoos.py --limit 3  (smoke test first 3)
  .venv_scraper\Scripts\python.exe scrape_dataset_tattoos.py --start-id 152
  .venv_scraper\Scripts\python.exe scrape_dataset_tattoos.py --pokemon "Gengar,Charizard"
"""
import os, re, json, time, random, shutil, argparse, io
import urllib.request, urllib.parse
from concurrent.futures import ThreadPoolExecutor
from PIL import Image
import imagehash
from ddgs import DDGS

BASE = os.path.dirname(os.path.abspath(__file__))

# Auto-resolve master pokemon_data.json
CANDIDATE_MASTERS = [
    os.path.join(BASE, "..", "PokedexApp", "app", "src", "main", "assets", "pokemon_data.json"),
    os.path.join(BASE, "..", "..", "app", "src", "main", "assets", "pokemon_data.json"),
    r"C:\Users\pagan\OneDrive\Documents\Code\Pokedex\PokedexApp\app\src\main\assets\pokemon_data.json",
]
MASTER = next((p for p in CANDIDATE_MASTERS if os.path.exists(p)), CANDIDATE_MASTERS[0])

# Auto-resolve output directory
REF_IMGS_BASE = os.path.abspath(os.path.join(BASE, "..", "..", "Pokemon_ref_imgs")) if "PokedexApp" in BASE else BASE
DEFAULT_OUT = os.path.join(REF_IMGS_BASE, "varaiants_tattoos")

QUERIES = [
    "tattoo",
    "ink tattoo skin",
    "tattoo design lineart",
    "traditional tattoo flash",
    "minimalist tattoo outline",
    "blackwork tattoo",
    "arm forearm tattoo",
    "anime tattoo art"
]
KEEP_PER_QUERY = 12        # target up to ~60-96 images per Pokemon after dedup
CANDIDATES_PER_QUERY = 25  # fetch extra to cover dead links / duplicates
SIZE = 224                 # matches MobileNetV2 input shape
HASH_DIST = 6              # perceptual-hash distance treated as duplicate

SEARCH_OVERRIDES = {
    29: "nidoran female", 32: "nidoran male", 122: "mr mime", 250: "ho-oh", 386: "deoxys",
    413: "wormadam", 439: "mime jr", 474: "porygon-z", 487: "giratina", 492: "shaymin",
    550: "basculin", 555: "darmanitan", 592: "frillish", 593: "jellicent", 641: "tornadus",
    642: "thundurus", 645: "landorus", 647: "keldeo", 648: "meloetta", 668: "pyroar",
    678: "meowstic", 681: "aegislash", 710: "pumpkaboo", 711: "gourgeist", 718: "zygarde",
    741: "oricorio", 745: "lycanroc", 746: "wishiwashi", 772: "type null", 774: "minior",
    778: "mimikyu", 782: "jangmo-o", 783: "hakamo-o", 784: "kommo-o", 849: "toxtricity",
    866: "mr rime", 875: "eiscue", 876: "indeedee", 877: "morpeko", 892: "urshifu",
    902: "basculegion", 905: "enamorus", 916: "oinkologne", 925: "maushold",
    931: "squawkabilly", 964: "palafin", 978: "tatsugiri", 982: "dudunsparce",
    1001: "wo-chien", 1002: "chien-pao", 1003: "ting-lu", 1004: "chi-yu",
}
UA = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    "(KHTML, like Gecko) Chrome/124.0 Safari/537.36"}


def safe_folder(name):
    """Windows-safe folder name."""
    return re.sub(r'[<>:"/\\|?*]', "", name).strip().rstrip(".")


def search_urls(query, count):
    """DuckDuckGo image search with exponential backoff on rate-limit."""
    for attempt in range(4):
        try:
            with DDGS() as d:
                res = list(d.images(query, safesearch="on", max_results=count))
            return [r["image"] for r in res if r.get("image")][:count]
        except Exception as e:
            wait = 15 * (attempt + 1)
            print(f"   search error ({type(e).__name__}); retrying in {wait}s", flush=True)
            time.sleep(wait)
    return []


def fetch(url):
    try:
        req = urllib.request.Request(url, headers=UA)
        data = urllib.request.urlopen(req, timeout=10).read(8_000_000)
        img = Image.open(io.BytesIO(data))
        img.load()
        return img
    except Exception:
        return None


def to_square(img):
    if img.mode in ("RGBA", "LA", "P"):
        img = img.convert("RGBA")
        bg = Image.new("RGBA", img.size, (255, 255, 255, 255))
        img = Image.alpha_composite(bg, img)
    img = img.convert("RGB")
    img.thumbnail((SIZE, SIZE), Image.LANCZOS)
    canvas = Image.new("RGB", (SIZE, SIZE), (255, 255, 255))
    canvas.paste(img, ((SIZE - img.width) // 2, (SIZE - img.height) // 2))
    return canvas


class Saver:
    def __init__(self, folder):
        self.folder, self.seen, self.n = folder, [], 0

    def add(self, img):
        try:
            if min(img.size) < 64:                     # skip tiny thumbnails
                return False
            sq = to_square(img)
            h = imagehash.phash(sq)
            if any(h - s <= HASH_DIST for s in self.seen):
                return False
            self.seen.append(h)
            sq.save(os.path.join(self.folder, f"{self.n:04d}.jpg"), "JPEG", quality=85, optimize=True)
            self.n += 1
            return True
        except Exception:
            return False


def main():
    ap = argparse.ArgumentParser(description="Scrapes Pokemon tattoo images for all 1,025 Pokemon.")
    ap.add_argument("--limit", type=int, default=None, help="only first N Pokemon (smoke test)")
    ap.add_argument("--start-id", type=int, default=1, help="start scraping at Pokédex ID N")
    ap.add_argument("--workers", type=int, default=16, help="concurrent image fetch threads")
    ap.add_argument("--out-dir", type=str, default=DEFAULT_OUT, help="output directory for images")
    ap.add_argument("--pokemon", type=str, default=None, help="comma-separated names of specific Pokemon to scrape")
    args = ap.parse_args()

    out_dir = os.path.abspath(args.out_dir)
    os.makedirs(out_dir, exist_ok=True)
    print(f"Output directory: {out_dir}")

    data = json.load(open(MASTER, encoding="utf-8"))
    mons = sorted({(p["id"], p["name"]) for p in data if 1 <= p["id"] <= 1025})

    if args.pokemon:
        target_names = {x.strip().lower() for x in args.pokemon.split(",")}
        mons = [m for m in mons if m[1].lower() in target_names]
    else:
        mons = [m for m in mons if m[0] >= args.start_id]
        if args.limit:
            mons = mons[:args.limit]

    print(f"Total Pokemon to scrape: {len(mons)}")

    label_map = {safe_folder(n): n for _, n in mons}
    json.dump(label_map, open(os.path.join(out_dir, "label_map.json"), "w", encoding="utf-8"), indent=1)

    pool = ThreadPoolExecutor(max_workers=args.workers)
    start = time.time()

    for i, (pid, name) in enumerate(mons, 1):
        folder = os.path.join(out_dir, safe_folder(name))
        done = os.path.join(folder, ".done")
        if os.path.exists(done):
            print(f"[{i}/{len(mons)}] #{pid} {name}: already done (skipping).", flush=True)
            continue

        shutil.rmtree(folder, ignore_errors=True)       # restart partial folders cleanly
        os.makedirs(folder, exist_ok=True)
        saver = Saver(folder)

        # Pre-load hashes from other variant folders to avoid duplicates
        for existing_dir in ["varaiants", "varaiants_round3", "varaiants_3d_prints", "varaiants_sleep"]:
            old_folder = os.path.join(REF_IMGS_BASE, existing_dir, safe_folder(name))
            if os.path.isdir(old_folder):
                for old_f in os.listdir(old_folder):
                    if old_f.endswith(".jpg"):
                        try:
                            old_img = Image.open(os.path.join(old_folder, old_f))
                            saver.seen.append(imagehash.phash(old_img))
                        except Exception:
                            pass

        search_name = SEARCH_OVERRIDES.get(pid, name.replace("-", " "))
        for q in QUERIES:
            query_str = f"{search_name} pokemon {q}"
            urls = search_urls(query_str, CANDIDATES_PER_QUERY)
            kept = 0
            for img in pool.map(fetch, urls):
                if kept >= KEEP_PER_QUERY:
                    break
                if img is not None and saver.add(img):
                    kept += 1
            time.sleep(random.uniform(1.5, 3.0))

        open(done, "w").write(str(saver.n))
        el = time.time() - start
        print(f"[{i}/{len(mons)}] #{pid} {name}: {saver.n} images ({el/60:.1f} min elapsed)", flush=True)
        time.sleep(random.uniform(1.0, 2.0))

    pool.shutdown()
    print(f"Completed scraping! Images stored in: {out_dir}")


if __name__ == "__main__":
    main()
