$p = Get-Content 'app\src\main\assets\data\persona3\portable_personas.json' -Raw | ConvertFrom-Json

# All Lovers arcana, sorted by level
$lovers = @()
foreach ($prop in $p.PSObject.Properties) {
    if ($prop.Value.arcana -eq 'Lovers') {
        $lovers += [PSCustomObject]@{ Name=$prop.Name; Level=[int]$prop.Value.level }
    }
}
$lovers = $lovers | Sort-Object Level
Write-Host "=== Lovers arcana ==="
$lovers | ForEach-Object { Write-Host "  $($_.Name) Lv.$($_.Level)" }

# Trace same-arcana formula for Alp (Lv.6)
Write-Host ""
Write-Host "=== Same-arcana fusion targeting Alp (Lv.6) ==="
$target = "Alp"; $targetLevel = 6
$allLevels = @($lovers | ForEach-Object { $_.Level })
$candidates = @($lovers | Where-Object { $_.Name -ne $target })

for ($i = 0; $i -lt $candidates.Count; $i++) {
    $ing1 = $candidates[$i]; $lvl1 = $ing1.Level
    $resultLvls = @($allLevels | Where-Object { $_ -ne $lvl1 } | ForEach-Object { $_ * 2 } | Sort-Object)

    for ($j = $i + 1; $j -lt $candidates.Count; $j++) {
        $ing2 = $candidates[$j]; $lvl2 = $ing2.Level
        $sum = $lvl1 + $lvl2 + 2
        $resultLvlIndex = (@($resultLvls | Where-Object { $_ -le $sum }).Count) - 1
        if ($resultLvlIndex -ge 0 -and ($resultLvls[$resultLvlIndex] / 2) -eq $lvl2) { $resultLvlIndex-- }
        if ($resultLvlIndex -lt 0) { continue }
        $resultLevel = $resultLvls[$resultLvlIndex] / 2

        $result = $lovers | Where-Object { $_.Level -eq $resultLevel } | Select-Object -First 1
        if ($result -and $result.Name -eq $target) {
            Write-Host "  HIT: $($ing1.Name)(Lv.$lvl1) + $($ing2.Name)(Lv.$lvl2) => resultLvlIndex=$resultLvlIndex resultLevel=$resultLevel"
        }
    }
}

# Also show cross-arcana for Alp
Write-Host ""
Write-Host "=== Cross-arcana fusion targeting Alp (Lovers Lv.6) ==="
$chart = Get-Content 'app\src\main\assets\data\fusion-charts\p3p-fusion-chart.json' -Raw | ConvertFrom-Json
$races = $chart.races; $table = $chart.table

$byArcana = @{}
foreach ($prop in $p.PSObject.Properties) {
    $a = $prop.Value.arcana
    if (-not $byArcana[$a]) { $byArcana[$a] = [System.Collections.Generic.List[object]]::new() }
    $byArcana[$a].Add([PSCustomObject]@{ Name=$prop.Name; Level=[int]$prop.Value.level; Arcana=$a })
}
foreach ($k in @($byArcana.Keys)) { $byArcana[$k] = @($byArcana[$k] | Sort-Object Level) }

$allPersonas = @()
foreach ($prop in $p.PSObject.Properties) {
    $allPersonas += [PSCustomObject]@{ Name=$prop.Name; Level=[int]$prop.Value.level; Arcana=$prop.Value.arcana }
}

# Manually check the 5 pairs from the screenshot
Write-Host "=== Manual check of megaten screenshot pairs ==="
$pairs = @(
    @("Orpheus","Fool",1,"Angel","Justice",4),
    @("Apsaras","Priestess",3,"Angel","Justice",4),
    @("Nekomata","Magician",5,"Apsaras","Priestess",3),
    @("Jack Frost","Magician",8,"Apsaras","Priestess",3),
    @("Orpheus","Fool",1,"Archangel","Justice",10)
)
foreach ($pair in $pairs) {
    $nameA=$pair[0]; $arcanaA=$pair[1]; $lvlA=[int]$pair[2]
    $nameB=$pair[3]; $arcanaB=$pair[4]; $lvlB=[int]$pair[5]
    $idxA = [array]::IndexOf($races, $arcanaA)
    $idxB = [array]::IndexOf($races, $arcanaB)
    $r1 = $table[$idxA][$idxB]; $r2 = $table[$idxB][$idxA]
    $resultLevel = [math]::Floor(($lvlA + $lvlB) / 2) + 1
    $candidate = $byArcana['Lovers'] | Where-Object { $_.Level -ge $resultLevel -and $_.Name -ne $nameA -and $_.Name -ne $nameB } | Select-Object -First 1
    Write-Host "  $nameA($arcanaA,$lvlA) + $nameB($arcanaB,$lvlB): table[$idxA][$idxB]=$r1 table[$idxB][$idxA]=$r2 resultLevel=$resultLevel => $($candidate.Name)"
}

Write-Host ""
Write-Host "=== All cross-arcana hits for Alp ==="
for ($i = 0; $i -lt $allPersonas.Count; $i++) {
    for ($j = $i+1; $j -lt $allPersonas.Count; $j++) {
        $a = $allPersonas[$i]; $b = $allPersonas[$j]
        if ($a.Arcana -eq $b.Arcana) { continue }
        $idxA = [array]::IndexOf($races, $a.Arcana)
        $idxB = [array]::IndexOf($races, $b.Arcana)
        if ($idxA -lt 0 -or $idxB -lt 0) { continue }
        $r1 = $table[$idxA][$idxB]; $r2 = $table[$idxB][$idxA]
        $resultArcanas = @()
        if ($r1 -and $r1 -ne "-") { $resultArcanas += $r1 }
        if ($r2 -and $r2 -ne "-" -and $r2 -ne $r1) { $resultArcanas += $r2 }
        foreach ($ra in $resultArcanas) {
            if ($ra -ne 'Lovers') { continue }
            $resultLevel = [math]::Floor(($a.Level + $b.Level) / 2) + 1
            $candidate = $byArcana['Lovers'] | Where-Object { $_.Level -ge $resultLevel -and $_.Name -ne $a.Name -and $_.Name -ne $b.Name } | Select-Object -First 1
            if ($candidate -and $candidate.Name -eq $target) {
                Write-Host "  HIT: $($a.Name)($($a.Arcana),Lv.$($a.Level)) + $($b.Name)($($b.Arcana),Lv.$($b.Level)) => resultLevel=$resultLevel => $($candidate.Name)"
            }
        }
    }
}
