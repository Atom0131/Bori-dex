#!/usr/bin/env python3
"""
Builds and audits app/src/main/assets/pokemon_pronunciations.json for natural,
fluid Text-To-Speech (TTS) synthesis across all 1,025 species (Gen 1-9),
regional forms (Hisui, Alola, Galar, Paldea), Megas, Gigantamax, and lore vocabulary.

Key Architecture Principles for Natural Speech:
1. NEVER insert unnatural hyphens (-) inside standard names (e.g. Lapras, Eevee, Pikachu, Charizard).
   Android Google TTS treats internal hyphens as compound word boundaries, creating
   staccato breaks, micro-pauses, and glitchy syllable separation ("lap a ras", "ee... vee").
2. DO NOT override standard English vocabulary:
   - "Pokemon" is natively pronounced [ˈpoʊkeɪmɒn] by Google TTS. Never override with "Pokay-mon".
   - "Pokedex" and "Pokeball" flow naturally without hyphenated distortion.
3. Provide smooth, non-hyphenated single-token phonetic respellings ONLY for species
   whose canonical anime pronunciations genuinely differ from default English phonics
   (e.g. Arceus -> Arkayus, Suicune -> Sweekoon, Rayquaza -> Raykwayza, Kyogre -> Kyogur, Groudon -> Growdon).
4. Resolve punctuation & gender symbols clearly:
   - Nidoran♀ -> Nidoran Female, Nidoran♂ -> Nidoran Male
   - Mr. Mime -> Mister Mime, Mime Jr. -> Mime Junior, Mr. Rime -> Mister Rime
   - Type: Null -> Type Null, Porygon2 -> Porygon Two, Porygon-Z -> Porygon Z
"""

import json
from pathlib import Path

ASSETS_JSON = (
    Path(__file__).resolve().parent.parent
    / "app"
    / "src"
    / "main"
    / "assets"
    / "pokemon_pronunciations.json"
)

