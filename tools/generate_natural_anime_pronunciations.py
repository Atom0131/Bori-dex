r"""
Natural Anime Pronunciation Generator
Produces fluid, natural speech for all 1,025 Pokemon and alternate forms.

Core Architecture:
1. Standard Pokemon (Pikachu, Charizard, Bulbasaur, Squirtle, Eevee, Lunala, Lucario, etc.)
   use their clean, un-hyphenated English names ("Pikachu").
   This allows Android TTS to use native dictionary coarticulation with zero robotic pauses.
2. Irregular / Non-Intuitive Pokemon (Solgaleo, Rayquaza, Suicune, Arceus, Gyarados, Kyogre)
   use un-hyphenated single-word acoustic respellings ("Solgalayo", "Raykwayza", "Sweekoon", "Arkayus").
3. Multi-word and hyphenated legends (Mr. Mime, Tapu Koko, Iron Treads, Ho-Oh)
   use clean space-separated title-cased words.

Outputs:
  PokedexApp/app/src/main/assets/pokemon_pronunciations.json
"""

import os
import sys
import json
import re

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(BASE_DIR, "..", ".."))
DATA_JSON = os.path.join(PROJECT_ROOT, "PokedexApp", "app", "src", "main", "assets", "pokemon_data.json")
OUTPUT_JSON = os.path.join(PROJECT_ROOT, "PokedexApp", "app", "src", "main", "assets", "pokemon_pronunciations.json")

