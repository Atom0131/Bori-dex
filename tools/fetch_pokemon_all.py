import urllib.request
import json
import os
import concurrent.futures

out_dir = r"C:\Users\pagan\OneDrive\Documents\Code\Pokedex\PokedexApp\app\src\main\assets"
os.makedirs(out_dir, exist_ok=True)
out_file = os.path.join(out_dir, "pokemon_data.json")

def fetch_pokemon(p):
    url = p['url']
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        r = json.loads(urllib.request.urlopen(req).read())
        
        id = r['id']
        name = r['name']
        types = '/'.join([t['type']['name'].capitalize() for t in r['types']])
        height = f"{r['height'] / 10.0} m"
        weight = f"{r['weight'] / 10.0} kg"
        
        sprites = r.get('sprites', {})
        sprite_url = sprites.get('front_default') or ""
        shiny_url = sprites.get('front_shiny') or ""
        
        # species flavor text
        flavor_text = "No description available."
        try:
            species_url = r['species']['url']
            s_req = urllib.request.Request(species_url, headers={'User-Agent': 'Mozilla/5.0'})
            s_r = json.loads(urllib.request.urlopen(s_req).read())
            for entry in s_r['flavor_text_entries']:
                if entry['language']['name'] == 'en':
                    flavor_text = entry['flavor_text'].replace('\n', ' ').replace('\f', ' ').replace('\r', ' ')
                    break
        except Exception:
            pass
            
        return {
            "id": id,
            "name": name,
            "type": types,
            "height": height,
            "weight": weight,
            "flavorText": flavor_text,
            "spriteUrl": sprite_url,
            "shinySpriteUrl": shiny_url
        }
    except Exception as e:
        print(f"Error fetching {url}: {e}")
        return None

if __name__ == "__main__":
    print("Fetching pokemon list...")
    req = urllib.request.Request("https://pokeapi.co/api/v2/pokemon?limit=1500", headers={'User-Agent': 'Mozilla/5.0'})
    res = json.loads(urllib.request.urlopen(req).read())
    results = res['results']
    
    print(f"Found {len(results)} pokemon. Fetching details...")
    
    pokemon_data = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=20) as executor:
        futures = [executor.submit(fetch_pokemon, p) for p in results]
        for i, future in enumerate(concurrent.futures.as_completed(futures)):
            data = future.result()
            if data:
                pokemon_data.append(data)
            if (i + 1) % 100 == 0:
                print(f"Fetched {i + 1}/{len(results)}")

    pokemon_data.sort(key=lambda x: x['id'])
    
    with open(out_file, 'w', encoding='utf-8') as f:
        json.dump(pokemon_data, f, ensure_ascii=False, indent=2)
        
    print(f"Done! Wrote {len(pokemon_data)} records to pokemon_data.json")
