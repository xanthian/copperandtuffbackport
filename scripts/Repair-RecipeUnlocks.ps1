param([string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$data = Join-Path $ProjectRoot 'src/main/resources/data/copperandtuffbackport'
$recipes = @(Get-ChildItem (Join-Path $data 'recipes') -Filter '*.json' | ForEach-Object { $_.BaseName })
$changed = 0
foreach ($file in Get-ChildItem (Join-Path $data 'advancements/recipes') -Recurse -Filter '*.json') {
    $advancement = Get-Content $file.FullName -Raw | ConvertFrom-Json
    $dirty = $false
    $advancement.rewards.recipes = @($advancement.rewards.recipes | ForEach-Object {
        if ($_.StartsWith('minecraft:') -and $_.Substring(10) -in $recipes) {
            $dirty = $true
            'copperandtuffbackport:' + $_.Substring(10)
        } else { $_ }
    })
    foreach ($criterion in $advancement.criteria.PSObject.Properties) {
        $recipe = $criterion.Value.conditions.recipe
        if ($recipe -and $recipe.StartsWith('minecraft:') -and $recipe.Substring(10) -in $recipes) {
            $criterion.Value.conditions.recipe = 'copperandtuffbackport:' + $recipe.Substring(10)
            $dirty = $true
        }
    }
    if ($dirty) {
        [IO.File]::WriteAllText($file.FullName, ($advancement | ConvertTo-Json -Depth 40) + "`n")
        $changed++
    }
}
Write-Output "Corrected recipe namespaces in $changed recipe advancements."