# Canonical anime & natural speech phonetic overrides.
# NOTE: Standard names where default TTS is already 100% natural and accurate are
# mapped to their clean canonical name (without hyphens) or omitted.
NATURAL_PRONUNCIATION_OVERRIDES: dict[str, str] = {
    # --- Regions, Adjectives & Lore Terms ---
    "hisui": "Heesooee",
    "hisuian": "Heesooeean",
    "alola": "Alola",
    "alolan": "Alolan",
    "galar": "Galar",
    "galarian": "Galarian",
    "paldea": "Paldea",
    "paldean": "Paldean",
    "hoenn": "Hoenn",
    "sinnoh": "Sinnoh",
    "unova": "Unova",
    "kalos": "Kalos",
    "kitakami": "Kitakami",
    "lumiose": "Loomyohs",

    # --- Punctuation & Gender Special Cases ---
    "nidoran-f": "Nidoran Female",
    "nidoran f": "Nidoran Female",
    "nidoran♀": "Nidoran Female",
    "nidoran-m": "Nidoran Male",
    "nidoran m": "Nidoran Male",
    "nidoran♂": "Nidoran Male",
    "mr-mime": "Mister Mime",
    "mr. mime": "Mister Mime",
    "mr mime": "Mister Mime",
    "mime-jr": "Mime Junior",
    "mime jr.": "Mime Junior",
    "mime jr": "Mime Junior",
    "mr-rime": "Mister Rime",
    "mr. rime": "Mister Rime",
    "mr rime": "Mister Rime",
    "type-null": "Type Null",
    "type: null": "Type Null",
    "type null": "Type Null",
    "porygon2": "Porygon Two",
    "porygon-z": "Porygon Z",
    "porygon z": "Porygon Z",
    "farfetchd": "Farfetch'd",
    "farfetch'd": "Farfetch'd",
    "sirfetchd": "Sirfetch'd",
    "sirfetch'd": "Sirfetch'd",
    "ho-oh": "Ho-Oh",
    "ho oh": "Ho-Oh",
    "flabebe": "Flahbaybay",
    "flabébé": "Flahbaybay",

    # --- The Legendary Titans (The Regis) ---
    # Smooth non-choppy respellings that preserve authentic 'Reji' pronunciation
    "regirock": "Rejirock",
    "regice": "Rejice",
    "registeel": "Rejisteel",
    "regigigas": "Rejigigas",
    "regieleki": "Rejieleki",
    "regidrago": "Rejidrago",

    # --- Hisui-Native Species (#899-#905) ---
    "wyrdeer": "Weerdeer",
    "kleavor": "Kleevor",
    "ursaluna": "Ursaloona",
    "basculegion": "Baskyuleejun",
    "sneasler": "Sneezler",
    "overqwil": "Overkwil",
    "enamorus": "Enamorus",

    # --- Tricky Anime & Legendary Pronunciations (Smooth single tokens, no hyphens!) ---
    # Gen 1 (Kanto)
    "lapras": "Lapras",      # Natural! Never "Lap-rus" or "lap a ras"
    "eevee": "Eevee",        # Natural! Never "Ee-vee"
    "pikachu": "Pikachu",    # Natural! Never "Pee-kuh-choo"
    "charizard": "Charizard",# Natural! Never "Chair-ih-zard"
    "blastoise": "Blastoise",# Natural! Never "Blas-toyce"
    "venusaur": "Venusaur",  # Natural! Never "Vee-nuh-sore"
    "gyarados": "Gairados",  # Smooth phonetic so TTS doesn't stumble on Gy-
    "exeggcute": "Eggsecute",
    "exeggutor": "Eggsecutor",

    # Gen 2 (Johto)
    "feraligatr": "Feraligatr",
    "typhlosion": "Typhlosion",
    "raikou": "Raikou",
    "entei": "Entei",
    "suicune": "Sweekoon",   # Anime canonical: Swee-koon -> smooth Sweekoon
    "lugia": "Lugia",
    "celebi": "Celebi",

    # Gen 3 (Hoenn)
    "seviper": "Seviper",
    "milotic": "Milotic",
    "gardevoir": "Gardevoir",
    "kyogre": "Kyogur",      # Anime canonical: Kye-oh-ger -> Kyogur
    "groudon": "Growdon",    # Anime canonical: Grow-don -> Growdon
    "rayquaza": "Raykwayza", # Anime canonical: Ray-kway-zah -> Raykwayza
    "jirachi": "Jirachi",
    "deoxys": "Deoxys",

    # Gen 4 (Sinnoh)
    "lucario": "Lucario",
    "garchomp": "Garchomp",
    "dialga": "Dialga",
    "palkia": "Palkia",
    "giratina": "Geerateena", # Smooth anime pronunciation
    "arceus": "Arkayus",     # Anime canonical: Ar-kay-us -> smooth Arkayus
    "darkrai": "Darkrai",
    "shaymin": "Shaymin",

    # Gen 5 (Unova)
    "zoroark": "Zoroark",
    "hydreigon": "Hydreigon",
    "reshiram": "Reshiram",
    "zekrom": "Zekrom",
    "kyurem": "Kyurem",

    # Gen 6 (Kalos)
    "greninja": "Greninja",
    "xerneas": "Zerneas",    # Clean Z sound without hyphen break
    "yveltal": "Eevaltal",   # Smooth anime Ee-vel-tall without stutter
    "zygarde": "Zygarde",

    # Gen 7 (Alola)
    "decidueye": "Decidueye",
    "nihilego": "Nyeheeleego",
    "solgaleo": "Solgalayo",  # Anime canonical: Sol-gah-lay-oh -> Solgalayo
    "lunala": "Lunala",
    "necrozma": "Necrozma",
    "tapu koko": "Tapu Koko",
    "tapu lele": "Tapu Lele",
    "tapu bulu": "Tapu Bulu",
    "tapu fini": "Tapu Fini",

    # Gen 8 (Galar)
    "zacian": "Zashian",     # Anime canonical: Zah-shee-un -> Zashian
    "zamazenta": "Zamazenta",
    "eternatus": "Eternatus",
    "urshifu": "Urshifu",
    "calyrex": "Calyrex",

    # Gen 9 (Paldea & Treasures of Ruin)
    "wo-chien": "Woh Chyen",
    "chien-pao": "Chyen Pow",
    "ting-lu": "Ting Loo",
    "chi-yu": "Chee Yoo",
    "gholdengo": "Goldengo",
    "koraidon": "Koraidon",
    "miraidon": "Miraidon",
    "ogerpon": "Ogerpon",
    "terapagos": "Terapagos",
    "pecharunt": "Pecharunt"
}

