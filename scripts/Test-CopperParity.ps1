param(
    [Parameter(Mandatory = $true)][string]$VanillaClientJar,
    [string]$BaseClientJar,
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
    $names = @('chiseled_copper', 'copper_door', 'copper_trapdoor', 'copper_grate', 'copper_bulb' | ForEach-Object {
        $family = $_
        foreach ($wax in '', 'waxed_') {
            foreach ($prefix in '', 'exposed_', 'weathered_', 'oxidized_') { "$wax$prefix$family" }
        }
    })
    $registered = @(Select-String -Path (Join-Path $ProjectRoot 'src/main/java/xanthian/copperandtuffbackport/blocks/ModCopperBlocks.java') `
        -Pattern 'registerBlock\("([^"]+)"' | ForEach-Object { $_.Matches[0].Groups[1].Value })
    $vanillaNames = @($zip.Entries | Where-Object { $_.FullName -match '^assets/minecraft/blockstates/[^/]*(chiseled_copper|copper_door|copper_trapdoor|copper_grate|copper_bulb)\.json$' } |
        ForEach-Object { [IO.Path]::GetFileNameWithoutExtension($_.Name) })
    if (Compare-Object $names $vanillaNames) { $failures.Add('Missing or extra vanilla copper block variants') }
    if (Compare-Object $names $registered) { $failures.Add('Copper block registration set') }

    foreach ($name in $names) {
        $local = (Get-Content (Join-Path $modData "loot_tables/blocks/$name.json") -Raw).Replace('copperandtuffbackport:', 'minecraft:') | ConvertFrom-Json
        $vanilla = Read-Vanilla "data/minecraft/loot_table/blocks/$name.json"
        # Random loot sequence identifiers were introduced after 1.20.1.
        $local.PSObject.Properties.Remove('random_sequence')
        $vanilla.PSObject.Properties.Remove('random_sequence')
        Compare-Data $local $vanilla "Loot table: $name"
    }

    $expectedRecipes = @{}
    foreach ($entry in $zip.Entries | Where-Object { $_.FullName -like 'data/minecraft/recipe/*copper*.json' }) {
        $recipe = Read-Vanilla $entry.FullName
        if ($recipe.result.id -and $recipe.result.id.Replace('minecraft:', '') -in $names) {
            $expectedRecipes[$entry.Name] = $recipe
        }
    }
    # The two Copper Age families have a separate 1.21.9 reference target.
    $localRecipes = @(Get-ChildItem (Join-Path $modData 'recipes') -Filter '*copper*.json' |
        Where-Object {
            $recipe = Get-Content $_.FullName -Raw | ConvertFrom-Json
            $output = if ($recipe.result -is [string]) { $recipe.result } else { $recipe.result.item }
            $output -and $output.Replace('copperandtuffbackport:', '') -in $names
        })
    if (Compare-Object @($expectedRecipes.Keys) @($localRecipes.Name)) { $failures.Add('Missing or extra copper recipes') }
    foreach ($file in $localRecipes) {
        if (!$expectedRecipes.ContainsKey($file.Name)) { continue }
        $local = (Get-Content $file.FullName -Raw).Replace('copperandtuffbackport:', 'minecraft:') | ConvertFrom-Json
        $vanilla = $expectedRecipes[$file.Name]
        # Copper bars occupy the old trapdoor pattern. Use vanilla 1.21.9's
        # four-ingot/one-trapdoor recipe as the sole 1.21.1 recipe exception.
        if ($file.Name -eq 'copper_trapdoor.json') {
            $vanilla.pattern = @('##', '##')
            $vanilla.result.count = 1
        }
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
    foreach ($entry in $zip.Entries | Where-Object { $_.FullName -like 'data/minecraft/advancement/recipes/*/*copper*.json' }) {
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

    foreach ($tag in 'mineable/pickaxe', 'doors', 'trapdoors', 'mob_interactable_doors', 'needs_stone_tool', 'needs_iron_tool', 'needs_diamond_tool') {
        $vanilla = Read-Vanilla "data/minecraft/tags/block/$tag.json"
        $path = Join-Path $resources "data/minecraft/tags/blocks/$tag.json"
        $local = if (Test-Path $path) { (Get-Content $path -Raw).Replace('copperandtuffbackport:', 'minecraft:') | ConvertFrom-Json } else { $null }
        $expected = @($vanilla.values | Where-Object { $_ -is [string] -and $_.Replace('minecraft:', '') -in $names })
        $actual = @($local.values | Where-Object { $_ -is [string] -and $_.Replace('minecraft:', '') -in $names })
        if ((($expected | Sort-Object) -join ',') -cne (($actual | Sort-Object) -join ',')) { $failures.Add("Block tag: $tag") }
    }
    foreach ($tag in 'doors', 'trapdoors') {
        $vanilla = Read-Vanilla "data/minecraft/tags/item/$tag.json"
        $local = (Get-Content (Join-Path $resources "data/minecraft/tags/items/$tag.json") -Raw).Replace('copperandtuffbackport:', 'minecraft:') | ConvertFrom-Json
        $expected = @($vanilla.values | Where-Object { $_ -is [string] -and $_.Replace('minecraft:', '') -in $names })
        $actual = @($local.values | Where-Object { $_ -is [string] -and $_.Replace('minecraft:', '') -in $names })
        if ((($expected | Sort-Object) -join ',') -cne (($actual | Sort-Object) -join ',')) { $failures.Add("Item tag: $tag") }
    }
    if ($failures.Count) { throw ($failures -join "`n") }
    Write-Output "PASS: 40 added copper blocks, 40 loot tables, $($expectedRecipes.Count) recipes, $advancementCount recipe unlocks, and mining/shape tags match Java 1.21.1, with the intentional 1.21.9 trapdoor recipe compatibility exception."
    if ($BaseClientJar) {
        $baseZip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $BaseClientJar))
        try {
            $baseNames = @('copper_block', 'cut_copper', 'cut_copper_slab', 'cut_copper_stairs' | ForEach-Object {
                $family = $_
                foreach ($wax in '', 'waxed_') {
                    foreach ($prefix in '', 'exposed_', 'weathered_', 'oxidized_') {
                        $suffix = if ($family -eq 'copper_block' -and $prefix) { 'copper' } else { $family }
                        "$wax$prefix$suffix"
                    }
                }
            })
            foreach ($name in $baseNames) {
                $entry = $baseZip.GetEntry("data/minecraft/loot_tables/blocks/$name.json")
                $reader = [IO.StreamReader]::new($entry.Open())
                try { $local = $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
                $vanilla = Read-Vanilla "data/minecraft/loot_table/blocks/$name.json"
                $local.PSObject.Properties.Remove('random_sequence')
                $vanilla.PSObject.Properties.Remove('random_sequence')
                Compare-Data $local $vanilla "Existing copper loot: $name"
            }
            $baseRecipes = @{}
            foreach ($entry in $baseZip.Entries | Where-Object { $_.FullName -like 'data/minecraft/recipes/*copper*.json' }) {
                $reader = [IO.StreamReader]::new($entry.Open())
                try { $local = $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
                $result = if ($local.result -is [string]) { $local.result } else { $local.result.item }
                if ($result -and $result.Replace('minecraft:', '') -in $baseNames) { $baseRecipes[$entry.Name] = $local }
            }
            $baseExpected = @{}
            foreach ($entry in $zip.Entries | Where-Object { $_.FullName -like 'data/minecraft/recipe/*copper*.json' }) {
                $recipe = Read-Vanilla $entry.FullName
                if ($recipe.result.id -and $recipe.result.id.Replace('minecraft:', '') -in $baseNames) { $baseExpected[$entry.Name] = $recipe }
            }
            if (Compare-Object @($baseRecipes.Keys) @($baseExpected.Keys)) { $failures.Add('Existing copper recipe set') }
            foreach ($name in $baseRecipes.Keys) {
                if (!$baseExpected.ContainsKey($name)) { continue }
                $local = $baseRecipes[$name]
                $vanilla = $baseExpected[$name]
                $vanilla.result | Add-Member -NotePropertyName item -NotePropertyValue $vanilla.result.id
                $vanilla.result.PSObject.Properties.Remove('id')
                if ($local.result -is [string]) {
                    $local.result = [pscustomobject]@{item = $local.result; count = $local.count}
                    $local.PSObject.Properties.Remove('count')
                }
                if (!$local.result.count) { $local.result | Add-Member -NotePropertyName count -NotePropertyValue 1 }
                # Both versions default recipe notifications to true.
                if ($local.show_notification -eq $true) { $local.PSObject.Properties.Remove('show_notification') }
                if ($vanilla.show_notification -eq $true) { $vanilla.PSObject.Properties.Remove('show_notification') }
                Compare-Data $local $vanilla "Existing copper recipe: $name"
            }
            if ($failures.Count) { throw ($failures -join "`n") }
            Write-Output "PASS: 32 existing copper variants, 32 loot tables, and $($baseExpected.Count) existing recipes match Java 1.21.1."
        } finally { $baseZip.Dispose() }
    }
} finally {
    $zip.Dispose()
}
