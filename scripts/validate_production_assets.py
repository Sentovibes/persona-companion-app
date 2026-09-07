#!/usr/bin/env python3
"""
Standalone Programmatic Validation Script for Persona Companion App Assets
Validates 100% description coverage, JSON syntax, schema compliance,
and confirms 0 placeholders across all 14 production JSON files (7 items, 7 skills).
"""

import sys
import os
import json
import re

# Resolve base project directory
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, ".."))

ITEMS_DIR = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "data", "items")
SKILLS_DIR = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "data", "skills")

GAMES = ["p3fes", "p3p", "p3r", "p4", "p4g", "p5", "p5r"]

# Regex patterns for invalid descriptions and placeholders
HEX_OFFSET_RE = re.compile(r"^[0-9A-Fa-f]{1,4}$")
ITEM_PLACEHOLDER_RE = re.compile(r"Item_[0-9A-Fa-f]+", re.IGNORECASE)
SKILL_PLACEHOLDER_RE = re.compile(r"Skill_?[0-9A-Fa-f]+", re.IGNORECASE)
EXACT_PLACEHOLDERS = {
    "none", "-", "?", "unknown", "todo", "tbd", "n/a",
    "blank", "true blank skill"
}

def is_placeholder(desc: str, name: str, is_skill: bool = False) -> tuple[bool, str]:
    cleaned = desc.strip()
    cleaned_lower = cleaned.lower()

    if not cleaned:
        return True, "EMPTY_DESCRIPTION"

    if cleaned_lower in EXACT_PLACEHOLDERS:
        return True, f"EXACT_PLACEHOLDER('{cleaned}')"

    if HEX_OFFSET_RE.fullmatch(cleaned):
        return True, f"HEX_OFFSET('{cleaned}')"

    if cleaned.isdigit():
        return True, f"NUMERIC_STRING('{cleaned}')"

    if is_skill:
        if SKILL_PLACEHOLDER_RE.search(cleaned):
            return True, f"SKILL_PLACEHOLDER('{cleaned}')"
        if cleaned_lower == name.strip().lower():
            return True, f"TAUTOLOGY('{cleaned}')"
    else:
        if ITEM_PLACEHOLDER_RE.search(cleaned):
            return True, f"ITEM_PLACEHOLDER('{cleaned}')"

    return False, ""

def validate_items(game: str) -> dict:
    filename = f"{game}_items.json"
    filepath = os.path.join(ITEMS_DIR, filename)

    result = {
        "file": filename,
        "path": filepath,
        "exists": False,
        "valid_json": False,
        "count": 0,
        "with_desc": 0,
        "empty_desc": 0,
        "placeholders": [],
        "schema_errors": [],
        "file_size": 0
    }

    if not os.path.exists(filepath):
        return result

    result["exists"] = True
    result["file_size"] = os.path.getsize(filepath)

    try:
        with open(filepath, "r", encoding="utf-8") as f:
            data = json.load(f)
        result["valid_json"] = True
    except Exception as e:
        result["schema_errors"].append(f"JSON Parse Error: {e}")
        return result

    if not isinstance(data, dict) or "items" not in data or not isinstance(data["items"], list):
        result["schema_errors"].append("Root JSON must be an object with an 'items' array")
        return result

    items = data["items"]
    result["count"] = len(items)

    for idx, item in enumerate(items):
        if not isinstance(item, dict):
            result["schema_errors"].append(f"Item #{idx} is not an object")
            continue

        name = item.get("name", "")
        if not name or not isinstance(name, str) or not name.strip():
            result["schema_errors"].append(f"Item #{idx} missing valid 'name'")

        category = item.get("category", "")
        if not category or not isinstance(category, str) or not category.strip():
            result["schema_errors"].append(f"Item '{name}' (#{idx}) missing valid 'category'")

        if "description" not in item:
            result["empty_desc"] += 1
            result["schema_errors"].append(f"Item '{name}' missing 'description' key")
            continue

        desc = item["description"]
        if desc is None or not isinstance(desc, str) or not desc.strip():
            result["empty_desc"] += 1
            continue

        placeholder, reason = is_placeholder(desc, name, is_skill=False)
        if placeholder:
            result["placeholders"].append((name, desc, reason))
        else:
            result["with_desc"] += 1

    return result

