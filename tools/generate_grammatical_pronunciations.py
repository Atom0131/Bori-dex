import json

path = r'C:\Users\pagan\OneDrive\Documents\Code\Pokedex\PokedexApp\app\src\main\assets\pokemon_pronunciations.json'
with open(path, 'r', encoding='utf-8') as f:
    d = json.load(f)

# Acoustic single-word phonetic respellings for English neural TTS:
# Grammatically correct: First letter capitalized, lowercase letters after.
# Never split into separate space-delimited words unless it's a multi-word Pokemon (like Mr. Mime, Iron Treads, Tapu Koko).

special_acoustic_respellings = {
    # Gen 1
    "venusaur": "Venasaur",       # Pronounced natively by Google US TTS as [ˈviːnəsɔːr]
    "ivysaur": "Ivysaur",         # Pronounced [ˈaɪviːsɔːr]
    "bulbasaur": "Bulbasaur",     # Pronounced [ˈbʊlbəsɔːr]
    "charmeleon": "Charmeleon",
    "charizard": "Charizard",
    "squirtle": "Squirtle",
    "wartortle": "Wartortle",
    "blastoise": "Blastoyce",
    "pidgeotto": "Pidgeotto",
    "pidgeot": "Pidgeot",
    "rattata": "Rattatah",
    "raticate": "Raticate",
    "spearow": "Spearow",
    "arbok": "Arbok",
    "pikachu": "Pikachu",
    "raichu": "Raichu",
    "ninetales": "Ninetales",
    "growlithe": "Growlith",
    "arcanine": "Arcanine",
    "poliwag": "Poliwag",
    "poliwhirl": "Poliwhirl",
    "poliwrath": "Poliwrath",
    "alakazam": "Alakazam",
    "machamp": "Machamp",
    "bellsprout": "Bellsprout",
    "geodude": "Geodude",
    "ponyta": "Ponyta",
    "rapidash": "Rapidash",
    "slowpoke": "Slowpoke",
    "magneton": "Magneton",
    "farfetchd": "Farfetch'd",
    "exeggcute": "Exeggcute",
    "exeggutor": "Exeggutor",
    "cubone": "Cubone",
    "marowak": "Marowak",
    "lickitung": "Lickitung",
    "koffing": "Koffing",
    "weezing": "Weezing",
    "rhyhorn": "Rhyhorn",
    "rhydon": "Rhydon",
    "kangaskhan": "Kangaskhan",
    "scyther": "Scyther",
    "electabuzz": "Electabuzz",
    "magmar": "Magmar",
    "pinsir": "Pinsir",
    "tauros": "Tauros",
    "magikarp": "Magikarp",
    "gyarados": "Gairadose",     # Eliminates "Gee-ar-a-dos" mispronunciation
    "lapras": "Lapras",
    "ditto": "Ditto",
    "eevee": "Eevee",
    "vaporeon": "Vaporeon",
    "jolteon": "Jolteon",
    "flareon": "Flareon",
    "porygon": "Porygon",
    "omanyte": "Omanyte",
    "omastar": "Omastar",
    "kabuto": "Kabuto",
    "kabutops": "Kabutops",
    "aerodactyl": "Aerodactyl",
    "snorlax": "Snorlax",
    "articuno": "Articuno",
    "zapdos": "Zapdos",
    "moltres": "Moltrace",       # Guarantees -trace rather than -tress
    "dratini": "Dratini",
    "dragonair": "Dragonair",
    "dragonite": "Dragonite",
    "mewtwo": "Mewtwo",
    "mew": "Mew",

    # Later Gens & Legendaries
    "chikorita": "Chikorita",
    "cyndaquil": "Cyndaquil",
    "totodile": "Totodile",
    "feraligatr": "Feraligator",
    "togepi": "Togepi",
    "sudowoodo": "Sudowoodo",
    "ampharos": "Ampharos",
    "espeon": "Espeon",
    "umbreon": "Umbreon",
    "raikou": "Ryeko",           # Guarantees Rye-ko rather than Ray-ko
    "entei": "Entei",
    "suicune": "Sweekoon",       # Eliminates "Soo-ee-soon"
    "tyranitar": "Tyranitar",
    "lugia": "Lugia",
    "ho-oh": "Ho-Oh",
    "celebi": "Celebi",
    "gardevoir": "Gardevoir",
    "milotic": "Milotic",
    "salamence": "Salamence",
    "metagross": "Metagross",
    "kyogre": "Kyogur",          # Eliminates "Kye-og-ree", matches anime "Kye-oh-gur"
    "groudon": "Growdon",        # Matches anime "Grow-don"
    "rayquaza": "Raykwayza",     # Anime/Official [reɪˈkweɪzə], eliminates "Ray-kwah-zah"
    "deoxys": "Deeoxiss",        # Matches anime "Dee-ox-iss"
    "lucario": "Lucario",
    "garchomp": "Garchomp",
    "dialga": "Dialga",
    "palkia": "Palkia",
    "giratina": "Giratina",
    "darkrai": "Darkrai",
    "arceus": "Arkayus",         # Prevents "Ar-see-us"
    "greninja": "Greninja",
    "xerneas": "Zerneas",
    "yveltal": "Eeveltal",
    "zygarde": "Zygarde",
    "decidueye": "Decidueye",
    "incineroar": "Incineroar",
    "mimikyu": "Mimikyu",
    "zacian": "Zahshee-an",
    "zamazenta": "Zamazenta",
    "eternatus": "Eternatus",
    "koraidon": "Koraidon",
    "miraidon": "Miraidon",
    "ogerpon": "Ogerpon",
    "terapagos": "Terapagos",
    "pecharunt": "Pecharunt",
    "archaludon": "Archaludon",
    "ceruledge": "Ceruledge",
    "armarouge": "Armarouge",
    "tinkaton": "Tinkaton",
    "baxcalibur": "Baxcalibur"
}

# Transform all entries in dictionary
new_dict = {}
for k, v in d.items():
    if k in special_acoustic_respellings:
        new_dict[k] = special_acoustic_respellings[k]
    else:
        # Default grammatical formatting: Capitalize first letter, lowercase after
        # Clean hyphens into spaces for multi-word pokemon
        words = k.replace('-', ' ').split()
        capitalized = ' '.join(w.capitalize() for w in words)
        new_dict[k] = capitalized

with open(path, 'w', encoding='utf-8') as f:
    json.dump(new_dict, f, indent=2)

print(f"Generated {len(new_dict)} single-word grammatically formatted entries!")
print(f"  venusaur -> {new_dict['venusaur']}")
print(f"  rayquaza -> {new_dict['rayquaza']}")
print(f"  suicune -> {new_dict['suicune']}")
print(f"  arceus -> {new_dict['arceus']}")
print(f"  gyarados -> {new_dict['gyarados']}")
