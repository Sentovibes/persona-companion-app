# Simulates the fusion algorithm for all games and finds personas with 0 recipes.
# Run from: persona-companion-app/
# Usage: powershell -ExecutionPolicy Bypass -File find_zero_recipe_personas.ps1

$dataRoot = "app\src\main\assets\data"

$games = @(
    @{ id="p3fes"; personas="$dataRoot\persona3\personas.json";          chart="$dataRoot\fusion-charts\p3-fusion-chart.json";      special="$dataRoot\special-fusions\p3-special.json"; triangular=$false },
    @{ id="p3p";   personas="$dataRoot\persona3\portable_personas.json"; chart="$dataRoot\fusion-charts\p3p-fusion-chart.json";     special="$dataRoot\special-fusions\p3-special.json"; triangular=$false },
    @{ id="p3r";   personas="$dataRoot\persona3\reload_personas.json";   chart="$dataRoot\fusion-charts\p3r-fusion-chart.json";     special="$dataRoot\special-fusions\p3r-special.json"; triangular=$true },
    @{ id="p4";    personas="$dataRoot\persona4\personas.json";          chart="$dataRoot\fusion-charts\p4-base-fusion-chart.json"; special="$dataRoot\special-fusions\p4-special.json"; triangular=$false },
    @{ id="p4g";   personas="$dataRoot\persona4\golden_personas.json";   chart="$dataRoot\fusion-charts\p4-fusion-chart.json";      special="$dataRoot\special-fusions\p4-special.json"; triangular=$false },
    @{ id="p5";    personas="$dataRoot\persona5\personas.json";          chart="$dataRoot\fusion-charts\p5-fusion-chart.json";      special="$dataRoot\special-fusions\p5-special.json"; triangular=$true },
    @{ id="p5r";   personas="$dataRoot\persona5\royal_personas.json";    chart="$dataRoot\fusion-charts\p5-fusion-chart.json";      special="$dataRoot\special-fusions\p5r-special.json"; triangular=$true }
)

function Get-ResultArcana($table, $races, $arcanaA, $arcanaB, $triangular) {
    $idxA = [array]::IndexOf($races, $arcanaA)
    $idxB = [array]::IndexOf($races, $arcanaB)
    if ($idxA -lt 0 -or $idxB -lt 0) { return $null }
    if ($triangular) {
        $hi = [math]::Max($idxA, $idxB)
        $lo = [math]::Min($idxA, $idxB)
        $r = $table[$hi][$lo]
    } else {
        $r1 = $table[$idxA][$idxB]
        $r2 = $table[$idxB][$idxA]
        $r = if ($r1 -and $r1 -ne "-") { $r1 } else { $r2 }
    }
    if (-not $r -or $r -eq "-") { return $null }
    return $r
}

