import json

with open("app/src/main/assets/pokemon_data.json", "r", encoding="utf-8") as f:
    pokemon_data = json.load(f)

with open("evo_map.json", "r", encoding="utf-8") as f:
    evo_map = json.load(f)

# Helper to find forms
base_names = [p['name'] for p in pokemon_data if '-' not in p['name'] or p['name'] in ['ho-oh', 'porygon-z', 'jangmo-o', 'hakamo-o', 'kommo-o', 'tapu-koko', 'tapu-lele', 'tapu-bulu', 'tapu-fini', 'chi-yu', 'chien-pao', 'ting-lu', 'wo-chien']]

for p in pokemon_data:
    name = p["name"]
    # Evolutions
    if name in evo_map:
        p["evolvesTo"] = evo_map[name]
    else:
        p["evolvesTo"] = []
    
    # Forms
    p["forms"] = []
    if name in base_names:
        forms = [x['name'] for x in pokemon_data if x['name'] != name and x['name'].startswith(name + "-")]
        if forms:
            p["forms"] = forms

with open("app/src/main/assets/pokemon_data.json", "w", encoding="utf-8") as f:
    json.dump(pokemon_data, f, indent=2)

print("Updated pokemon_data.json")
