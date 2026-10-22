r"""
Pronunciation Integrity Audit for all 1,025 Pokemon (Natural Anime Speech)

Verifies:
1. 100% of National Dex Pokemon 1 to 1025 have active pronunciations.
2. Standard Pokemon (Pikachu, Charizard, Bulbasaur, Squirtle, Eevee, Lunala) are un-hyphenated single words.
3. Irregular Pokemon (Solgaleo, Rayquaza, Suicune, Arceus, Gyarados, Kyogre) have un-hyphenated acoustic respellings.
4. Multi-word Pokemon (Tapu Koko, Mr. Mime, Iron Treads) are properly spaced title-case words.
"""

import os
import sys
import json

if sys.stdout and hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

BASE = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(BASE, "..", ".."))

MASTER_JSON = os.path.join(PROJECT_ROOT, "PokedexApp", "app", "src", "main", "assets", "pokemon_data.json")
PRONUNCIATION_JSON = os.path.join(PROJECT_ROOT, "PokedexApp", "app", "src", "main", "assets", "pokemon_pronunciations.json")

def run_audit():
    print("=================================================================")
    print("  AUDITING NATURAL ANIME PRONUNCIATION INTEGRITY (1-1025)")
    print("=================================================================")

    with open(MASTER_JSON, 'r', encoding='utf-8') as f:
        pk_data = json.load(f)

    with open(PRONUNCIATION_JSON, 'r', encoding='utf-8') as f:
        pron_data = json.load(f)

    base_pokemon = [p for p in pk_data if p.get('id') <= 1025]
    forms = [p for p in pk_data if p.get('id') > 1025]

    missing_base = []
    for p in base_pokemon:
        name = p['name'].lower().strip()
        val = pron_data.get(name) or pron_data.get(name.replace('-', ' '))
        if not val:
            missing_base.append((p['id'], name))

    print(f"\n1. Base Pokemon Coverage (1 to 1025):")
    print(f"   Total Base Species: {len(base_pokemon)}")
    print(f"   Missing Mappings:   {len(missing_base)}")
    if missing_base:
        print(f"   FAILED MISSING: {missing_base}")
    else:
        print(f"   [OK] 100% of all 1,025 base Pokemon have active pronunciations!")

    # Check 1: Standard Pokemon MUST be clean single words (NO hyphens)
    print(f"\n2. Standard Pokemon Natural Fluidity Checks (Zero Hyphens):")
    standard_checks = [
        ("pikachu", "Pikachu"),
        ("charizard", "Charizard"),
        ("bulbasaur", "Bulbasaur"),
        ("squirtle", "Squirtle"),
        ("eevee", "Eevee"),
        ("lucario", "Lucario"),
        ("gengar", "Gengar"),
        ("snorlax", "Snorlax"),
        ("mewtwo", "Mewtwo"),
        ("lunala", "Lunala")
    ]
    standard_pass = True
    for name, expected in standard_checks:
        actual = pron_data.get(name)
        passed = (actual == expected)
        status = "PASSED" if passed else "FAILED"
        if not passed:
            standard_pass = False
        print(f"   [{status:6s}] {name:<14} -> '{actual}' (expected: '{expected}')")

    # Check 2: Irregular Pokemon MUST have un-hyphenated phonetic respellings
    print(f"\n3. Irregular Pokemon Acoustic Respellings:")
    irregular_checks = [
        ("solgaleo", "Solgalayo"),
        ("rayquaza", "Raykwayza"),
        ("suicune", "Sweekoon"),
        ("arceus", "Arkayus"),
        ("gyarados", "Gairadose"),
        ("kyogre", "Kyogur"),
        ("groudon", "Growdon"),
        ("raikou", "Ryeko"),
        ("moltres", "Moltrace"),
        ("venusaur", "Venasaur"),
        ("blastoise", "Blastoyce"),
        ("xerneas", "Zerneas"),
        ("yveltal", "Eeveltal"),
        ("deoxys", "Deeoxiss")
    ]
    irregular_pass = True
    for name, expected in irregular_checks:
        actual = pron_data.get(name)
        passed = (actual == expected)
        status = "PASSED" if passed else "FAILED"
        if not passed:
            irregular_pass = False
        print(f"   [{status:6s}] {name:<14} -> '{actual}' (expected: '{expected}')")

    # Check 3: Multi-word & Special Characters
    print(f"\n4. Multi-Word & Special Cases:")
    multi_checks = [
        ("tapu koko", "Tahpoo Koko"),
        ("mr. mime", "Mister Mime"),
        ("iron treads", "Iron Treads"),
        ("ho-oh", "Ho Oh")
    ]
    multi_pass = True
    for name, expected in multi_checks:
        actual = pron_data.get(name)
        passed = (actual == expected)
        status = "PASSED" if passed else "FAILED"
        if not passed:
            multi_pass = False
        print(f"   [{status:6s}] {name:<14} -> '{actual}' (expected: '{expected}')")

    # Overall Verdict
    all_ok = (not missing_base) and standard_pass and irregular_pass and multi_pass
    print("\n" + "=" * 65)
    if all_ok:
        print("  ALL 1,025 NATURAL ANIME PRONUNCIATION AUDITS PASSED PERFECTLY!")
        print("=" * 65)
        return True
    else:
        print("  AUDIT FAILED - PLEASE REVIEW LOGS")
        print("=" * 65)
        return False

if __name__ == "__main__":
    success = run_audit()
    sys.exit(0 if success else 1)
