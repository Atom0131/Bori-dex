import json
import re
import time

t0 = time.time()
DATA_PATH = r"C:\Users\pagan\OneDrive\Documents\Code\Pokedex\PokedexApp\app\src\main\assets\pokemon_data.json"

with open(DATA_PATH, "r", encoding="utf-8") as f:
    data = json.load(f)

# Build uppercase lookup map: e.g. "RAYQUAZA" -> "Rayquaza", "RAYQUAZA'S" -> "Rayquaza's"
upper_name_map = {}
for d in data:
    name = d["name"].lower().strip()
    title_name = " ".join(part.capitalize() for part in name.replace("-", " ").split())
    if name == "ho-oh":
        title_name = "Ho-Oh"
    elif name == "porygon-z":
        title_name = "Porygon-Z"
    elif name == "jangmo-o":
        title_name = "Jangmo-o"
    elif name == "hakamo-o":
        title_name = "Hakamo-o"
    elif name == "kommo-o":
        title_name = "Kommo-o"
    elif name == "mime-jr":
        title_name = "Mime Jr."
    elif name == "mr-mime":
        title_name = "Mr. Mime"
    elif name == "mr-rime":
        title_name = "Mr. Rime"
    
    clean_key = name.replace("-", "").upper()
    upper_name_map[clean_key] = title_name
    upper_name_map[name.upper()] = title_name

ACRONYMS = {"DNA", "TM", "HM", "HP", "TV", "AM", "PM", "PC", "UFO", "US", "UK", "ID", "OK", "X", "Y", "Z", "II", "III", "IV", "VI", "VII", "VIII", "IX"}

word_pattern = re.compile(r"\b[A-Za-z0-9'’\-]+\b")

def clean_text(text: str) -> str:
    if not text:
        return text
    
    t = text
    # 1. Strip page breaks, soft hyphens, line breaks
    t = t.replace("\u000c", " ").replace("\n", " ").replace("\r", " ")
    t = t.replace("\u2019", "'").replace("\u2018", "'").replace("\u201c", '"').replace("\u201d", '"')
    t = t.replace("\u00ad", "")
    t = t.replace("-\n", "").replace("-\r\n", "")
    
    # 2. Normalize Pokémon references
    t = re.sub(r'(?i)POK[eé]-\s*MON', 'Pokemon', t)
    t = re.sub(r'(?i)\bPOK[eé]MON\b', 'Pokemon', t)
    
    # 3. Fast single-pass token replacement for all-caps words
    def replace_word(m):
        w = m.group(0)
        # Check if entire token is uppercase letters (2 or more chars)
        if len(w) >= 2 and w.isupper():
            if w in ACRONYMS:
                return w
            # Check for trailing 'S (e.g. VENUSAUR'S)
            has_possessive = w.endswith("'S") or w.endswith("’S")
            base = w[:-2] if has_possessive else w
            
            if base in upper_name_map:
                res = upper_name_map[base]
                return res + ("'s" if has_possessive else "")
            
            # Shouting generic word -> lowercase
            return w.lower()
        return w

    t = word_pattern.sub(replace_word, t)
    
    # 4. Capitalize first letter of every sentence
    def capitalize_sentence(m):
        return m.group(1) + m.group(2).upper()

    t = re.sub(r'(^|[.!?]\s+)([a-z])', capitalize_sentence, t)
    
    # 5. Normalize whitespace
    t = re.sub(r'\s+', ' ', t).strip()
    return t

count_cleaned = 0
for d in data:
    old_flavor = d.get("flavorText", "")
    new_flavor = clean_text(old_flavor)
    if old_flavor != new_flavor:
        d["flavorText"] = new_flavor
        count_cleaned += 1
        
    pokedex_entries = d.get("pokedexEntries", {})
    for ver, entry_text in pokedex_entries.items():
        pokedex_entries[ver] = clean_text(entry_text)

print(f"Cleaned flavor texts across {count_cleaned} / {len(data)} entries in {time.time() - t0:.2f}s.")

with open(DATA_PATH, "w", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print("Saved updated pokemon_data.json successfully.")