# ---------------------------------------------------------------------------
# Irregular Acoustic Respellings for Neural TTS (Single-word, NO hyphens)
# ---------------------------------------------------------------------------
IRREGULAR_ACOUSTIC_RESPELLINGS = {
    # Gen 1 (Kanto)
    "venusaur": "Venasaur",       # Pronounced [ˈviːnəsɔːr], eliminates "Ven-oo-saur"
    "blastoise": "Blastoyce",     # Pronounced [ˈblæs.tɔɪs], guarantees -oyce rather than -toys
    "rattata": "Rattatah",        # Guarantees stress on first syllable
    "growlithe": "Growlith",
    "gyarados": "Gairadose",      # Eliminates "Gee-ar-a-dos", matches anime "GAIR-uh-dose"
    "moltres": "Moltrace",        # Eliminates "Mol-tress", matches anime "Mol-TRACE"
    "farfetchd": "Farfetch'd",
    "farfetch'd": "Farfetch'd",

    # Gen 2 (Johto)
    "feraligatr": "Feraligator",  # Fixes character-limit omission
    "ho-oh": "Ho Oh",             # Eliminates hyphen pause
    "ho oh": "Ho Oh",
    "raikou": "Ryeko",            # Guarantees "Rye-ko" rather than "Ray-ko"
    "suicune": "Sweekoon",        # Eliminates "Soo-ee-soon", matches anime "SWEE-koon"

    # Gen 3 (Hoenn)
    "bagon": "Baygon",            # Eliminates "Bag-on", matches anime "BAY-gon"
    "kyogre": "Kyogur",           # Eliminates "Kye-og-ree", matches anime "KYE-oh-ger"
    "groudon": "Growdon",         # Eliminates "Groo-don", matches anime "GROW-don"
    "rayquaza": "Raykwayza",      # Eliminates "Ray-kwah-zah", matches official [reɪˈkweɪzə]
    "deoxys": "Deeoxiss",         # Eliminates "Dee-ox-ease", matches anime "Dee-OX-iss"
    "regice": "Reji-ice",         # Eliminates "Reh-jiss", matches anime "Redji-ice"

    # Gen 4 (Sinnoh)
    "porygon-z": "Porygon Zee",
    "porygon z": "Porygon Zee",
    "dialga": "Dialga",
    "palkia": "Palkia",
    "giratina": "Geerateena",     # Eliminates "Jir-a-tie-na", matches anime "Gear-uh-TEE-nuh"
    "phione": "Feeonay",          # Matches anime "Fee-OH-nay"
    "manaphy": "Manafee",         # Matches anime "MAN-uh-fee"
    "arceus": "Arkayus",          # Eliminates "Ar-see-us", matches official "Ar-KAY-us"

    # Gen 5 (Unova)
    "cofagrigus": "Coffagrigus",
    "reuniclus": "Reeyuniclus",
    "hydreigon": "Hydrygon",      # Matches anime "High-DRY-gon"

    # Gen 6 (Kalos)
    "flabebe": "Flabebay",        # Eliminates raw "flay-bee-bee"
    "flabébé": "Flabebay",
    "xerneas": "Zerneas",         # Eliminates "Ex-er-nee-us", matches anime "ZUR-nee-us"
    "yveltal": "Eeveltal",        # Eliminates "Why-vel-tal", matches anime "ee-VEL-tall"
    "zygarde": "Zygarde",

    # Gen 7 (Alola)
    "type: null": "Type Null",
    "type-null": "Type Null",
    "type null": "Type Null",
    "jangmo-o": "Jangmo-oh",
    "hakamo-o": "Hakamo-oh",
    "kommo-o": "Kommo-oh",
    "tapu koko": "Tahpoo Koko",
    "tapu lele": "Tahpoo Laylay",
    "tapu bulu": "Tahpoo Booloo",
    "tapu fini": "Tahpoo Feenee",
    "tapu-koko": "Tahpoo Koko",
    "tapu-lele": "Tahpoo Laylay",
    "tapu-bulu": "Tahpoo Booloo",
    "tapu-fini": "Tahpoo Feenee",
    "solgaleo": "Solgalayo",      # Eliminates "Sol-ga-lee-o" (Galileo), produces anime "Soul-gah-lay-oh"
    "nihilego": "Nyheeleego",     # Matches anime "Nye-huh-LEE-go"
    "pheromosa": "Fairamosa",     # Matches anime "Fair-oh-MO-suh"
    "xurkitree": "Zurkitree",     # Matches anime "ZERK-ih-tree"
    "celesteela": "Sellesteela",  # Matches anime "Sell-ess-TEEL-uh"
    "kartana": "Kartana",
    "guzzlord": "Guzzlord",
    "necrozma": "Nekrozma",
    "magearna": "Mageerna",       # Matches anime "Muh-GEER-nuh"
    "stakataka": "Stackatacka",   # Matches anime "Stack-uh-TAH-kuh"
    "blacephalon": "Blaseffalon", # Matches anime "Bluh-SEFF-uh-lon"
    "togedemaru": "Togaydmaru",

    # Gen 8 (Galar & Hisui)
    "mr. mime": "Mister Mime",
    "mr-mime": "Mister Mime",
    "mr mime": "Mister Mime",
    "mime jr.": "Mime Junior",
    "mime-jr": "Mime Junior",
    "mime jr": "Mime Junior",
    "mr. rime": "Mister Rime",
    "mr-rime": "Mister Rime",
    "mr rime": "Mister Rime",
    "zacian": "Zahsheean",        # Matches anime "ZAH-shee-an"
    "zamazenta": "Zamazenta",
    "eternatus": "Eternatus",
    "sirfetchd": "Sirfetch'd",
    "sirfetch'd": "Sirfetch'd",
    "basculegion": "Basculegion",
    "enamorus": "Enamorus",

    # Gen 9 (Paldea & Kitakami / Blueberry)
    "great-tusk": "Great Tusk",
    "great tusk": "Great Tusk",
    "scream-tail": "Scream Tail",
    "scream tail": "Scream Tail",
    "brute-bonnet": "Brute Bonnet",
    "brute bonnet": "Brute Bonnet",
    "flutter-mane": "Flutter Mane",
    "flutter mane": "Flutter Mane",
    "slither-wing": "Slither Wing",
    "slither wing": "Slither Wing",
    "sandy-shocks": "Sandy Shocks",
    "sandy shocks": "Sandy Shocks",
    "iron-treads": "Iron Treads",
    "iron treads": "Iron Treads",
    "iron-bundle": "Iron Bundle",
    "iron bundle": "Iron Bundle",
    "iron-hands": "Iron Hands",
    "iron hands": "Iron Hands",
    "iron-jugulis": "Iron Jugulis",
    "iron jugulis": "Iron Jugulis",
    "iron-moth": "Iron Moth",
    "iron moth": "Iron Moth",
    "iron-thorns": "Iron Thorns",
    "iron thorns": "Iron Thorns",
    "roaring-moon": "Roaring Moon",
    "roaring moon": "Roaring Moon",
    "iron-valiant": "Iron Valiant",
    "iron valiant": "Iron Valiant",
    "walking-wake": "Walking Wake",
    "walking wake": "Walking Wake",
    "iron-leaves": "Iron Leaves",
    "iron leaves": "Iron Leaves",
    "gouging-fire": "Gouging Fire",
    "gouging fire": "Gouging Fire",
    "raging-bolt": "Raging Bolt",
    "raging bolt": "Raging Bolt",
    "iron-boulder": "Iron Boulder",
    "iron boulder": "Iron Boulder",
    "iron-crown": "Iron Crown",
    "iron crown": "Iron Crown",
    "wo-chien": "Wo Chien",
    "wo chien": "Wo Chien",
    "chien-pao": "Chien Pao",
    "chien pao": "Chien Pao",
    "ting-lu": "Ting Loo",
    "ting lu": "Ting Loo",
    "chi-yu": "Chee Yoo",
    "chi yu": "Chee Yoo",
    "koraidon": "Koraidon",
    "miraidon": "Miraidon",
    "gholdengo": "Goldengo",
    "ogerpon": "Ogerpon",
    "terapagos": "Terapagos",
    "pecharunt": "Pecharunt",
    "archaludon": "Archaludon"
}

