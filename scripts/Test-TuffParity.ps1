param(
    [Parameter(Mandatory = $true)][string]$VanillaClientJar,
    [string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent)
)

# Compare against a locally supplied, official Java 1.21.1 client JAR.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$resources = Join-Path $ProjectRoot 'src/main/resources'
$modData = Join-Path $resources 'data/copperandtuffbackport'
$zip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $VanillaClientJar))
$failures = [Collections.Generic.List[string]]::new()

function Read-Vanilla([string]$Name) {
    $entry = $zip.GetEntry($Name)
    if (!$entry) { throw "Vanilla entry missing: $Name" }
    $reader = [IO.StreamReader]::new($entry.Open())
    try { return $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
}

function Canonical($Value) {
    if ($null -eq $Value) { return 'null' }
    if ($Value -is [Management.Automation.PSCustomObject]) {
        $parts = foreach ($p in ($Value.PSObject.Properties | Sort-Object Name)) {
            ($p.Name | ConvertTo-Json -Compress) + ':' + (Canonical $p.Value)
        }
        return '{' + ($parts -join ',') + '}'
    }
    if ($Value -is [array]) { return '[' + (($Value | ForEach-Object { Canonical $_ }) -join ',') + ']' }
    return ConvertTo-Json -InputObject $Value -Compress
}

function Compare-Data($Local, $Vanilla, [string]$Label) {
    if ((Canonical $Local) -ne (Canonical $Vanilla)) { $failures.Add($Label) }
}

try {
    $names = @('tuff_slab', 'tuff_stairs', 'tuff_wall', 'polished_tuff', 'polished_tuff_slab',
        'polished_tuff_stairs', 'polished_tuff_wall', 'chiseled_tuff', 'tuff_bricks',
        'tuff_brick_slab', 'tuff_brick_stairs', 'tuff_brick_wall', 'chiseled_tuff_bricks')
    $registered = @(Select-String -Path (Join-Path $ProjectRoot 'src/main/java/xanthian/copperandtuffbackport/blocks/ModTuffBlocks.java') `
        -Pattern 'registerBlock\("([^"]+)"' | ForEach-Object { $_.Matches[0].Groups[1].Value })
    $vanillaNames = @($zip.Entries | Where-Object { $_.FullName -match '^assets/minecraft/blockstates/[^/]*tuff[^/]*\.json$' -and $_.Name -ne 'tuff.json' } |
        ForEach-Object { [IO.Path]::GetFileNameWithoutExtension($_.Name) })
    if (Compare-Object $names $vanillaNames) { $failures.Add('Missing or extra vanilla tuff block variants') }
    if (Compare-Object $names $registered) { $failures.Add('Tuff block registration set') }

    foreach ($name in $names) {
        $local = (Get-Content (Join-Path $modData "loot_tables/blocks/$name.json") -Raw).Replace('copperandtuffbackport:', 'minecraft:') | ConvertFrom-Json
        $vanilla = Read-Vanilla "data/minecraft/loot_table/blocks/$name.json"
        # Random loot sequence identifiers were introduced after 1.20.1.
        $local.PSObject.Properties.Remove('random_sequence')
        $vanilla.PSObject.Properties.Remove('random_sequence')
        Compare-Data $local $vanilla "Loot table: $name"
    }

    $expectedRecipes = @{}
    foreach ($entry in $zip.Entries | Where-Object { $_.FullName -like 'data/minecraft/recipe/*tuff*.json' }) {
        $recipe = Read-Vanilla $entry.FullName
        if ($recipe.result.id -and $recipe.result.id.Replace('minecraft:', '') -in $names) {
            $expectedRecipes[$entry.Name] = $recipe
        }
    }
    $localRecipes = @(Get-ChildItem (Join-Path $modData 'recipes') -Filter '*tuff*.json')
    if (Compare-Object @($expectedRecipes.Keys) @($localRecipes.Name)) { $failures.Add('Missing or extra tuff recipes') }
    foreach ($file in $localRecipes) {
        if (!$expectedRecipes.ContainsKey($file.Name)) { continue }
        $local = (Get-Content $file.FullName -Raw).Replace('copperandtuffbackport:', 'minecraft:') | ConvertFrom-Json
        $vanilla = $expectedRecipes[$file.Name]
        $vanilla.result | Add-Member -NotePropertyName item -NotePropertyValue $vanilla.result.id
        $vanilla.result.PSObject.Properties.Remove('id')
        if ($local.result -is [string]) {
            $local.result = [pscustomobject]@{item = $local.result; count = $local.count}
            $local.PSObject.Properties.Remove('count')
        }
        if (!$local.result.count) { $local.result | Add-Member -NotePropertyName count -NotePropertyValue 1 }
        Compare-Data $local $vanilla "Recipe: $($file.Name)"
    }

    $advancementCount = 0
    foreach ($entry in $zip.Entries | Where-Object { $_.FullName -like 'data/minecraft/advancement/recipes/*/*tuff*.json' }) {
        if (!$expectedRecipes.ContainsKey($entry.Name)) { continue }
        $relative = $entry.FullName.Replace('data/minecraft/advancement/', '')
        $path = Join-Path $modData "advancements/$relative"
        if (!(Test-Path $path)) { $failures.Add("Missing recipe unlock: $relative"); continue }
        $local = (Get-Content $path -Raw).Replace('copperandtuffbackport:', 'minecraft:') | ConvertFrom-Json
        $vanilla = Read-Vanilla $entry.FullName
        foreach ($criterion in $vanilla.criteria.PSObject.Properties) {
            foreach ($predicate in $criterion.Value.conditions.items) {
                if ($predicate.items -is [string]) { $predicate.items = @($predicate.items) }
            }
        }
        Compare-Data $local $vanilla "Recipe unlock: $relative"
        $advancementCount++
    }
    if ($advancementCount -ne $expectedRecipes.Count) { $failures.Add('Incomplete recipe unlock coverage') }

    foreach ($tag in 'mineable/pickaxe', 'slabs', 'stairs', 'walls', 'needs_stone_tool', 'needs_iron_tool', 'needs_diamond_tool') {
        $vanilla = Read-Vanilla "data/minecraft/tags/block/$tag.json"
        $path = Join-Path $resources "data/minecraft/tags/blocks/$tag.json"
        $local = if (Test-Path $path) { (Get-Content $path -Raw).Replace('copperandtuffbackport:', 'minecraft:') | ConvertFrom-Json } else { $null }
        $expected = @($vanilla.values | Where-Object { $_ -is [string] -and $_.Replace('minecraft:', '') -in $names })
        $actual = @($local.values | Where-Object { $_ -is [string] -and $_.Replace('minecraft:', '') -in $names })
        if ((($expected | Sort-Object) -join ',') -cne (($actual | Sort-Object) -join ',')) { $failures.Add("Block tag: $tag") }
    }
    foreach ($tag in 'slabs', 'stairs', 'walls') {
        $vanilla = Read-Vanilla "data/minecraft/tags/item/$tag.json"
        $local = (Get-Content (Join-Path $resources "data/minecraft/tags/items/$tag.json") -Raw).Replace('copperandtuffbackport:', 'minecraft:') | ConvertFrom-Json
        $expected = @($vanilla.values | Where-Object { $_ -is [string] -and $_.Replace('minecraft:', '') -in $names })
        $actual = @($local.values | Where-Object { $_ -is [string] -and $_.Replace('minecraft:', '') -in $names })
        if ((($expected | Sort-Object) -join ',') -cne (($actual | Sort-Object) -join ',')) { $failures.Add("Item tag: $tag") }
    }
    if ($failures.Count) { throw ($failures -join "`n") }
    Write-Output "PASS: 13 added tuff blocks, 13 loot tables, $($expectedRecipes.Count) recipes, $advancementCount recipe unlocks, and mining/shape tags match Java 1.21.1."
} finally {
    $zip.Dispose()
}