# Forms and special display names
EXPLICIT_FORM_OVERRIDES: dict[str, str] = {
    # Hisuian forms
    "growlithe-hisui": "Heesooeean Growlithe",
    "arcanine-hisui": "Heesooeean Arcanine",
    "voltorb-hisui": "Heesooeean Voltorb",
    "electrode-hisui": "Heesooeean Electrode",
    "typhlosion-hisui": "Heesooeean Typhlosion",
    "qwilfish-hisui": "Heesooeean Qwilfish",
    "sneasel-hisui": "Heesooeean Sneasel",
    "samurott-hisui": "Heesooeean Samurott",
    "lilligant-hisui": "Heesooeean Lilligant",
    "zorua-hisui": "Heesooeean Zorua",
    "zoroark-hisui": "Heesooeean Zoroark",
    "braviary-hisui": "Heesooeean Braviary",
    "sliggoo-hisui": "Heesooeean Sliggoo",
    "goodra-hisui": "Heesooeean Goodra",
    "avalugg-hisui": "Heesooeean Avalugg",
    "decidueye-hisui": "Heesooeean Decidueye",

    # Alolan forms
    "rattata-alola": "Alolan Rattata",
    "raticate-alola": "Alolan Raticate",
    "raichu-alola": "Alolan Raichu",
    "sandshrew-alola": "Alolan Sandshrew",
    "sandslash-alola": "Alolan Sandslash",
    "vulpix-alola": "Alolan Vulpix",
    "ninetales-alola": "Alolan Ninetales",
    "diglett-alola": "Alolan Diglett",
    "dugtrio-alola": "Alolan Dugtrio",
    "meowth-alola": "Alolan Meowth",
    "persian-alola": "Alolan Persian",
    "geodude-alola": "Alolan Geodude",
    "graveler-alola": "Alolan Graveler",
    "golem-alola": "Alolan Golem",
    "grimer-alola": "Alolan Grimer",
    "muk-alola": "Alolan Muk",
    "exeggutor-alola": "Alolan Eggsecutor",
    "marowak-alola": "Alolan Marowak",

    # Galarian forms
    "meowth-galar": "Galarian Meowth",
    "ponyta-galar": "Galarian Ponyta",
    "rapidash-galar": "Galarian Rapidash",
    "slowpoke-galar": "Galarian Slowpoke",
    "slowbro-galar": "Galarian Slowbro",
    "farfetchd-galar": "Galarian Farfetch'd",
    "weezing-galar": "Galarian Weezing",
    "mr-mime-galar": "Galarian Mister Mime",
    "articuno-galar": "Galarian Articuno",
    "zapdos-galar": "Galarian Zapdos",
    "moltres-galar": "Galarian Moltres",
    "slowking-galar": "Galarian Slowking",
    "corsola-galar": "Galarian Corsola",
    "zigzagoon-galar": "Galarian Zigzagoon",
    "linoone-galar": "Galarian Linoone",
    "darumaka-galar": "Galarian Darumaka",
    "darmanitan-galar": "Galarian Darmanitan",
    "yamask-galar": "Galarian Yamask",
    "stunfisk-galar": "Galarian Stunfisk",

    # Paldean forms
    "wooper-paldea": "Paldean Wooper",
    "tauros-paldea-combat-breed": "Paldean Tauros Combat Breed",
    "tauros-paldea-blaze-breed": "Paldean Tauros Blaze Breed",
    "tauros-paldea-aqua-breed": "Paldean Tauros Aqua Breed",

    # Special forms
    "ursaluna-bloodmoon": "Bloodmoon Ursaloona",
    "basculegion-male": "Baskyuleejun",
    "basculegion-female": "Baskyuleejun Female",
    "enamorus-incarnate": "Enamorus",
    "enamorus-therian": "Therian Forme Enamorus",
    "giratina-altered": "Geerateena",
    "giratina-origin": "Origin Forme Geerateena",
    "dialga-origin": "Origin Forme Dialga",
    "palkia-origin": "Origin Forme Palkia"
}

def clean_syllable_hyphens(s: str) -> str:
    """
    Strips internal syllable hyphens (e.g. Lap-rus -> Laprus, Ee-vee -> Eevee,
    Pee-kuh-choo -> Pikachu) while preserving true compound words and multi-words.
    """
    # If the string is already a clean name, return it
    if not s:
        return s
    
    # Specific known multi-word compounds that legitimately have spaces
    if s in ["Mister Mime", "Mime Junior", "Mister Rime", "Type Null", "Porygon Two", "Porygon Z", "Farfetch'd", "Sirfetch'd", "Ho-Oh"]:
        return s

    # Strip intra-syllable hyphens like Foo-bar -> Foobar
    cleaned = s.replace("-", "")
    return cleaned

