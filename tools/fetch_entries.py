import urllib.request
import json
import re

# Load existing JSON
json_path = 'app/src/main/assets/pokemon_data.json'
with open(json_path, 'r', encoding='utf-8') as f:
    pokemon_list = json.load(f)

# Query GraphQL
print("Fetching GraphQL...")
query = """
query {
  pokemon_v2_pokemonspecies {
    name
    pokemon_v2_pokemonspeciesflavortexts(where: {language_id: {_eq: 9}}) {
      flavor_text
      pokemon_v2_version {
        name
      }
    }
  }
}
"""
req = urllib.request.Request(
    'https://beta.pokeapi.co/graphql/v1beta',
    json.dumps({'query': query}).encode('utf-8'),
    {'Content-Type': 'application/json', 'User-Agent': 'Mozilla'}
)
res = urllib.request.urlopen(req)
data = json.loads(res.read())

print("Parsing GraphQL...")
species_entries = {}
for species in data['data']['pokemon_v2_pokemonspecies']:
    species_name = species['name']
    entries_map = {}
    for entry in species['pokemon_v2_pokemonspeciesflavortexts']:
        version_name = entry['pokemon_v2_version']['name'].replace('-', ' ').title()
        flavor_text = entry['flavor_text'].replace('\n', ' ').replace('\f', ' ')
        entries_map[version_name] = flavor_text
    species_entries[species_name] = entries_map

# Merge into pokemon_list
print("Merging data...")
for pk in pokemon_list:
    # Handle megas/forms by taking the base species name
    # e.g. "charizard-mega-x" -> "charizard"
    # PokeAPI species name doesn't have the form suffixes
    base_name = pk['name'].split('-')[0]
    # Special cases like mr-mime
    if pk['name'].startswith('mr-rime'):
        base_name = 'mr-rime'
    elif pk['name'].startswith('mr-mime'):
        base_name = 'mr-mime'
    elif pk['name'].startswith('mime-jr'):
        base_name = 'mime-jr'
    elif pk['name'].startswith('ho-oh'):
        base_name = 'ho-oh'
    elif pk['name'].startswith('porygon-z'):
        base_name = 'porygon-z'
    elif pk['name'].startswith('jangmo-o'):
        base_name = 'jangmo-o'
    elif pk['name'].startswith('hakamo-o'):
        base_name = 'hakamo-o'
    elif pk['name'].startswith('kommo-o'):
        base_name = 'kommo-o'
    elif pk['name'].startswith('tapu-'):
        base_name = pk['name']
    elif pk['name'].startswith('type-null'):
        base_name = 'type-null'
    elif pk['name'].startswith('nidoran'):
        base_name = pk['name']
    elif pk['name'].startswith('ho-oh'):
        base_name = pk['name']
    elif pk['name'].startswith('wo-chien'):
        base_name = pk['name']
    elif pk['name'].startswith('chien-pao'):
        base_name = pk['name']
    elif pk['name'].startswith('ting-lu'):
        base_name = pk['name']
    elif pk['name'].startswith('chi-yu'):
        base_name = pk['name']
    elif pk['name'].startswith('roaring-moon'):
        base_name = pk['name']
    elif pk['name'].startswith('iron-'):
        base_name = pk['name'].split('-')[0] + '-' + pk['name'].split('-')[1] # e.g. iron-valiant
    elif pk['name'].startswith('scream-tail') or pk['name'].startswith('brute-bonnet') or pk['name'].startswith('flutter-mane') or pk['name'].startswith('slither-wing') or pk['name'].startswith('sandy-shocks'):
        base_name = pk['name']
    elif pk['name'].startswith('great-tusk'):
        base_name = pk['name']
    
    # Try exact match first
    if pk['name'] in species_entries:
        pk['pokedexEntries'] = species_entries[pk['name']]
    elif base_name in species_entries:
        pk['pokedexEntries'] = species_entries[base_name]
    else:
        # Fallback to the single flavor text
        pk['pokedexEntries'] = {"Default": pk.get('flavorText', '')}

# Save back
with open(json_path, 'w', encoding='utf-8') as f:
    json.dump(pokemon_list, f, indent=2)

print("Done!")
