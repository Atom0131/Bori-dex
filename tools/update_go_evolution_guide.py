import json

path = r'app/src/main/assets/evolution_guide.json'
with open(path, 'r', encoding='utf-8') as f:
    guide = json.load(f)

trade_evos = {'64': '65', '67': '68', '75': '76', '93': '94', '525': '526', '533': '534', '589': '590', '616': '617', '708': '709', '710': '711'}
sinnoh_evos = {'190', '193', '108', '114', '125', '126', '137', '207', '215', '176', '198', '200', '315', '112', '356'}
unova_evos = {'608', '572', '603', '511', '513', '515'}

updated = 0
for pid, pdata in guide.items():
    evos = pdata.get('evolutions', [])
    for evo in evos:
        tid = str(evo.get('targetId', ''))
        vn = evo.setdefault('versionNotes', {})
        if 'GO' not in vn:
            if pid in trade_evos:
                vn['GO'] = {'method': '100 Candy (or 0 Candy via Trade)', 'item': 'pokemongo.png', 'note': 'Trading this Pokémon waives the 100 Candy requirement.'}
            elif pid in sinnoh_evos:
                vn['GO'] = {'method': '100 Candy + Sinnoh Stone', 'item': 'sinnoh-stone.png', 'note': 'Earn Sinnoh Stones from Team Leader training or PvP battles.'}
            elif pid in unova_evos:
                vn['GO'] = {'method': '100 Candy + Unova Stone', 'item': 'unova-stone.png', 'note': 'Earn Unova Stones from defeating Team GO Rocket Leaders.'}
            elif pid == '349':
                vn['GO'] = {'method': 'Walk 20km as buddy + 100 Candy', 'item': 'pokemongo.png', 'note': 'Set Feebas as active buddy and walk 20km.'}
            elif pid == '129':
                vn['GO'] = {'method': '400 Magikarp Candy', 'item': 'pokemongo.png', 'note': 'Catch and walk Magikarp to accumulate 400 Candy.'}
            elif pid == '133':
                vn['GO'] = {'method': '25 Candy + Buddy / Lure Tasks', 'item': 'pokemongo.png', 'note': 'Use name tricks or 10km buddy walks (day/night), Lures, or 70 Buddy Hearts.'}
            elif pid == '686':
                vn['GO'] = {'method': '50 Candy + Invert Phone', 'item': 'pokemongo.png', 'note': 'Physically rotate your phone upside down to evolve Inkay.'}
            else:
                default_method = evo.get('defaultMethod', '')
                item_val = evo.get('defaultItem')
                candy_amt = '50 Candy'
                if any(x in default_method for x in ['Level 1', 'Level 2']):
                    candy_amt = '25 Candy'
                elif any(x in default_method for x in ['Level 3', 'Level 4', 'Level 5', 'Level 6']):
                    candy_amt = '100 Candy'
                elif item_val:
                    clean_item = item_val.replace('.png', '').replace('-', ' ').title()
                    candy_amt = f'50 Candy + {clean_item}'
                vn['GO'] = {'method': candy_amt, 'item': item_val or 'pokemongo.png', 'note': 'Standard Pokémon GO species Candy evolution.'}
            updated += 1

with open(path, 'w', encoding='utf-8') as f:
    json.dump(guide, f, ensure_ascii=False, indent=2)

print(f'Added Pokemon GO evolution notes to {updated} evolution links in evolution_guide.json')
