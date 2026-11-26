param(
    [Parameter(Mandatory = $true)][string]$VanillaClientJar,
    [Parameter(Mandatory = $true)][string]$BaseClientJar,
    [Parameter(Mandatory = $true)][string]$NameTagReferenceJar,
    [string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent)
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $VanillaClientJar))
$base = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $BaseClientJar))
$nameTagZip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $NameTagReferenceJar))
$root = Join-Path $ProjectRoot 'src/main/resources'
$names = @('copper_torch', 'copper_wall_torch', 'copper_nugget') + @('copper_lantern', 'copper_chain', 'lightning_rod' | ForEach-Object {
    $family = $_
    foreach ($wax in '', 'waxed_') { foreach ($prefix in '', 'exposed_', 'weathered_', 'oxidized_') {
        $name = "$wax$prefix$family"
        if ($name -ne 'lightning_rod') { $name }
    } }
})
$templates = @('template_chain', 'template_lightning_rod')
function Read-Entry($archive, $name) {
    $entry = $archive.GetEntry($name)
    if (!$entry) { throw "Missing vanilla entry: $name" }
    $reader = [IO.StreamReader]::new($entry.Open())
    try { return $reader.ReadToEnd() } finally { $reader.Dispose() }
}
function Remap($text) {
    foreach ($name in @($names) + @('copper_fire_flame')) {
        $text = $text -replace "minecraft:$name(?=[`"/_])", "copperandtuffbackport:$name"
        $text = $text -replace "minecraft:(block/|item/|blocks/|recipes/[^/]+/)$name(?=[`"/_])", "copperandtuffbackport:`$1$name"
    }
    foreach ($template in $templates) { $text = $text.Replace("minecraft:block/$template", "copperandtuffbackport:block/$template") }
    return $text
}
function Write-Json($relative, $value) {
    $path = Join-Path $root $relative
    [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($path)) | Out-Null
    [IO.File]::WriteAllText($path, ($value | ConvertTo-Json -Depth 60) + "`n")
}
function Ingredient($value) {
    if ($value -is [array]) { return ,@($value | ForEach-Object { Ingredient $_ }) }
    if ($value -eq 'copperandtuffbackport:copper_nugget') { return [pscustomobject]@{tag = 'copperandtuffbackport:copper_nuggets'} }
    if ($value.StartsWith('#')) { return [pscustomobject]@{tag = $value.Substring(1)} }
    return [pscustomobject]@{item = $value}
}
function Import-Recipe($archive, $entry) {
    $recipe = (Remap (Read-Entry $archive $entry.FullName)) | ConvertFrom-Json
    foreach ($key in $recipe.key.PSObject.Properties) { $key.Value = Ingredient $key.Value }
    if ($recipe.ingredients) { $recipe.ingredients = @($recipe.ingredients | ForEach-Object { Ingredient $_ }) }
    $recipe.result | Add-Member -NotePropertyName item -NotePropertyValue $recipe.result.id
    $recipe.result.PSObject.Properties.Remove('id')
    if (!$recipe.result.count) { $recipe.result | Add-Member -NotePropertyName count -NotePropertyValue 1 }
    Write-Json "data/copperandtuffbackport/recipes/$($entry.Name)" $recipe
}
try {
    $recipeNames = @('copper_nugget', 'copper_ingot_from_nuggets', 'copper_torch', 'copper_lantern', 'copper_chain') +
        @($names | Where-Object { $_.StartsWith('waxed_') } | ForEach-Object { "$_`_from_honeycomb" })
    foreach ($entry in $zip.Entries) {
        if ($entry.FullName -match '^assets/minecraft/(blockstates|models/block|models/item)/([^/]+)\.json$') {
            $folder = $matches[1]; $name = $matches[2]
            $selected = $name -in $names -or $name -in $templates
            foreach ($prefix in $names) { if ($name.StartsWith($prefix + '_')) { $selected = $true } }
            if ($selected) {
                $raw = Remap (Read-Entry $zip $entry.FullName)
                # Power/state variants may have an empty variant key, unsupported by Windows PowerShell's JSON object parser.
                $path = Join-Path $root "assets/copperandtuffbackport/$folder/$name.json"
                [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($path)) | Out-Null
                [IO.File]::WriteAllText($path, $raw.TrimEnd() + "`n")
            }
        }
        if ($entry.FullName -match '^assets/minecraft/textures/(block|item|particle)/([^/]+?)\.(png|png\.mcmeta)$') {
            $name = $matches[2]
            if ($name -in $names -or $name -eq 'copper_fire_flame') {
                $path = Join-Path $root $entry.FullName.Replace('assets/minecraft/', 'assets/copperandtuffbackport/')
                [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($path)) | Out-Null
                [IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $path, $true)
            }
        }
        if ($entry.FullName -match '^data/minecraft/recipe/([^/]+)\.json$' -and $matches[1] -in $recipeNames) { Import-Recipe $zip $entry }
        if ($entry.FullName -match '^data/minecraft/loot_table/blocks/([^/]+)\.json$' -and $matches[1] -in $names) {
            $name = $matches[1]
            $loot = (Remap (Read-Entry $zip $entry.FullName)) | ConvertFrom-Json
            $loot.PSObject.Properties.Remove('random_sequence')
            Write-Json "data/copperandtuffbackport/loot_tables/blocks/$name.json" $loot
        }
        if ($entry.FullName -match '^data/minecraft/advancement/recipes/([^/]+)/([^/]+)\.json$' -and $matches[2] -in $recipeNames) {
            $folder = $matches[1]; $name = $matches[2]
            $adv = (Remap (Read-Entry $zip $entry.FullName)) | ConvertFrom-Json
            # The ingot output is vanilla, but the added recipe is in this mod's namespace.
            $adv.rewards.recipes = @("copperandtuffbackport:$name")
            $adv.criteria.has_the_recipe.conditions.recipe = "copperandtuffbackport:$name"
            foreach ($criterion in $adv.criteria.PSObject.Properties) { foreach ($predicate in $criterion.Value.conditions.items) {
                if ($predicate.items -is [string]) { $predicate.items = @($predicate.items) }
                if (@($predicate.items) -contains 'copperandtuffbackport:copper_nugget') {
                    $predicate.PSObject.Properties.Remove('items')
                    $predicate | Add-Member -NotePropertyName tag -NotePropertyValue 'copperandtuffbackport:copper_nuggets'
                }
            } }
            Write-Json "data/copperandtuffbackport/advancements/recipes/$folder/$name.json" $adv
        }
    }
    foreach ($name in 'exposed_lightning_rod', 'weathered_lightning_rod', 'oxidized_lightning_rod') {
        Write-Json "assets/copperandtuffbackport/models/item/$name.json" @{parent = "copperandtuffbackport:block/$name"}
    }
    foreach ($name in $names | Where-Object { $_.StartsWith('waxed_') }) {
        $parent = if ($name -eq 'waxed_lightning_rod') { 'minecraft:item/lightning_rod' } else { "copperandtuffbackport:item/$($name.Substring(6))" }
        Write-Json "assets/copperandtuffbackport/models/item/$name.json" @{parent = $parent}
    }
    Write-Json 'assets/copperandtuffbackport/particles/copper_fire_flame.json' @{textures = @('copperandtuffbackport:copper_fire_flame')}
    Import-Recipe $nameTagZip ($nameTagZip.GetEntry('data/minecraft/recipe/name_tag.json'))
    # Keep both conventions independent; the private union avoids introducing cycles between shared tags.
    foreach ($tag in 'copper_nuggets', 'nuggets/copper') {
        Write-Json "data/c/tags/items/$tag.json" @{replace = $false; values = @('copperandtuffbackport:copper_nugget')}
    }
    Write-Json 'data/copperandtuffbackport/tags/items/copper_nuggets.json' @{replace = $false; values = @('#c:copper_nuggets', '#c:nuggets/copper', '#forge:nuggets/copper')}
    Write-Json 'data/forge/tags/items/nuggets/copper.json' @{replace = $false; values = @('copperandtuffbackport:copper_nugget')}
    Write-Json 'data/forge/tags/items/nuggets.json' @{replace = $false; values = @('#forge:nuggets/copper')}
    Write-Json 'data/c/tags/items/nuggets.json' @{replace = $false; values = @('#copperandtuffbackport:copper_nuggets')}
    Write-Json 'data/minecraft/tags/items/metal_nuggets.json' @{replace = $false; values = @('minecraft:iron_nugget', 'minecraft:gold_nugget', '#copperandtuffbackport:copper_nuggets')}
    Write-Json 'data/copperandtuffbackport/advancements/recipes/tools/name_tag.json' @{
        parent = 'minecraft:recipes/root'
        criteria = @{has_paper = @{trigger = 'minecraft:inventory_changed'; conditions = @{items = @(@{items = @('minecraft:paper')})}}
            has_the_recipe = @{trigger = 'minecraft:recipe_unlocked'; conditions = @{recipe = 'copperandtuffbackport:name_tag'}}}
        requirements = @(,@('has_paper', 'has_the_recipe'))
        rewards = @{recipes = @('copperandtuffbackport:name_tag')}
    }
    $known = @{blocks = @{}; items = @{}}
    foreach ($entry in $base.Entries) {
        if ($entry.FullName -match '^assets/minecraft/blockstates/([^/]+)\.json$') { $known.blocks[$matches[1]] = $true }
        if ($entry.FullName -match '^assets/minecraft/models/item/([^/]+)\.json$') { $known.items[$matches[1]] = $true }
    }
    $modNames = @{blocks = @{}; items = @{}}
    foreach ($kind in 'blocks', 'items') {
        $folder = if ($kind -eq 'blocks') { 'blockstates' } else { 'models/item' }
        foreach ($file in Get-ChildItem (Join-Path $root "assets/copperandtuffbackport/$folder") -Filter '*.json') { $modNames[$kind][$file.BaseName] = $true }
    }
    $groups = @('chains', 'lanterns', 'lightning_rods')
    foreach ($entry in $zip.Entries) {
        if ($entry.FullName -notmatch '^data/minecraft/tags/(block|item)/(.+)\.json$') { continue }
        $kind = $matches[1] + 's'; $tag = $matches[2]
        $obj = Read-Entry $zip $entry.FullName | ConvertFrom-Json
        $selected = @($obj.values | Where-Object { $_ -is [string] -and ($_.Replace('minecraft:', '') -in $names -or $_.Replace('#minecraft:', '') -in $groups) })
        if (!$selected.Count -and $tag -notin $groups) { continue }
        $path = Join-Path $root "data/minecraft/tags/$kind/$tag.json"
        $existing = if (Test-Path $path) { (Get-Content $path -Raw | ConvertFrom-Json).values } else { @() }
        $values = if ($tag -in $groups) { $obj.values } else { $selected }
        $added = @($values | ForEach-Object {
            if ($_ -is [string]) {
                $value = $_.Replace('minecraft:iron_chain', 'minecraft:chain')
                $id = $value.Replace('minecraft:', '')
                if ($value.StartsWith('#')) { $value }
                elseif ($modNames[$kind].ContainsKey($id)) { "copperandtuffbackport:$id" }
                elseif ($known[$kind].ContainsKey($id)) { $value }
            }
        })
        Write-Json "data/minecraft/tags/$kind/$tag.json" @{replace = $false; values = @(@($existing) + $added | Sort-Object -Unique)}
    }
    $langPath = Join-Path $root 'assets/copperandtuffbackport/lang/en_us.json'
    $language = Get-Content $langPath -Raw | ConvertFrom-Json
    $vanillaLanguage = Read-Entry $zip 'assets/minecraft/lang/en_us.json' | ConvertFrom-Json
    foreach ($name in $names) {
        $kind = if ($name -eq 'copper_nugget') { 'item' } else { 'block' }
        if ($vanillaLanguage."$kind.minecraft.$name") { $language | Add-Member -NotePropertyName "$kind.copperandtuffbackport.$name" -NotePropertyValue $vanillaLanguage."$kind.minecraft.$name" -Force }
    }
    Write-Json 'assets/copperandtuffbackport/lang/en_us.json' $language
    Write-Output "Imported $($names.Count) selected decoration/nugget identifiers, $($recipeNames.Count) 1.21.9 recipes, and the requested later name-tag recipe."
} finally { $zip.Dispose(); $base.Dispose(); $nameTagZip.Dispose() }
