param([string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$data = Join-Path $ProjectRoot 'src/main/resources/data/copperandtuffbackport'
$ids = @(Get-ChildItem (Join-Path $data 'recipes') -Filter '*.json' | ForEach-Object { "copperandtuffbackport:$($_.BaseName)" })
$unlocked = [Collections.Generic.HashSet[string]]::new()
foreach ($file in Get-ChildItem (Join-Path $data 'advancements/recipes') -Recurse -Filter '*.json') {
    $advancement = Get-Content $file.FullName -Raw | ConvertFrom-Json
    foreach ($id in $advancement.rewards.recipes) {
        if ($id -notin $ids) { throw "Invalid recipe reward $id in $($file.FullName)" }
        [void]$unlocked.Add($id)
    }
    foreach ($criterion in $advancement.criteria.PSObject.Properties) {
        if ($criterion.Value.trigger -eq 'minecraft:recipe_unlocked' -and $criterion.Value.conditions.recipe -notin $ids) {
            throw "Invalid recipe criterion in $($file.FullName)"
        }
    }
    foreach ($group in $advancement.requirements) { foreach ($criterion in $group) {
        if (!$advancement.criteria.PSObject.Properties[$criterion]) { throw "Unknown required criterion $criterion in $($file.FullName)" }
    } }
}
foreach ($id in $ids) { if (!$unlocked.Contains($id)) { throw "Missing recipe unlock for $id" } }
Write-Output "PASS: all $($ids.Count) recipes have valid advancement rewards, criteria, and unlock coverage."
