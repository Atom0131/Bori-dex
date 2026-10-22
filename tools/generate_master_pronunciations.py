r"""
Master Pokemon Pronunciation Generator

Combines:
1. Official Bulbapedia / Pokedex 3D Pro / Pokemon Handbook pronunciations (Gens 1-7)
2. Curated modern phonetic lexicon (Gen 8 Galar/Hisui + Gen 9 Paldea/Kitakami/Blueberry)
3. Ultra Beasts, Paradox Pokemon, Hyphenated & Multi-Word legendaries
4. All 326 Alternate Forms & Megas

Outputs:
  PokedexApp/app/src/main/assets/pokemon_pronunciations.json
"""

import os
import sys
import json
import re

# Set UTF-8 output encoding
if sys.stdout and hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

BASE = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(BASE, "..", ".."))

MASTER_JSON = os.path.join(PROJECT_ROOT, "PokedexApp", "app", "src", "main", "assets", "pokemon_data.json")
BULBAPEDIA_JSON = os.path.join(PROJECT_ROOT, "bulbapedia_pronunciations.json")
OUTPUT_JSON = os.path.join(PROJECT_ROOT, "PokedexApp", "app", "src", "main", "assets", "pokemon_pronunciations.json")

# ---------------------------------------------------------------------------
# 1. Bulbapedia Official Base Extraction
# ---------------------------------------------------------------------------
def extract_bulbapedia_pronunciations():
    if not os.path.exists(BULBAPEDIA_JSON):
        print(f"[Warning] {BULBAPEDIA_JSON} not found!")
        return {}

    with open(BULBAPEDIA_JSON, 'r', encoding='utf-8') as f:
        bp_data = json.load(f)

    wiki_text = list(bp_data['query']['pages'].values())[0]['revisions'][0]['slots']['main']['*']
    lines = wiki_text.split('\n')

    bp_dict = {}
    current_name = None
    for line in lines:
        m = re.search(r'\{\{p\|([^}]+)\}\}', line)
        if m:
            current_name = m.group(1).strip().lower()
            continue
        if current_name and line.startswith('|') and not line.startswith('|-') and not line.startswith('|rowspan') and not line.startswith('|{{') and not line.startswith('|/'):
            val = line[1:].strip()
            val = re.sub(r'<ref[^>]*>.*?</ref>', '', val)
            val = re.sub(r'<ref[^>]*/>', '', val)
            val = re.sub(r'<br\s*/?>', '; ', val)
            val = re.sub(r'\[https?://[^\s]+ ([^\]]+)\]', r'\1', val)
            val = val.strip()
            if val and not val.startswith('/') and not val.isdigit() and len(val) > 1:
                if current_name not in bp_dict:
                    choices = [c.strip() for c in val.split(';') if c.strip()]
                    if choices:
                        bp_dict[current_name] = choices[0]
    return bp_dict

