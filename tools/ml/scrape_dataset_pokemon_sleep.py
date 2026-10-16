r"""
Optimized High-Speed Scraper for Pokemon Sleep game assets, sleep styles,
sleeping plushies, sleeping figures, and sleep accessories (Pokemon GO Plus + / Sleep Band).

Performance tuned:
- 32 concurrent download workers
- Tight 20-candidate fetch window
- Fast search cooldowns (0.35s - 0.65s)
- Direct in-batch perceptual hash deduplication without redundant disk scans

Output: Pokemon_ref_imgs/varaiants_sleep/<pokemon_name>/NNNN.jpg (224x224 RGB JPEG q85, letterboxed)
Resumable: finished Pokemon get a `.done` marker and are skipped on re-run.
"""
import os, re, json, time, random, shutil, argparse, io
import urllib.request, urllib.parse
from concurrent.futures import ThreadPoolExecutor
from PIL import Image
import imagehash
from ddgs import DDGS

BASE = os.path.dirname(os.path.abspath(__file__))
MASTER = os.path.join(BASE, "..", "PokedexApp", "app", "src", "main", "assets", "pokemon_data.json")
OUT = os.path.join(BASE, "varaiants_sleep")

QUERIES = [
    "pokemon sleep game screenshot",
    "pokemon sleep style artwork",
    "sleeping plush toy",
    "sleeping figure pose",
    "pokemon sleep band go plus accessory",
    "asleep eyes closed curled up",
    "nightcap sleeping pillow",
    "slumbering rest pose"
]
KEEP_PER_QUERY = 15        # target up to ~80-120 images per Pokemon after dedup
CANDIDATES_PER_QUERY = 20  # lean candidate count to maximize throughput
SIZE = 224                 # matches model input; smallest useful size
HASH_DIST = 6              # perceptual-hash distance treated as duplicate
MAX_WORKERS = 32           # high concurrency for parallel HTTP image downloads

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
    """DuckDuckGo image search with fast exponential backoff on rate-limit."""
    for attempt in range(4):
        try:
            with DDGS() as d:
                res = d.images(query, safesearch="on", max_results=count)
            return [r["image"] for r in res if r.get("image")][:count]
        except Exception as e:
            wait = 10 * (attempt + 1)
            print(f"   search error ({type(e).__name__}); retrying in {wait}s", flush=True)
            time.sleep(wait)
    return []


def fetch(url):
    try:
        req = urllib.request.Request(url, headers=UA)
        data = urllib.request.urlopen(req, timeout=6).read(8_000_000)
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
    ap = argparse.ArgumentParser()
    ap.add_argument("--limit", type=int, default=None, help="only first N Pokemon (smoke test)")
    args = ap.parse_args()

    data = json.load(open(MASTER, encoding="utf-8"))
    mons = sorted({(p["id"], p["name"]) for p in data if 1 <= p["id"] <= 1025})
    if args.limit:
        mons = mons[:args.limit]
    os.makedirs(OUT, exist_ok=True)

    label_map = {safe_folder(n): n for _, n in mons}
    json.dump(label_map, open(os.path.join(OUT, "label_map.json"), "w", encoding="utf-8"), indent=1)

    pool = ThreadPoolExecutor(max_workers=MAX_WORKERS)
    start = time.time()

    for i, (pid, name) in enumerate(mons, 1):
        folder = os.path.join(OUT, safe_folder(name))
        done = os.path.join(folder, ".done")
        if os.path.exists(done):
            continue
        shutil.rmtree(folder, ignore_errors=True)       # restart partial folders cleanly
        os.makedirs(folder)
        saver = Saver(folder)

        search_name = SEARCH_OVERRIDES.get(pid, name.replace("-", " "))
        for q in QUERIES:
            urls = search_urls(f"{search_name} pokemon {q}", CANDIDATES_PER_QUERY)
            kept = 0
            for img in pool.map(fetch, urls):
                if kept >= KEEP_PER_QUERY:
                    break
                if img is not None and saver.add(img):
                    kept += 1
            time.sleep(random.uniform(0.35, 0.65))

        open(done, "w").write(str(saver.n))
        el = time.time() - start
        print(f"[{i}/{len(mons)}] #{pid} {name}: {saver.n} images  ({el/60:.1f} min elapsed)", flush=True)

    pool.shutdown()
    print("All done!")


if __name__ == "__main__":
    main()
