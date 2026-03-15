# Converts megaten-fusion-tool demon-data.json files into the app's persona JSON format.
# Run from: persona-companion-app/
# Usage: pwsh convert_persona_data.ps1

$megaten = "C:\Users\omare\Downloads\megaten-fusion-tool-master\megaten-fusion-tool-master\src\app"
$out     = "app\src\main\assets\data"

function Convert-Demon($name, $d, $extras = @{}, $dlcNames = @()) {
    $obj = [ordered]@{
        arcana = $d.race
        level  = $d.lvl
        stats  = $d.stats
        skills = $d.skills
        resists = $d.resists
    }
    if ($d.heart)  { $obj.heart   = $d.heart }
    if ($d.trait)  { $obj.trait   = $d.trait }
    if ($d.item)   { $obj.item    = $d.item }
    if ($d.itemr)  { $obj.itemr   = $d.itemr }
    if ($d.inherits) { $obj.inherits = $d.inherits }
    foreach ($k in $extras.Keys) { $obj[$k] = $extras[$k] }
    # Mark DLC personas
    if ($dlcNames -contains $name) { $obj.isDlc = $true }
    return $obj
}

function Get-DlcNames($unlockFiles) {
    $names = @()
    foreach ($f in $unlockFiles) {
        if (-not (Test-Path $f)) { continue }
        $data = Get-Content $f -Raw | ConvertFrom-Json
        foreach ($entry in $data) {
            if ($entry.category -eq "Downloadable Content") {
                foreach ($prop in $entry.conditions.PSObject.Properties) {
                    # Keys may be comma-separated: "Orpheus,Orpheus Picaro"
                    $prop.Name -split "," | ForEach-Object { $names += $_.Trim() }
                }
            }
        }
    }
    return $names
}

function Merge-And-Write($sources, $outPath, $extras = @{}, $unlockFiles = @()) {
    $dlcNames = Get-DlcNames $unlockFiles
    $merged = [ordered]@{}
    foreach ($src in $sources) {
        $json = Get-Content $src -Raw | ConvertFrom-Json
        foreach ($prop in $json.PSObject.Properties) {
            $ex = if ($extras.ContainsKey($prop.Name)) { $extras[$prop.Name] } else { @{} }
            $merged[$prop.Name] = Convert-Demon $prop.Name $prop.Value $ex $dlcNames
        }
    }
    # Sort by name
    $sorted = [ordered]@{}
    foreach ($k in ($merged.Keys | Sort-Object)) { $sorted[$k] = $merged[$k] }
    $sorted | ConvertTo-Json -Depth 10 | Set-Content $outPath -Encoding UTF8
    Write-Host "Written: $outPath ($($sorted.Count) personas, $($dlcNames.Count) DLC tagged)"
}

# ── P3 FES ──────────────────────────────────────────────────────────────────
# FES = van + fes override (fes wins on conflict)
Merge-And-Write @(
    "$megaten\p3\data\van-demon-data.json",
    "$megaten\p3\data\fes-demon-data.json"
) "$out\persona3\personas.json" @{} @(
    "$megaten\p3\data\van-demon-unlocks.json",
    "$megaten\p3\data\fes-demon-unlocks.json"
)

# ── P3 Portable ─────────────────────────────────────────────────────────────
# P3P = van + fes + p3p override
Merge-And-Write @(
    "$megaten\p3\data\van-demon-data.json",
    "$megaten\p3\data\fes-demon-data.json",
    "$megaten\p3\data\p3p-demon-data.json"
) "$out\persona3\portable_personas.json" @{} @(
    "$megaten\p3\data\van-demon-unlocks.json",
    "$megaten\p3\data\fes-demon-unlocks.json"
)

# ── P3 Reload ────────────────────────────────────────────────────────────────
Merge-And-Write @(
    "$megaten\p3r\data\demon-data.json"
) "$out\persona3\reload_personas.json" @{} @(
    "$megaten\p3r\data\demon-unlocks.json"
)

