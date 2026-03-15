# Generate P3P fusion chart from FES chart by removing the Aeon row/column
$fes = Get-Content 'C:\Users\omare\Downloads\megaten-fusion-tool-master\megaten-fusion-tool-master\src\app\p3\data\fes-fusion-chart.json' -Raw | ConvertFrom-Json

$races = [System.Collections.Generic.List[string]]::new()
foreach ($r in $fes.races) { $races.Add($r) }
$aeonIdx = $races.IndexOf('Aeon')
Write-Host "Aeon index: $aeonIdx, total races: $($races.Count)"

# Remove Aeon from races
$races.RemoveAt($aeonIdx)

# Remove Aeon column from each row, and remove Aeon row
$newTable = [System.Collections.Generic.List[object]]::new()
for ($i = 0; $i -lt $fes.table.Count; $i++) {
    if ($i -eq $aeonIdx) { continue } # skip Aeon row
    $row = [System.Collections.Generic.List[string]]::new()
    for ($j = 0; $j -lt $fes.table[$i].Count; $j++) {
        if ($j -eq $aeonIdx) { continue } # skip Aeon column
        $row.Add($fes.table[$i][$j])
    }
    $newTable.Add($row.ToArray())
}

# Build output object
$out = [ordered]@{
    races = $races.ToArray()
    table = $newTable.ToArray()
}

$json = $out | ConvertTo-Json -Depth 10
Set-Content 'app\src\main\assets\data\fusion-charts\p3p-fusion-chart.json' $json
Write-Host "Written p3p-fusion-chart.json with $($races.Count) races and $($newTable.Count) rows"

# Verify a known cell: Fool(0) x Justice(8) should be Lovers
$foolIdx = [array]::IndexOf($out.races, 'Fool')
$justiceIdx = [array]::IndexOf($out.races, 'Justice')
Write-Host "Fool=$foolIdx Justice=$justiceIdx table[Fool][Justice]=$($out.table[$foolIdx][$justiceIdx])"
