# Injects unlock conditions from megaten unlock files into persona JSON files
# Run from: persona-companion-app/

$megatenBase = "C:\Users\omare\Downloads\megaten-fusion-tool-master\megaten-fusion-tool-master\src\app"
$dataRoot = "app\src\main\assets\data"

# Map: game -> [personaFile, unlockFiles[]]
$games = @(
    @{
        id = "p3fes"
        personaFile = "$dataRoot\persona3\personas.json"
        unlockFiles = @("$megatenBase\p3\data\fes-demon-unlocks.json")
    },
    @{
        id = "p3p"
        personaFile = "$dataRoot\persona3\portable_personas.json"
        unlockFiles = @("$megatenBase\p3\data\fes-demon-unlocks.json")
    },
    @{
        id = "p3r"
        personaFile = "$dataRoot\persona3\reload_personas.json"
        unlockFiles = @("$megatenBase\p3r\data\demon-unlocks.json")
    },
    @{
        id = "p4"
        personaFile = "$dataRoot\persona4\personas.json"
        unlockFiles = @("$megatenBase\p4\data\demon-unlocks.json")
    },
    @{
        id = "p4g"
        personaFile = "$dataRoot\persona4\golden_personas.json"
        unlockFiles = @("$megatenBase\p4\data\golden-demon-unlocks.json")
    },
    @{
        id = "p5"
        personaFile = "$dataRoot\persona5\personas.json"
        unlockFiles = @("$megatenBase\p5\data\demon-unlocks.json")
    },
    @{
        id = "p5r"
        personaFile = "$dataRoot\persona5\royal_personas.json"
        unlockFiles = @("$megatenBase\p5\data\roy-demon-unlocks.json")
    }
)

foreach ($game in $games) {
    Write-Host "Processing $($game.id)..."

    # Build unlock map: personaName -> "Category: condition"
    $unlockMap = @{}
    foreach ($unlockFile in $game.unlockFiles) {
        if (-not (Test-Path $unlockFile)) { Write-Host "  SKIP missing: $unlockFile"; continue }
        $unlocks = Get-Content $unlockFile -Raw | ConvertFrom-Json
        foreach ($group in $unlocks) {
            $category = $group.category
            foreach ($prop in $group.conditions.PSObject.Properties) {
                # Handle comma-separated persona names (e.g. "Izanagi,Izanagi Picaro")
                $names = $prop.Name -split ","
                foreach ($name in $names) {
                    $name = $name.Trim()
                    $unlockMap[$name] = "$category`: $($prop.Value)"
                }
            }
        }
    }

    Write-Host "  Found $($unlockMap.Count) unlock entries"

    # Load persona JSON
    $personaJson = Get-Content $game.personaFile -Raw | ConvertFrom-Json

    $injected = 0
    foreach ($prop in $personaJson.PSObject.Properties) {
        $name = $prop.Name
        if ($unlockMap.ContainsKey($name)) {
            $prop.Value | Add-Member -NotePropertyName "unlock" -NotePropertyValue $unlockMap[$name] -Force
            $injected++
        }
    }

    Write-Host "  Injected $injected unlock fields"

    # Write back
    $personaJson | ConvertTo-Json -Depth 20 | Set-Content $game.personaFile -Encoding UTF8
    Write-Host "  Saved $($game.personaFile)"
}

Write-Host ""
Write-Host "Done. Now sync web data manually or run sync script."
