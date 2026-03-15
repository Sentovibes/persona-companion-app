$p = Get-Content 'app\src\main\assets\data\persona3\personas.json' -Raw | ConvertFrom-Json
$found = 0
foreach ($prop in $p.PSObject.Properties) {
    if ($prop.Value.unlock) {
        Write-Host "$($prop.Name): $($prop.Value.unlock)"
        $found++
        if ($found -ge 5) { break }
    }
}
if ($found -eq 0) { Write-Host "No unlock fields found in p3fes personas" }
