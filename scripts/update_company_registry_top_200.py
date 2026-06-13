#!/usr/bin/env python3
from pathlib import Path
import csv, json, re, urllib.request

REGISTRY = Path("src/main/java/com/jobseeker/config/CompanyRegistry.java")
CSV = Path("top_200_pbc_targets.csv")

if not REGISTRY.exists():
    raise SystemExit("Run this from /workspaces/job-seeker-backend and keep top_200_pbc_targets.csv in the same folder.")
if not CSV.exists():
    raise SystemExit("Missing top_200_pbc_targets.csv")

text = REGISTRY.read_text()

def fetch_json(url):
    try:
        req = urllib.request.Request(url, headers={"User-Agent":"Mozilla/5.0", "Accept":"application/json"})
        with urllib.request.urlopen(req, timeout=12) as resp:
            return json.loads(resp.read().decode("utf-8", errors="ignore"))
    except Exception:
        return None

def greenhouse_count(slug):
    data = fetch_json(f"https://boards-api.greenhouse.io/v1/boards/{slug}/jobs?content=true")
    return len(data.get("jobs", [])) if isinstance(data, dict) else 0

def lever_count(slug):
    data = fetch_json(f"https://api.lever.co/v0/postings/{slug}?mode=json")
    return len(data) if isinstance(data, list) else 0

def verify(platform, slug):
    if not slug: return 0
    if platform == "GREENHOUSE": return greenhouse_count(slug)
    if platform == "LEVER": return lever_count(slug)
    return 0

def derive_slug(name):
    return re.sub(r"[^a-z0-9]", "", name.lower().replace("&", "and").replace("/", " "))

def best_platform_and_slug(row):
    preferred_platform = row["platform"].strip() or "CAREERS_PAGE"
    preferred_slug = row["slug"].strip() or derive_slug(row["name"])

    # Try preferred first if it is an ATS.
    if preferred_platform in ("GREENHOUSE", "LEVER"):
        count = verify(preferred_platform, preferred_slug)
        if count > 0:
            return preferred_platform, preferred_slug, count

    # Probe common public ATS slugs.
    candidate_slugs = []
    base = derive_slug(row["name"])
    candidate_slugs.extend([preferred_slug, base, base.replace("labs", ""), base.replace("ai", "")])
    candidate_slugs = [s for i,s in enumerate(candidate_slugs) if s and s not in candidate_slugs[:i]]

    for slug in candidate_slugs:
        c = greenhouse_count(slug)
        if c > 0: return "GREENHOUSE", slug, c
        c = lever_count(slug)
        if c > 0: return "LEVER", slug, c

    return "CAREERS_PAGE", preferred_slug, 0

def upsert_company_line(text, name, slug, platform, tier, career_url):
    line = f'        add("{name}", "{slug}", "{platform}", {tier}, "{career_url}");'
    pattern = re.compile(r'\s*add\("' + re.escape(name) + r'",\s*"[^"]*",\s*"[^"]*",\s*\d+,\s*"[^"]*"\);')
    if pattern.search(text):
        text = pattern.sub("\n" + line, text)
        return text, "updated"
    marker = "\n    }\n\n    private void add("
    idx = text.find(marker)
    if idx == -1:
        raise SystemExit("Could not find constructor end marker in CompanyRegistry.java")
    text = text[:idx] + "\n" + line + text[idx:]
    return text, "added"

added=updated=ats=career=0
rows = list(csv.DictReader(CSV.open(encoding="utf-8")))
for row in rows:
    name = row["name"].strip()
    tier = int(row["tier"])
    career_url = row["career_url"].strip()
    platform, slug, count = best_platform_and_slug(row)
    text, action = upsert_company_line(text, name, slug, platform, tier, career_url)
    if action == "added": added += 1
    else: updated += 1
    if platform in ("GREENHOUSE", "LEVER"):
        ats += 1
        print(f"{action.upper():7} {name:32} -> {platform:10} {slug:32} ({count} jobs)")
    else:
        career += 1

REGISTRY.write_text(text)
print("\nSUMMARY")
print("added:", added)
print("updated:", updated)
print("verified ATS companies:", ats)
print("career-page-only companies:", career)
print("total targets processed:", len(rows))