# ---------------------------------------------------------------------------
# 2. Curated Supplemental Pronunciations (Gen 8, Gen 9, Ultra Beasts, Legends)
# ---------------------------------------------------------------------------
CURATED_PRONUNCIATIONS = {
    # Sun & Moon (Gen 7) Legendaries, Ultra Beasts & Mythicals
    "solgaleo": "Soul-Gah-Lay-Oh",
    "lunala": "Loo-Nah-Luh",
    "nihilego": "Nie-Uh-Lee-Go",
    "buzzwole": "Buzz-Wole",
    "pheromosa": "Fair-Uh-Mo-Suh",
    "xurkitree": "Zurk-Ih-Tree",
    "celesteela": "Sell-Uh-Steel-Uh",
    "kartana": "Kar-Tah-Nuh",
    "guzzlord": "Guzz-Lord",
    "necrozma": "Neh-Krohz-Muh",
    "magearna": "Muh-Geer-Nuh",
    "marshadow": "Mar-Shad-Oh",
    "poipole": "Poy-Pull",
    "naganadel": "Naw-Guh-Naw-Duhl",
    "stakataka": "Stack-Uh-Tack-Uh",
    "blacephalon": "Blass-Sef-Uh-Lawn",
    "zeraora": "Zair-Uh-Or-Uh",
    "meltan": "Mel-Tan",
    "melmetal": "Mel-Met-Ul",
    "silvally": "Sill-Val-Ee",
    "bruxish": "Bruk-Sish",

    # Key Legendaries & Hard-To-Pronounce Favorites
    "rayquaza": "Ray-Kway-Zuh",
    "arceus": "Ar-Key-Us",
    "suicune": "Swee-Koon",
    "kyogre": "Kye-Oh-Gur",
    "groudon": "Grow-Don",
    "gyarados": "Gair-Ih-Dose",
    "pidgeot": "Pid-Jit",
    "rattata": "Ruh-Tah-Tah",
    "regice": "Redj-Ice",
    "yveltal": "Ee-Vel-Tall",
    "xerneas": "Zur-Nee-Us",
    "zygarde": "Zye-Gard",
    "machamp": "May-Champ",
    "farfetchd": "Far-Fetcht",
    "flabebe": "Flah-Beh-Bay",
    "snom": "Snohm",

    # Gen 8 - Galar & Hisui
    "grookey": "GROO-kee",
    "thwackey": "THWACK-ee",
    "rillaboom": "RILL-uh-boom",
    "scorbunny": "SKOR-bun-ee",
    "raboot": "RA-boot",
    "cinderace": "SIN-der-ace",
    "sobble": "SOB-ul",
    "drizzile": "DRIZ-ul",
    "inteleon": "in-TELL-ee-on",
    "skwovet": "SKWO-vet",
    "greedent": "GREE-dent",
    "rookidee": "ROOK-ih-dee",
    "corvisquire": "KOR-vih-skwire",
    "corviknight": "KOR-vih-night",
    "blipbug": "BLIP-bug",
    "dottler": "DOT-ler",
    "orbeetle": "OR-bee-tul",
    "nickit": "NICK-it",
    "thievul": "THEE-vul",
    "gossifleur": "GOSS-ih-flur",
    "eldegoss": "EL-deh-goss",
    "wooloo": "WOO-loo",
    "dubwool": "DUB-wool",
    "chewtle": "CHOO-tul",
    "drednaw": "DRED-naw",
    "yamper": "YAM-purr",
    "boltund": "BOLE-tund",
    "rolycoly": "ROE-lee-KOE-lee",
    "carkol": "KAR-kole",
    "coalossal": "kuh-LOSS-ul",
    "applin": "AP-lin",
    "flapple": "FLAP-ul",
    "appletun": "AP-ul-ton",
    "silicobra": "SILL-ih-KOE-bruh",
    "sandaconda": "SAND-uh-KON-duh",
    "cramorant": "KRAM-uh-rant",
    "arrokuda": "AIR-oh-KOO-duh",
    "barraskewda": "BARE-uh-SKEW-duh",
    "toxel": "TOX-ul",
    "toxtricity": "tox-TRISS-ih-tee",
    "sizzlipede": "SIZZ-lee-peed",
    "centiskorch": "SEN-tih-skorch",
    "clobbopus": "KLOB-uh-puss",
    "grapploct": "GRAP-lockt",
    "sinistea": "SIN-iss-TEE",
    "polteageist": "POLE-ter-GAYST",
    "hatenna": "huh-TEN-uh",
    "hattrem": "HAT-rem",
    "hatterene": "HAT-er-een",
    "impidimp": "IMP-ih-dimp",
    "morgrem": "MOR-grem",
    "grimmsnarl": "GRIM-snarl",
    "obstagoon": "OB-stuh-goon",
    "perrserker": "purr-SIR-kurr",
    "cursola": "kur-SO-luh",
    "sirfetchd": "SIR-fetcht",
    "mr-rime": "MISS-ter RHYME",
    "runerigus": "ROON-er-EE-gus",
    "milcery": "MILL-ser-ee",
    "alcremie": "al-KREE-mee",
    "falinks": "FAY-links",
    "pincurchin": "PIN-kur-chin",
    "frosmoth": "FROSS-moth",
    "stonjourner": "STONE-jur-ner",
    "eiscue": "ICE-cue",
    "indeedee": "in-DEE-dee",
    "morpeko": "mor-PECK-oh",
    "cufant": "CUE-fant",
    "copperajah": "KOP-er-RAH-juh",
    "dracozolt": "DRAY-koe-ZOLT",
    "arctozolt": "ARK-toe-ZOLT",
    "dracovish": "DRAY-koe-VISH",
    "arctovish": "ARK-toe-VISH",
    "duraludon": "dyur-AL-yoo-don",
    "dreepy": "DREE-pee",
    "drakloak": "drak-LOKE",
    "dragapult": "DRAG-uh-pult",
    "zacian": "ZAH-shee-an",
    "zamazenta": "ZAH-mah-ZEN-tuh",
    "eternatus": "ee-TUR-nuh-tuss",
    "kubfu": "KUB-foo",
    "urshifu": "UR-shih-foo",
    "zarude": "zuh-ROOD",
    "regieleki": "RED-jee-ee-LECK-ee",
    "regidrago": "RED-jee-DRAY-go",
    "glastrier": "GLAS-tree-er",
    "spectrier": "SPECK-tree-er",
    "calyrex": "KAL-ih-rex",
    "wyrdeer": "WEER-deer",
    "kleavor": "KLEE-vor",
    "ursaluna": "UR-suh-LOO-nuh",
    "basculegion": "BASS-cue-LEE-jun",
    "sneasler": "SNEEZ-ler",
    "overqwil": "OH-ver-quil",
    "enamorus": "ee-NAM-er-us",

    # Gen 9 - Paldea, Kitakami & Blueberry
    "sprigatito": "spree-guh-TEE-toe",
    "floragato": "FLOR-uh-GAH-toe",
    "meowscarada": "MEOW-skuh-RAH-duh",
    "fuecoco": "fway-COE-coe",
    "crocalor": "KROE-kuh-lor",
    "skeledirge": "SKELL-eh-dirj",
    "quaxly": "KWACKS-lee",
    "quaxwell": "KWACKS-well",
    "quaquaval": "kwah-KWAH-vuhl",
    "lechonk": "leh-CHONK",
    "oinkologne": "OINK-uh-LONE",
    "tarountula": "tuh-RAWN-chuh-luh",
    "spidops": "SPY-dops",
    "nymble": "NIM-bul",
    "lokix": "LOW-kicks",
    "pawmi": "PAW-mee",
    "pawmo": "PAW-moe",
    "pawmot": "PAW-mot",
    "tandemaus": "TAN-duh-mouse",
    "maushold": "MOUSE-hold",
    "fidough": "FYE-doe",
    "dachsbun": "DOX-bun",
    "smoliv": "SMOE-liv",
    "dolliv": "DOLE-liv",
    "arboliva": "AR-boe-LEE-vuh",
    "squawkabilly": "SKWAWK-uh-bill-ee",
    "nacli": "NACK-lee",
    "naclstack": "NACK-ul-stack",
    "garganacl": "gar-GAN-uh-kuhl",
    "charcadet": "CHAR-kuh-det",
    "armarouge": "AR-muh-ROOZH",
    "ceruledge": "seh-ROO-ledge",
    "tadbulb": "TAD-bulb",
    "bellibolt": "BELL-ee-bolt",
    "wattrel": "WAHT-rul",
    "kilowattrel": "KILL-oh-WAHT-rul",
    "maschiff": "MASS-chiff",
    "mabosstiff": "muh-BOSS-tiff",
    "shroodle": "SHROO-dul",
    "grafaiai": "GRAF-eye-eye",
    "bramblin": "BRAM-blin",
    "brambleghast": "BRAM-bul-ghast",
    "toedscool": "TOAD-skool",
    "toedscruel": "TOAD-skroo-ul",
    "klawf": "KLAW-ff",
    "capsakid": "CAP-suh-kid",
    "scovillain": "SKOE-vill-un",
    "rellor": "RELL-or",
    "rabsca": "RAB-skuh",
    "flittle": "FLIT-ul",
    "espathra": "ess-PAH-thruh",
    "tinkatink": "TINK-uh-tink",
    "tinkatuff": "TINK-uh-tuff",
    "tinkaton": "TINK-uh-ton",
    "wiglett": "WIG-let",
    "wugtrio": "WUG-tree-oh",
    "bombirdier": "BOM-bur-deer",
    "finizen": "FIN-ih-zen",
    "palafin": "PAL-uh-fin",
    "varoom": "vuh-ROOM",
    "revavroom": "rev-uh-VROOM",
    "cyclizar": "SYE-clih-zar",
    "orthworm": "ORTH-wurm",
    "glimmet": "GLIM-it",
    "glimmora": "glim-MOR-uh",
    "greavard": "GREE-vard",
    "houndstone": "HOUND-stone",
    "flamigo": "fluh-MEE-go",
    "cetoddle": "seh-TODD-ul",
    "cetitan": "see-TYE-tan",
    "veluza": "veh-LOO-zuh",
    "dondozo": "don-DOE-zoe",
    "tatsugiri": "TAT-soo-GEER-ee",
    "annihilape": "uh-NYE-uh-lape",
    "clodsire": "KLOD-sire",
    "farigiraf": "fuh-RIJ-er-af",
    "dudunsparce": "doo-DUN-sparse",
    "kingambit": "KING-gam-bit",
    "great-tusk": "Great Tusk",
    "scream-tail": "Scream Tail",
    "brute-bonnet": "Brute Bonnet",
    "flutter-mane": "Flutter Mane",
    "slither-wing": "Slither Wing",
    "sandy-shocks": "Sandy Shocks",
    "iron-treads": "Iron Treads",
    "iron-bundle": "Iron Bundle",
    "iron-hands": "Iron Hands",
    "iron-jugulis": "Iron Jug-yoo-liss",
    "iron-moth": "Iron Moth",
    "iron-thorns": "Iron Thorns",
    "frigibax": "FRIJ-ih-bax",
    "arctibax": "ARK-tih-bax",
    "baxcalibur": "BAX-cal-ih-bur",
    "gimmighoul": "GIM-ee-ghool",
    "gholdengo": "GOAL-den-go",
    "wo-chien": "WOE-chee-en",
    "chien-pao": "CHEE-en-pow",
    "ting-lu": "TING-loo",
    "chi-yu": "CHEE-yoo",
    "roaring-moon": "Roaring Moon",
    "iron-valiant": "Iron Val-ee-unt",
    "koraidon": "koe-RYE-don",
    "miraidon": "mee-RYE-don",
    "walking-wake": "Walking Wake",
    "iron-leaves": "Iron Leaves",
    "dipplin": "DIP-lin",
    "poltchageist": "POLE-chuh-GAYST",
    "sinistcha": "sin-ISS-chuh",
    "okidogi": "OH-kee-DOE-gee",
    "munkidori": "MUN-kee-DOR-ee",
    "fezandipiti": "FEZ-un-DIP-ih-tee",
    "ogerpon": "OH-ger-pahn",
    "archaludon": "ar-kuh-LOO-don",
    "hydrapple": "high-DRAP-ul",
    "gouging-fire": "Gouging Fire",
    "raging-bolt": "Raging Bolt",
    "iron-boulder": "Iron Bowl-der",
    "iron-crown": "Iron Crown",
    "terapagos": "teh-RAH-pah-gos",
    "pecharunt": "PETCH-uh-runt",

    # Hyphenated & Multi-Word Names
    "tapu-koko": "Tah-Poo Koe-Koe",
    "tapu-lele": "Tah-Poo Lay-Lay",
    "tapu-bulu": "Tah-Poo Boo-Loo",
    "tapu-fini": "Tah-Poo Fee-Nee",
    "tapu koko": "Tah-Poo Koe-Koe",
    "tapu lele": "Tah-Poo Lay-Lay",
    "tapu bulu": "Tah-Poo Boo-Loo",
    "tapu fini": "Tah-Poo Fee-Nee",
    "type-null": "Type Null",
    "type null": "Type Null",
    "ho-oh": "Hoe-Oh",
    "porygon-z": "Pore-Ee-Gon Zed",
    "mr-mime": "Miss-Ter Mime",
    "mr mime": "Miss-Ter Mime",
    "mime-jr": "Mime Junior",
    "mime jr": "Mime Junior",
    "mr-rime": "Miss-Ter Rhyme",
    "mr rime": "Miss-Ter Rhyme",
    "jangmo-o": "Jang-Moe-Oh",
    "hakamo-o": "Hah-Kuh-Moe-Oh",
    "kommo-o": "Kom-Moe-Oh"
}

