param(
    [Parameter(Mandatory = $true)][string]$VanillaClientJar,
    [Parameter(Mandatory = $true)][string]$BaseClientJar,
    [string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent)
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $VanillaClientJar))
$baseZip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $BaseClientJar))
$root = Join-Path $ProjectRoot 'src/main/resources'
$names = @('copper_bars', 'copper_chest', 'copper_lantern', 'copper_chain', 'lightning_rod' | ForEach-Object {
    $family = $_
    foreach ($wax in '', 'waxed_') { foreach ($prefix in '', 'exposed_', 'weathered_', 'oxidized_') { "$wax$prefix$family" } }
})
$names = @($names | Where-Object { $_ -ne 'lightning_rod' }) + @('copper_torch')
$recipePattern = '^data/minecraft/recipe/(?:[^/]*copper_(?:bars|chest|lantern|chain|torch)(?:_|\.json)|waxed_[^/]*lightning_rod_from_honeycomb\.json|copper_nugget\.json|copper_ingot_from_nuggets\.json|copper_trapdoor\.json)'
function Read-Entry($name) {
    $reader = [IO.StreamReader]::new($zip.GetEntry($name).Open())
    try { return $reader.ReadToEnd() } finally { $reader.Dispose() }
}
function Canonical($value) {
    if ($null -eq $value) { return 'null' }
    if ($value -is [Management.Automation.PSCustomObject]) {
        $parts = foreach ($p in ($value.PSObject.Properties | Sort-Object Name)) { ($p.Name | ConvertTo-Json -Compress) + ':' + (Canonical $p.Value) }
        return '{' + ($parts -join ',') + '}'
    }
    if ($value -is [array]) { return '[' + (($value | ForEach-Object { Canonical $_ }) -join ',') + ']' }
    return ConvertTo-Json -InputObject $value -Compress
}
function Read-Local($relative) {
    return (Get-Content (Join-Path $root $relative) -Raw).Replace('copperandtuffbackport:', 'minecraft:') | ConvertFrom-Json
}
function Ingredient($value) {
    if ($value -is [array]) { return ,@($value | ForEach-Object { Ingredient $_ }) }
    if ($value -eq 'minecraft:copper_nugget') { return [pscustomobject]@{tag = 'minecraft:copper_nuggets'} }
    if ($value.StartsWith('#')) { return [pscustomobject]@{tag = $value.Substring(1)} }
    return [pscustomobject]@{item = $value}
}
try {
    foreach ($name in $names) {
        if (!(Test-Path (Join-Path $root "assets/copperandtuffbackport/models/item/$name.json"))) { throw "Missing item model: $name" }
        $local = Read-Local "data/copperandtuffbackport/loot_tables/blocks/$name.json"
        $expected = Read-Entry "data/minecraft/loot_table/blocks/$name.json" | ConvertFrom-Json
        $expected.PSObject.Properties.Remove('random_sequence')
        foreach ($pool in $expected.pools) { foreach ($item in $pool.entries) {
            if ($item.functions) { $item.functions = @($item.functions | ForEach-Object {
                if ($_.function -eq 'minecraft:copy_components') {
                    if (($_.include -join ',') -ne 'minecraft:custom_name') { throw 'Unexpected chest loot components' }
                    [pscustomobject]@{function = 'minecraft:copy_name'; source = 'block_entity'}
                } else { $_ }
            }) }
        } }
        if ((Canonical $local) -cne (Canonical $expected)) { throw "Loot mismatch: $name" }
    }
    $recipes = @($zip.Entries | Where-Object { $_.FullName -match $recipePattern })
    $localRecipes = @(Get-ChildItem (Join-Path $root 'data/copperandtuffbackport/recipes') -File |
        Where-Object { ('data/minecraft/recipe/' + $_.Name) -match $recipePattern })
    if (Compare-Object @($recipes.Name) @($localRecipes.Name)) { throw 'Missing or extra Copper Age recipes' }
    foreach ($entry in $recipes) {
        $local = Read-Local "data/copperandtuffbackport/recipes/$($entry.Name)"
        $expected = Read-Entry $entry.FullName | ConvertFrom-Json
        foreach ($key in $expected.key.PSObject.Properties) { $key.Value = Ingredient $key.Value }
        if ($expected.ingredients) { $expected.ingredients = @($expected.ingredients | ForEach-Object { Ingredient $_ }) }
        $expected.result | Add-Member -NotePropertyName item -NotePropertyValue $expected.result.id
        $expected.result.PSObject.Properties.Remove('id')
        if (!$expected.result.count) { $expected.result | Add-Member -NotePropertyName count -NotePropertyValue 1 }
        if ((Canonical $local) -cne (Canonical $expected)) { throw "Recipe mismatch: $($entry.Name)" }
    }
    $unlockCount = 0
    foreach ($entry in $zip.Entries | Where-Object { ('data/minecraft/recipe/' + $_.Name) -match $recipePattern -and $_.FullName -match '^data/minecraft/advancement/recipes/' }) {
        $relative = $entry.FullName.Replace('data/minecraft/advancement/', '')
        $local = Read-Local "data/copperandtuffbackport/advancements/$relative"
        $expected = Read-Entry $entry.FullName | ConvertFrom-Json
        foreach ($criterion in $expected.criteria.PSObject.Properties) { foreach ($predicate in $criterion.Value.conditions.items) {
            if ($predicate.items -is [string]) { $predicate.items = @($predicate.items) }
            if (@($predicate.items) -contains 'minecraft:copper_nugget') {
                $predicate.PSObject.Properties.Remove('items')
                $predicate | Add-Member -NotePropertyName tag -NotePropertyValue 'minecraft:copper_nuggets'
            }
        } }
        if ((Canonical $local) -cne (Canonical $expected)) { throw "Recipe unlock mismatch: $relative" }
        $unlockCount++
    }
    $assetCount = 0
    foreach ($file in Get-ChildItem (Join-Path $root 'assets/copperandtuffbackport') -Recurse -Filter '*.json') {
        $relative = $file.FullName.Substring((Join-Path $root 'assets/copperandtuffbackport').Length + 1).Replace('\', '/')
        if ($relative -notmatch '^(models|blockstates)/' -or $relative -notmatch 'copper_(bars|chest)|template_bars_') { continue }
        $raw = Get-Content $file.FullName -Raw
        foreach ($match in [regex]::Matches($raw, '"(?:parent|model)"\s*:\s*"([^":]+):([^"]+)"')) {
            $ns = $match.Groups[1].Value; $model = $match.Groups[2].Value
            if ($model -like 'builtin/*') { continue }
            if ($ns -eq 'copperandtuffbackport') {
                if (!(Test-Path (Join-Path $root "assets/$ns/models/$model.json"))) { throw "Missing custom model: $model" }
            } elseif ($ns -eq 'minecraft' -and !$baseZip.GetEntry("assets/minecraft/models/$model.json")) {
                throw "Model unavailable in 1.20.1: $model"
            }
        }
        $assetCount++
    }
    $textureCount = 0
    foreach ($file in Get-ChildItem (Join-Path $root 'assets/copperandtuffbackport/textures') -Recurse -File) {
        $relative = $file.FullName.Substring((Join-Path $root 'assets/copperandtuffbackport').Length + 1).Replace('\', '/')
        if ($relative -notmatch 'copper_(bars|chest|lantern|chain|torch|nugget|fire_flame)|lightning_rod') { continue }
        $entry = $zip.GetEntry("assets/minecraft/$relative")
        if (!$entry) { throw "Texture absent from release: $relative" }
        if ($relative.EndsWith('.mcmeta')) {
            $expected = Read-Entry "assets/minecraft/$relative" | ConvertFrom-Json
            $actual = Get-Content $file.FullName -Raw | ConvertFrom-Json
            if ((Canonical $actual) -ne (Canonical $expected)) { throw "Animation metadata differs from release: $relative" }
            $textureCount++
            continue
        }
        $stream = $entry.Open()
        try { $hash = [BitConverter]::ToString([Security.Cryptography.SHA256]::Create().ComputeHash($stream)).Replace('-', '') } finally { $stream.Dispose() }
        if ((Get-FileHash $file.FullName -Algorithm SHA256).Hash -ne $hash) { throw "Texture differs from release: $relative" }
        $textureCount++
    }
    Write-Output "PASS: $($names.Count) loot tables, $($recipes.Count) recipes, $unlockCount recipe unlocks, $textureCount release textures, and $assetCount bars/chest model files checked."
} finally { $zip.Dispose(); $baseZip.Dispose() }