def validate_skills(game: str) -> dict:
    filename = f"{game}_skills.json"
    filepath = os.path.join(SKILLS_DIR, filename)

    result = {
        "file": filename,
        "path": filepath,
        "exists": False,
        "valid_json": False,
        "count": 0,
        "with_desc": 0,
        "empty_desc": 0,
        "placeholders": [],
        "schema_errors": [],
        "file_size": 0
    }

    if not os.path.exists(filepath):
        return result

    result["exists"] = True
    result["file_size"] = os.path.getsize(filepath)

    try:
        with open(filepath, "r", encoding="utf-8") as f:
            data = json.load(f)
        result["valid_json"] = True
    except Exception as e:
        result["schema_errors"].append(f"JSON Parse Error: {e}")
        return result

    if not isinstance(data, dict):
        result["schema_errors"].append("Root JSON must be an object of skill IDs to skill entries")
        return result

    result["count"] = len(data)

    for sid, skill in data.items():
        if not isinstance(skill, dict):
            result["schema_errors"].append(f"Skill ID '{sid}' value is not an object")
            continue

        name = skill.get("name", "")
        if not name or not isinstance(name, str) or not name.strip():
            result["schema_errors"].append(f"Skill ID '{sid}' missing valid 'name'")

        # Schema checks for app compatibility
        element = skill.get("element", "")
        if not element or not isinstance(element, str):
            result["schema_errors"].append(f"Skill '{name}' (ID {sid}) missing 'element'")

        if "cost" not in skill or not isinstance(skill["cost"], int):
            result["schema_errors"].append(f"Skill '{name}' (ID {sid}) missing valid integer 'cost'")

        if "a" not in skill or not isinstance(skill["a"], list) or len(skill["a"]) < 3:
            result["schema_errors"].append(f"Skill '{name}' (ID {sid}) missing valid 'a' array")

        if "b" not in skill or not isinstance(skill["b"], list) or len(skill["b"]) < 8:
            result["schema_errors"].append(f"Skill '{name}' (ID {sid}) missing valid 'b' array")

        if "c" not in skill or not isinstance(skill["c"], list) or len(skill["c"]) < 1:
            result["schema_errors"].append(f"Skill '{name}' (ID {sid}) missing valid 'c' array")

        if "description" not in skill:
            result["empty_desc"] += 1
            result["schema_errors"].append(f"Skill '{name}' missing 'description' key")
            continue

        desc = skill["description"]
        if desc is None or not isinstance(desc, str) or not desc.strip():
            result["empty_desc"] += 1
            continue

        placeholder, reason = is_placeholder(desc, name, is_skill=True)
        if placeholder:
            result["placeholders"].append((name, desc, reason))
        else:
            result["with_desc"] += 1

    return result