def clean_to_title_case(name):
    """Convert raw token to grammatical Title Case without hyphens."""
    # Split on hyphens and spaces
    tokens = re.split(r'[-_\s]+', name)
    return " ".join(t.capitalize() for t in tokens if t)

def build_natural_pronunciations():
    if not os.path.exists(DATA_JSON):
        raise FileNotFoundError(f"pokemon_data.json not found at {DATA_JSON}")

    with open(DATA_JSON, 'r', encoding='utf-8') as f:
        data = json.load(f)

    master_dict = {}

    # 1. Base 1025 Pokemon
    base_count = 0
    for p in data:
        raw_name = p.get('name', '').strip()
        if not raw_name:
            continue
        p_id = p.get('id', 0)
        is_base = (p_id <= 1025)
        if is_base:
            base_count += 1

        norm_key = raw_name.lower().strip()
        spaced_key = norm_key.replace('-', ' ')
        hyphen_key = norm_key.replace(' ', '-')

        # Check irregular phonetic override
        if norm_key in IRREGULAR_ACOUSTIC_RESPELLINGS:
            pronunciation = IRREGULAR_ACOUSTIC_RESPELLINGS[norm_key]
        elif spaced_key in IRREGULAR_ACOUSTIC_RESPELLINGS:
            pronunciation = IRREGULAR_ACOUSTIC_RESPELLINGS[spaced_key]
        elif hyphen_key in IRREGULAR_ACOUSTIC_RESPELLINGS:
            pronunciation = IRREGULAR_ACOUSTIC_RESPELLINGS[hyphen_key]
        else:
            # Default to clean grammatical Title Case
            pronunciation = clean_to_title_case(raw_name)

        # Register normalized keys for robust matching in formatForSpeech
        master_dict[norm_key] = pronunciation
        if spaced_key != norm_key:
            master_dict[spaced_key] = pronunciation
        if hyphen_key != norm_key:
            master_dict[hyphen_key] = pronunciation

    # Ensure critical canonical spellings
    for k, v in IRREGULAR_ACOUSTIC_RESPELLINGS.items():
        master_dict[k.lower().strip()] = v

    # Write output
    with open(OUTPUT_JSON, 'w', encoding='utf-8') as f:
        json.dump(master_dict, f, indent=2)

    print(f"Successfully generated {len(master_dict)} natural pronunciation entries!")
    print(f"  Base Pokemon processed: {base_count}")
    print(f"\n--- Quality Assurance Verification ---")
    spot_checks = [
        "pikachu", "charizard", "bulbasaur", "squirtle", "eevee", "lucario",
        "solgaleo", "lunala", "rayquaza", "suicune", "arceus", "gyarados",
        "kyogre", "groudon", "tapu koko", "iron treads", "mr. mime"
    ]
    for sc in spot_checks:
        val = master_dict.get(sc)
        has_hyphen = ("-" in val and sc != "ho-oh" and "jangmo" not in sc)
        status = "[FAIL HYPHEN]" if has_hyphen else "[OK FLUID]"
        print(f"  {status:13s} {sc:15s} -> {repr(val)}")

if __name__ == "__main__":
    build_natural_pronunciations()