def normalize_syllables(val):
    """Normalizes all-caps or mixed phonetic strings into clean TitleCase hyphenated syllables."""
    val = val.strip()
    # Replace standard pronunciation artifacts
    val = re.sub(r'(?i)\bsore\b', 'sawr', val)
    val = re.sub(r'(?i)\bsohr\b', 'sawr', val)
    
    parts = val.split('-')
    norm_parts = []
    for p in parts:
        words = p.split()
        norm_words = [w.capitalize() for w in words]
        norm_parts.append(' '.join(norm_words))
    return '-'.join(norm_parts)

def build_master_dictionary():
    print("=================================================================")
    print("  BUILDING MASTER POKEMON PRONUNCIATION LEXICON (1,025 + FORMS)")
    print("=================================================================")

    # 1. Load pokemon_data.json
    with open(MASTER_JSON, 'r', encoding='utf-8') as f:
        pk_data = json.load(f)

    base_species = {p['name'].lower().strip(): p['id'] for p in pk_data if p.get('id') <= 1025}
    form_species = {p['name'].lower().strip(): p['id'] for p in pk_data if p.get('id') > 1025}
    print(f"Loaded {len(base_species)} base Pokemon and {len(form_species)} forms from pokemon_data.json.")

    # 2. Extract Bulbapedia
    bp_dict = extract_bulbapedia_pronunciations()
    print(f"Extracted {len(bp_dict)} official pronunciations from Bulbapedia.")

    # 3. Layer dictionaries
    combined = {}
    for k, v in bp_dict.items():
        combined[k.lower().strip()] = v
    for k, v in CURATED_PRONUNCIATIONS.items():
        combined[k.lower().strip()] = v

    # 4. Generate Master Dictionary
    master_dict = {}

    # Base Pokemon
    for name, dex_id in sorted(base_species.items(), key=lambda x: x[1]):
        clean = name.replace("'", "").replace("é", "e")
        base = clean.split('-')[0]

        val = combined.get(name) or combined.get(clean) or combined.get(base)
        if val:
            master_dict[name] = normalize_syllables(val)
        else:
            master_dict[name] = name.title()

    # Alternate Forms (Megas, Primals, Regional, etc.)
    for form_name in form_species.keys():
        # Check direct match
        if form_name in combined:
            master_dict[form_name] = normalize_syllables(combined[form_name])
            continue

        # Extract base species prefix
        base_match = None
        for bname in sorted(base_species.keys(), key=len, reverse=True):
            if form_name.startswith(bname + "-") or form_name == bname:
                base_match = bname
                break
        
        if not base_match:
            base_match = form_name.split('-')[0]

        base_phonetic = master_dict.get(base_match, base_match.title())
        form_suffix = form_name[len(base_match):].strip('-').replace('-', ' ').title()

        # Build compound form phonetic
        if form_suffix:
            master_dict[form_name] = f"{form_suffix} {base_phonetic}"
        else:
            master_dict[form_name] = base_phonetic

    # Add space-separated variants of hyphenated names for robust speech matching
    hyphenated_keys = list(master_dict.keys())
    for k in hyphenated_keys:
        if '-' in k:
            space_key = k.replace('-', ' ')
            if space_key not in master_dict:
                master_dict[space_key] = master_dict[k]

    # Save to assets
    with open(OUTPUT_JSON, 'w', encoding='utf-8') as f:
        json.dump(master_dict, f, indent=2)

    print(f"\n[+] Successfully saved {len(master_dict)} pronunciations to {OUTPUT_JSON}!")

    # Verify key regression tests
    test_cases = [
        ("solgaleo", "Soul-Gah-Lay-Oh"),
        ("lunala", "Loo-Nah-Luh"),
        ("rayquaza", "Ray-Kway-Zuh"),
        ("arceus", "Ar-Key-Us"),
        ("suicune", "Swee-Koon"),
        ("kyogre", "Kye-Oh-Gur"),
        ("groudon", "Grow-Don"),
        ("nihilego", "Nie-Uh-Lee-Go"),
        ("xurkitree", "Zurk-Ih-Tree"),
        ("celesteela", "Sell-Uh-Steel-Uh"),
        ("stakataka", "Stack-Uh-Tack-Uh"),
        ("blacephalon", "Blass-Sef-Uh-Lawn"),
        ("necrozma", "Neh-Krohz-Muh"),
        ("tapu-koko", "Tah-Poo-Koe-Koe"),
        ("koraidon", "Koe-Rye-Don"),
        ("miraidon", "Mee-Rye-Don")
    ]

    print("\n--- Regression Verification ---")
    all_passed = True
    for test_key, expected in test_cases:
        actual = master_dict.get(test_key)
        # Check normalized match
        match = actual and actual.lower().replace('-', '') == expected.lower().replace('-', '')
        status = "PASSED" if match else "FAILED"
        if not match:
            all_passed = False
        print(f"  [{status}] {test_key} -> actual: '{actual}' | expected: '{expected}'")

    if all_passed:
        print("\n[SUCCESS] All pronunciation regression tests PASSED!")
    else:
        print("\n[WARNING] Some regression tests did not match exactly.")

    return master_dict

if __name__ == "__main__":
    build_master_dictionary()
