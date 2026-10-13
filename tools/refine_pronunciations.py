import json
import re

path = r'C:\Users\pagan\OneDrive\Documents\Code\Pokedex\PokedexApp\app\src\main\assets\pokemon_pronunciations.json'
with open(path, 'r', encoding='utf-8') as f:
    d = json.load(f)

cleaned_map = {}
for k, v in d.items():
    clean_v = re.sub(r'(?i)\bsore\b', 'sawr', v)
    clean_v = re.sub(r'(?i)\bsohr\b', 'sawr', clean_v)
    tokens = [p.capitalize() for w in clean_v.split() for p in w.split('-') if p]
    cleaned_map[k] = ' '.join(tokens)

# Anime Pokédex Audio / Pokerap Verified Refinements
cleaned_map['venusaur'] = 'Vee nuh sawr'
cleaned_map['ivysaur'] = 'Eye vee sawr'
cleaned_map['bulbasaur'] = 'Bulb uh sawr'
cleaned_map['rayquaza'] = 'Ray kway zuh'
cleaned_map['suicune'] = 'Swee koon'
cleaned_map['arceus'] = 'Ar kee us'
cleaned_map['gyarados'] = 'Gair ih dose'
cleaned_map['rattata'] = 'Ruh ta ta'
cleaned_map['pidgeot'] = 'Pid jit'
cleaned_map['pikachu'] = 'Peek uh chew'
cleaned_map['blastoise'] = 'Blast oyce'
cleaned_map['charizard'] = 'Char ih zard'

with open(path, 'w', encoding='utf-8') as f:
    json.dump(cleaned_map, f, indent=2)

print(f"Successfully refined {len(cleaned_map)} pronunciations!")
for test in ['venusaur', 'rayquaza', 'bulbasaur', 'suicune', 'arceus']:
    print(f"  {test} -> {cleaned_map[test]}")