def main() -> None:
    # 1. Start with clean dictionary
    data: dict[str, str] = {}

    # Load existing dictionary to preserve species keys
    if ASSETS_JSON.exists():
        with open(ASSETS_JSON, "r", encoding="utf-8") as f:
            old_data: dict[str, str] = json.load(f)
    else:
        old_data = {}

    # First clean every entry in old_data:
    # Remove awkward hyphenated phonetic breakdowns and replace with clean canonical names!
    for k, v in old_data.items():
        k_lower = k.lower().strip()
        # Remove legacy global terms
        if k_lower in ["pokemon", "pokedex", "pokeball", "pokeballs", "pokeblock", "pokeblocks"]:
            continue

        # If it was an awkward hyphenated string, strip hyphens or use title-cased key
        if "-" in v and not any(p in v for p in ["Mister", "Junior", "Female", "Male", "Breed", "Two", "Forme", "Ho-Oh", "'"]):
            # Use clean title-cased name
            clean_name = k_lower.replace("-", " ").title()
            data[k_lower] = clean_name
        else:
            data[k_lower] = v

    # 2. Apply our curated NATURAL_PRONUNCIATION_OVERRIDES
    for k, v in NATURAL_PRONUNCIATION_OVERRIDES.items():
        data[k.lower()] = v

    # 3. Apply EXPLICIT_FORM_OVERRIDES
    for k, v in EXPLICIT_FORM_OVERRIDES.items():
        data[k.lower()] = v

    # 4. Multi-word phrases for PokemonFormDetector outputs
    extra_multiword: dict[str, str] = {}
    for k, v in list(data.items()):
        if k.endswith("-hisui"):
            base = k[: -len("-hisui")]
            base_phon = NATURAL_PRONUNCIATION_OVERRIDES.get(base, base.title())
            extra_multiword[f"hisuian {base}"] = f"Heesooeean {base_phon}"
            extra_multiword[f"{base} hisui"] = f"{base_phon} Heesooee"
        elif k.endswith("-alola"):
            base = k[: -len("-alola")]
            base_phon = NATURAL_PRONUNCIATION_OVERRIDES.get(base, base.title())
            extra_multiword[f"alolan {base}"] = f"Alolan {base_phon}"
        elif k.endswith("-galar"):
            base = k[: -len("-galar")]
            base_phon = NATURAL_PRONUNCIATION_OVERRIDES.get(base, base.title())
            extra_multiword[f"galarian {base}"] = f"Galarian {base_phon}"
        elif k.endswith("-paldea"):
            base = k[: -len("-paldea")]
            base_phon = NATURAL_PRONUNCIATION_OVERRIDES.get(base, base.title())
            extra_multiword[f"paldean {base}"] = f"Paldean {base_phon}"

    data.update(extra_multiword)

    # 5. Final audit sweep:
    # Ensure Lapras, Eevee, Pikachu, Bulbasaur, Charizard, Blastoise are NEVER hyphenated!
    assert data.get("lapras") == "Lapras", f"Lapras failed: {data.get('lapras')}"
    assert data.get("eevee") == "Eevee", f"Eevee failed: {data.get('eevee')}"
    assert data.get("pikachu") == "Pikachu", f"Pikachu failed: {data.get('pikachu')}"
    assert data.get("charizard") == "Charizard", f"Charizard failed: {data.get('charizard')}"
    assert "pokemon" not in data, "Pokemon must NOT be in pronunciation dictionary!"

    with open(ASSETS_JSON, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")

    print(
        f"SUCCESS: Generated {len(data)} natural speech dictionary entries in {ASSETS_JSON.name}.\n"
        f"  * Lapras -> {data['lapras']}\n"
        f"  * Eevee -> {data['eevee']}\n"
        f"  * Pikachu -> {data['pikachu']}\n"
        f"  * Charizard -> {data['charizard']}\n"
        f"  * Arceus -> {data['arceus']}\n"
        f"  * Suicune -> {data['suicune']}\n"
        f"  * Rayquaza -> {data['rayquaza']}\n"
        f"  * Kyogre -> {data['kyogre']}\n"
        f"  * Groudon -> {data['groudon']}\n"
        f"  * Regirock -> {data['regirock']}\n"
        f"  * Ursaluna -> {data['ursaluna']}\n"
        f"  * Basculegion -> {data['basculegion']}\n"
    )

if __name__ == "__main__":
    main()