foreach ($game in $games) {
    if (-not (Test-Path $game.chart)) {
        Write-Host "[$($game.id)] SKIP - chart not found: $($game.chart)"
        continue
    }

    $personaJson = Get-Content $game.personas -Raw | ConvertFrom-Json
    $chartJson   = Get-Content $game.chart    -Raw | ConvertFrom-Json
    $specialJson = if (Test-Path $game.special) { Get-Content $game.special -Raw | ConvertFrom-Json } else { $null }

    $races = $chartJson.races
    $table = $chartJson.table

    # Build persona list
    $personas = @()
    foreach ($prop in $personaJson.PSObject.Properties) {
        $lvlRaw = $prop.Value.level
        $lvl = if ($lvlRaw -ne $null) { [int]$lvlRaw } else { 0 }
        $personas += [PSCustomObject]@{
            Name   = $prop.Name
            Arcana = $prop.Value.arcana
            Level  = $lvl
            IsDlc  = $prop.Value.isDlc -eq $true
        }
    }

    # Build byArcana map sorted by level
    $byArcana = @{}
    foreach ($p in $personas) {
        if (-not $byArcana.ContainsKey($p.Arcana)) { $byArcana[$p.Arcana] = [System.Collections.Generic.List[object]]::new() }
        $byArcana[$p.Arcana].Add($p)
    }
    $byArcanaKeys = @($byArcana.Keys)
    foreach ($k in $byArcanaKeys) {
        $byArcana[$k] = @($byArcana[$k] | Sort-Object Level)
    }

    $zeroRecipe = @()

    foreach ($target in $personas) {
        if ($target.IsDlc) { continue }

        # Check special fusion
        $specialKey = $target.Name
        if ($specialJson -and $specialJson.PSObject.Properties.Name -contains $specialKey) {
            $combos = $specialJson.$specialKey
            $valid = $false
            foreach ($combo in $combos) {
                $allFound = $true
                foreach ($ing in $combo) {
                    if (-not ($personas | Where-Object { $_.Name -eq $ing })) { $allFound = $false; break }
                }
                if ($allFound) { $valid = $true; break }
            }
            if ($valid) { continue }
        }

        # Normal fusion
        $found = $false

        # Same-arcana fusion
        $sameArcanaList = @($byArcana[$target.Arcana] | Where-Object { $_.Name -ne $target.Name })
        $allArcanaLevels = @($byArcana[$target.Arcana] | ForEach-Object { $_.Level })

        for ($i = 0; $i -lt $sameArcanaList.Count -and -not $found; $i++) {
            $ing1 = $sameArcanaList[$i]; $lvl1 = $ing1.Level
            $resultLvls = @($allArcanaLevels | Where-Object { $_ -ne $lvl1 } | ForEach-Object { $_ * 2 } | Sort-Object)

            for ($j = $i + 1; $j -lt $sameArcanaList.Count -and -not $found; $j++) {
                $ing2 = $sameArcanaList[$j]; $lvl2 = $ing2.Level
                $sum = $lvl1 + $lvl2 + 2
                $resultLvlIndex = (@($resultLvls | Where-Object { $_ -le $sum }).Count) - 1
                if ($resultLvlIndex -ge 0 -and ($resultLvls[$resultLvlIndex] / 2) -eq $lvl2) { $resultLvlIndex-- }
                if ($resultLvlIndex -lt 0) { continue }
                $resultLevel = $resultLvls[$resultLvlIndex] / 2

                $candidate = $byArcana[$target.Arcana] | Where-Object { $_.Level -eq $resultLevel } | Select-Object -First 1
                if ($candidate -and $candidate.Name -eq $target.Name) {
                    $found = $true
                }
            }
        }

        # Cross-arcana fusion
        for ($i = 0; $i -lt $personas.Count -and -not $found; $i++) {
            for ($j = $i + 1; $j -lt $personas.Count -and -not $found; $j++) {
                $a = $personas[$i]; $b = $personas[$j]
                if (-not $a.Arcana -or -not $b.Arcana) { continue }
                if ($a.Arcana -eq $b.Arcana) { continue } # handled above

                # For asymmetric square matrices, both table[A][B] and table[B][A] are valid results
                $idxA = [array]::IndexOf($races, $a.Arcana)
                $idxB = [array]::IndexOf($races, $b.Arcana)
                if ($idxA -lt 0 -or $idxB -lt 0) { continue }

                if ($game.triangular) {
                    $hi = [math]::Max($idxA, $idxB); $lo = [math]::Min($idxA, $idxB)
                    $resultArcanas = @($table[$hi][$lo]) | Where-Object { $_ -and $_ -ne "-" }
                } else {
                    $r1 = $table[$idxA][$idxB]; $r2 = $table[$idxB][$idxA]
                    $resultArcanas = @()
                    if ($r1 -and $r1 -ne "-") { $resultArcanas += $r1 }
                    if ($r2 -and $r2 -ne "-" -and $r2 -ne $r1) { $resultArcanas += $r2 }
                }

                foreach ($resultArcana in $resultArcanas) {
                    if ($resultArcana -ne $target.Arcana) { continue }

                    $resultLevel = [math]::Floor(($a.Level + $b.Level) / 2) + 1

                    $candidate = $byArcana[$resultArcana] | Where-Object {
                        $_.Level -ge $resultLevel -and $_.Name -ne $a.Name -and $_.Name -ne $b.Name
                    } | Select-Object -First 1

                    if ($candidate -and $candidate.Name -eq $target.Name) {
                        $found = $true; break
                    }
                }
            }
        }

        if (-not $found) {
            $zeroRecipe += "$($target.Name) ($($target.Arcana) Lv.$($target.Level))"
        }
    }

    Write-Host ""
    Write-Host "=== $($game.id) === $($zeroRecipe.Count) personas with 0 recipes ==="
    $zeroRecipe | ForEach-Object { Write-Host "  $_" }
}