def main():
    print("=" * 80)
    print("   PERSONA COMPANION APP -- PRODUCTION ASSETS VALIDATION SUITE")
    print("=" * 80)
    print(f"Target Items Directory : {ITEMS_DIR}")
    print(f"Target Skills Directory: {SKILLS_DIR}")
    print("=" * 80)

    total_files_checked = 0
    total_files_valid = 0
    total_items = 0
    total_items_with_desc = 0
    total_item_placeholders = 0
    total_item_schema_errors = 0

    total_skills = 0
    total_skills_with_desc = 0
    total_skill_placeholders = 0
    total_skill_schema_errors = 0

    all_passed = True

    print("\n[1/2] VALIDATING PRODUCTION ITEMS JSON FILES")
    print("-" * 80)
    print(f"{'Game':<8} | {'File':<18} | {'Size (B)':<10} | {'Count':<7} | {'With Desc':<10} | {'Coverage':<9} | {'Status'}")
    print("-" * 80)

    for game in GAMES:
        total_files_checked += 1
        res = validate_items(game)

        coverage = (res["with_desc"] / res["count"] * 100.0) if res["count"] > 0 else 0.0
        status = "PASS" if (res["valid_json"] and res["count"] > 0 and res["with_desc"] == res["count"] and len(res["placeholders"]) == 0 and len(res["schema_errors"]) == 0) else "FAIL"

        if status == "PASS":
            total_files_valid += 1
        else:
            all_passed = False

        total_items += res["count"]
        total_items_with_desc += res["with_desc"]
        total_item_placeholders += len(res["placeholders"])
        total_item_schema_errors += len(res["schema_errors"])

        print(f"{game.upper():<8} | {res['file']:<18} | {res['file_size']:<10,d} | {res['count']:<7} | {res['with_desc']:<10} | {coverage:6.2f}%   | {status}")

        if res["placeholders"]:
            print(f"  [!] Found {len(res['placeholders'])} placeholders in {res['file']}:")
            for p_name, p_desc, p_reason in res["placeholders"][:5]:
                print(f"      - '{p_name}': '{p_desc}' -> {p_reason}")
        if res["schema_errors"]:
            print(f"  [!] Found {len(res['schema_errors'])} schema errors in {res['file']}:")
            for err in res["schema_errors"][:5]:
                print(f"      - {err}")

    print("-" * 80)
    item_cov = (total_items_with_desc / total_items * 100.0) if total_items > 0 else 0.0
    print(f"ITEMS TOTAL: {total_items} items across 7 games | {total_items_with_desc} with valid descriptions ({item_cov:.2f}%)")

    print("\n[2/2] VALIDATING PRODUCTION SKILLS JSON FILES")
    print("-" * 80)
    print(f"{'Game':<8} | {'File':<18} | {'Size (B)':<10} | {'Count':<7} | {'With Desc':<10} | {'Coverage':<9} | {'Status'}")
    print("-" * 80)

    for game in GAMES:
        total_files_checked += 1
        res = validate_skills(game)

        coverage = (res["with_desc"] / res["count"] * 100.0) if res["count"] > 0 else 0.0
        status = "PASS" if (res["valid_json"] and res["count"] > 0 and res["with_desc"] == res["count"] and len(res["placeholders"]) == 0 and len(res["schema_errors"]) == 0) else "FAIL"

        if status == "PASS":
            total_files_valid += 1
        else:
            all_passed = False

        total_skills += res["count"]
        total_skills_with_desc += res["with_desc"]
        total_skill_placeholders += len(res["placeholders"])
        total_skill_schema_errors += len(res["schema_errors"])

        print(f"{game.upper():<8} | {res['file']:<18} | {res['file_size']:<10,d} | {res['count']:<7} | {res['with_desc']:<10} | {coverage:6.2f}%   | {status}")

        if res["placeholders"]:
            print(f"  [!] Found {len(res['placeholders'])} placeholders in {res['file']}:")
            for p_name, p_desc, p_reason in res["placeholders"][:5]:
                print(f"      - '{p_name}': '{p_desc}' -> {p_reason}")
        if res["schema_errors"]:
            print(f"  [!] Found {len(res['schema_errors'])} schema errors in {res['file']}:")
            for err in res["schema_errors"][:5]:
                print(f"      - {err}")

    print("-" * 80)
    skill_cov = (total_skills_with_desc / total_skills * 100.0) if total_skills > 0 else 0.0
    print(f"SKILLS TOTAL: {total_skills} skills across 7 games | {total_skills_with_desc} with valid descriptions ({skill_cov:.2f}%)")

    print("\n" + "=" * 80)
    print("                     FINAL VALIDATION SUMMARY")
    print("=" * 80)
    print(f"Files Checked           : {total_files_checked} / 14")
    print(f"Files Passed            : {total_files_valid} / 14")
    print(f"Total Items             : {total_items}")
    print(f"Items With Descriptions : {total_items_with_desc} ({item_cov:.2f}%)")
    print(f"Item Placeholders       : {total_item_placeholders}")
    print(f"Item Schema Errors      : {total_item_schema_errors}")
    print(f"Total Skills            : {total_skills}")
    print(f"Skills With Descriptions: {total_skills_with_desc} ({skill_cov:.2f}%)")
    print(f"Skill Placeholders      : {total_skill_placeholders}")
    print(f"Skill Schema Errors     : {total_skill_schema_errors}")
    print("=" * 80)

    # Acceptance Criteria Verification
    c1 = (total_files_checked == 14 and total_files_valid == 14)
    c2 = (total_items > 0 and total_items == total_items_with_desc)
    c3 = (total_skills > 0 and total_skills == total_skills_with_desc)
    c4 = (total_item_placeholders == 0 and total_skill_placeholders == 0)
    c5 = (total_item_schema_errors == 0 and total_skill_schema_errors == 0)

    print("\nACCEPTANCE CRITERIA VERIFICATION:")
    print(f"  [PASS] All 14 JSON files valid and present               : {'PASS' if c1 else 'FAIL'}")
    print(f"  [PASS] 100% of items have valid non-empty description     : {'PASS' if c2 else 'FAIL'} ({total_items_with_desc}/{total_items})")
    print(f"  [PASS] 100% of skills have valid non-empty description    : {'PASS' if c3 else 'FAIL'} ({total_skills_with_desc}/{total_skills})")
    print(f"  [PASS] 0 placeholder strings detected                     : {'PASS' if c4 else 'FAIL'}")
    print(f"  [PASS] 0 schema / structural errors detected              : {'PASS' if c5 else 'FAIL'}")

    if all_passed and c1 and c2 and c3 and c4 and c5:
        print("\n>>> OVERALL VERDICT: ALL ACCEPTANCE CRITERIA 100% MET (PASS) <<<\n")
        return 0
    else:
        print("\n>>> OVERALL VERDICT: VALIDATION FAILED <<<\n")
        return 1

if __name__ == "__main__":
    sys.exit(main())