# ── P4 ───────────────────────────────────────────────────────────────────────
Merge-And-Write @(
    "$megaten\p4\data\demon-data.json"
) "$out\persona4\personas.json" @{} @(
    "$megaten\p4\data\demon-unlocks.json"
)

# ── P4 Golden ────────────────────────────────────────────────────────────────
# Golden = p4 base + golden override
Merge-And-Write @(
    "$megaten\p4\data\demon-data.json",
    "$megaten\p4\data\golden-demon-data.json"
) "$out\persona4\golden_personas.json" @{} @(
    "$megaten\p4\data\demon-unlocks.json",
    "$megaten\p4\data\golden-demon-unlocks.json"
)

# ── P5 ───────────────────────────────────────────────────────────────────────
Merge-And-Write @(
    "$megaten\p5\data\demon-data.json"
) "$out\persona5\personas.json" @{} @(
    "$megaten\p5\data\demon-unlocks.json"
)

# ── P5 Royal ─────────────────────────────────────────────────────────────────
# Royal = p5 base + roy override
Merge-And-Write @(
    "$megaten\p5\data\demon-data.json",
    "$megaten\p5\data\roy-demon-data.json"
) "$out\persona5\royal_personas.json" @{} @(
    "$megaten\p5\data\demon-unlocks.json",
    "$megaten\p5\data\roy-demon-unlocks.json"
)

Write-Host "`nDone."

# ════════════════════════════════════════════════════════════════════════════
# SPECIAL FUSIONS CONVERSION
# Source format:  "Name": ["ing1", "ing2", ...]   (flat array = one combo)
# App format:     "Name": [["ing1", "ing2", ...]]  (array of combos)
# Entries with empty arrays (treasure demons) are skipped.
# ════════════════════════════════════════════════════════════════════════════

function Convert-Special($sources) {
    $merged = [ordered]@{}
    foreach ($src in $sources) {
        $json = Get-Content $src -Raw | ConvertFrom-Json
        foreach ($prop in $json.PSObject.Properties) {
            $ings = @($prop.Value)
            if ($ings.Count -eq 0) { continue }   # skip treasure demons
            $merged[$prop.Name] = @(, $ings)       # wrap in outer array
        }
    }
    # Sort by name
    $sorted = [ordered]@{}
    foreach ($k in ($merged.Keys | Sort-Object)) { $sorted[$k] = $merged[$k] }
    return $sorted
}

function Write-Special($sources, $outPath) {
    $data = Convert-Special $sources
    $data | ConvertTo-Json -Depth 10 | Set-Content $outPath -Encoding UTF8
    Write-Host "Written: $outPath ($($data.Count) special fusions)"
}

$sout = "$out\special-fusions"

# ── P3 FES + P3P (shared file) ───────────────────────────────────────────────
# van + fes override + pair (pair has 2-ingredient entries, same flat format)
Write-Special @(
    "$megaten\p3\data\van-special-recipes.json",
    "$megaten\p3\data\fes-special-recipes.json",
    "$megaten\p3\data\pair-special-recipes.json"
) "$sout\p3-special.json"

# ── P3 Reload ────────────────────────────────────────────────────────────────
Write-Special @(
    "$megaten\p3r\data\special-recipes.json"
) "$sout\p3r-special.json"

# ── P4 / P4G (shared file) ───────────────────────────────────────────────────
Write-Special @(
    "$megaten\p4\data\special-recipes.json"
) "$sout\p4-special.json"

# ── P5 ───────────────────────────────────────────────────────────────────────
Write-Special @(
    "$megaten\p5\data\special-recipes.json"
) "$sout\p5-special.json"

# ── P5 Royal ─────────────────────────────────────────────────────────────────
# roy overrides base p5
Write-Special @(
    "$megaten\p5\data\special-recipes.json",
    "$megaten\p5\data\roy-special-recipes.json"
) "$sout\p5r-special.json"

Write-Host "`nSpecial fusions done."
