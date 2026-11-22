param(
    [Parameter(Mandatory = $true)][string]$VanillaClientJar,
    [Parameter(Mandatory = $true)][string]$VanillaVersionJson,
    [string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent)
)

# Import the selected 1.21.9 assets and translate data to the 1.20.1 formats.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $VanillaClientJar))
$resourceRoot = Join-Path $ProjectRoot 'src/main/resources'
$modAssets = 'assets/copperandtuffbackport'
$modData = 'data/copperandtuffbackport'
$names = @('copper_bars', 'copper_chest' | ForEach-Object {
    $family = $_
    foreach ($wax in '', 'waxed_') { foreach ($prefix in '', 'exposed_', 'weathered_', 'oxidized_') { "$wax$prefix$family" } }
})
function Read-Entry($name) {
    $entry = $zip.GetEntry($name)
    if (!$entry) { throw "Missing vanilla asset: $name" }
    $reader = [IO.StreamReader]::new($entry.Open())
    try { return $reader.ReadToEnd() } finally { $reader.Dispose() }
}
function Remap($text) {
    # Keep all references to existing vanilla content intact.
    foreach ($name in $names) {
        $text = $text -replace "minecraft:$name(?=[`"/_])", "copperandtuffbackport:$name"
        $text = $text -replace "minecraft:(block/|item/|blocks/|recipes/[^/]+/)$name(?=[`"/_])", "copperandtuffbackport:`$1$name"
    }
    $text = $text.Replace('minecraft:block/template_bars_', 'copperandtuffbackport:block/template_bars_')
    return $text
}
function Write-Resource($relative, $text) {
    $path = Join-Path $resourceRoot $relative
    [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($path)) | Out-Null
    [IO.File]::WriteAllText($path, $text.TrimEnd() + "`n")
}
function Write-Json($relative, $value) { Write-Resource $relative ($value | ConvertTo-Json -Depth 60) }

try {
    foreach ($entry in $zip.Entries) {
        if ($entry.FullName -match '^assets/minecraft/(blockstates|models/block|models/item)/([^/]+)\.json$') {
            $folder = $matches[1]; $name = $matches[2]
            if ($name -in $names -or $name -match '^(exposed_|weathered_|oxidized_)?copper_bars_' -or $name -like 'template_bars_*') {
                if ($folder -eq 'models/item' -and $name -like '*copper_chest') {
                    # 1.20.1 uses the builtin entity renderer instead of item model definitions.
                    $template = Read-Entry 'assets/minecraft/models/item/template_chest.json' | ConvertFrom-Json
                    $template | Add-Member -NotePropertyName parent -NotePropertyValue 'builtin/entity'
                    Write-Json "$modAssets/$folder/$name.json" $template
                } else { Write-Resource "$modAssets/$folder/$name.json" (Remap (Read-Entry $entry.FullName)) }
            }
        }
        if ($entry.FullName -match '^assets/minecraft/textures/(block/((exposed_|weathered_|oxidized_)?copper_bars)\.png|entity/chest/copper[^/]*\.png)$') {
            $target = Join-Path $resourceRoot ($entry.FullName.Replace('assets/minecraft/', "$modAssets/"))
            [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($target)) | Out-Null
            [IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $target, $true)
        }
        if ($entry.FullName -match '^data/minecraft/recipe/([^/]+)\.json$' -and [regex]::IsMatch($matches[1], 'copper_(?:bars|chest)(?:_|$)')) {
            $name = $matches[1]
            $recipe = (Remap (Read-Entry $entry.FullName)) | ConvertFrom-Json
            if ($recipe.key) {
                foreach ($key in $recipe.key.PSObject.Properties) { $key.Value = [pscustomobject]@{item = $key.Value} }
            }
            if ($recipe.ingredients) { $recipe.ingredients = @($recipe.ingredients | ForEach-Object { [pscustomobject]@{item = $_} }) }
            $recipe.result | Add-Member -NotePropertyName item -NotePropertyValue $recipe.result.id
            $recipe.result.PSObject.Properties.Remove('id')
            Write-Json "$modData/recipes/$name.json" $recipe
        }
        if ($entry.FullName -match '^data/minecraft/loot_table/blocks/([^/]+)\.json$' -and $matches[1] -in $names) {
            $name = $matches[1]
            $loot = (Remap (Read-Entry $entry.FullName)) | ConvertFrom-Json
            $loot.PSObject.Properties.Remove('random_sequence')
            foreach ($pool in $loot.pools) { foreach ($item in $pool.entries) { if ($item.functions) {
                $item.functions = @($item.functions | ForEach-Object {
                    if ($_.function -eq 'minecraft:copy_components') { [pscustomobject]@{function = 'minecraft:copy_name'; source = 'block_entity'} } else { $_ }
                })
            } } }
            Write-Json "$modData/loot_tables/blocks/$name.json" $loot
        }
        if ($entry.FullName -match '^data/minecraft/advancement/recipes/([^/]+/[^/]+)\.json$' -and [regex]::IsMatch($matches[1], 'copper_(?:bars|chest)(?:_|$)')) {
            $relative = $matches[1]
            $advancement = (Remap (Read-Entry $entry.FullName)) | ConvertFrom-Json
            foreach ($criterion in $advancement.criteria.PSObject.Properties) { foreach ($predicate in $criterion.Value.conditions.items) {
                if ($predicate.items -is [string]) { $predicate.items = @($predicate.items) }
            } }
            Write-Json "$modData/advancements/recipes/$relative.json" $advancement
        }
    }
    # 1.21.9 aliases waxed items in its new item-definition format. Backport the aliases as models.
    foreach ($name in $names | Where-Object { $_ -like 'waxed_*' }) {
        Write-Json "$modAssets/models/item/$name.json" @{parent = "copperandtuffbackport:item/$($name.Substring(6))"}
    }
    foreach ($kind in 'blocks', 'items') {
        Write-Json "data/minecraft/tags/$kind/copper_chests.json" @{replace = $false; values = @($names | Where-Object {$_ -like '*chest'} | ForEach-Object {"copperandtuffbackport:$_"})}
        Write-Json "data/minecraft/tags/$kind/bars.json" @{replace = $false; values = @('minecraft:iron_bars') + @($names | Where-Object {$_ -like '*bars'} | ForEach-Object {"copperandtuffbackport:$_"})}
    }
    Write-Json 'data/minecraft/tags/blocks/guarded_by_piglins.json' @{replace = $false; values = @('#minecraft:copper_chests')}
    foreach ($tag in 'mineable/pickaxe', 'needs_stone_tool') {
        $path = "data/minecraft/tags/blocks/$tag.json"
        $existing = Get-Content (Join-Path $resourceRoot $path) -Raw | ConvertFrom-Json
        $added = if ($tag -eq 'needs_stone_tool') { @('#minecraft:copper_chests') } else { @($names | ForEach-Object {"copperandtuffbackport:$_"}) }
        $existing.values = @($existing.values) + @($added | Where-Object {$_ -notin $existing.values})
        Write-Json $path $existing
    }
    $language = Get-Content (Join-Path $resourceRoot "$modAssets/lang/en_us.json") -Raw | ConvertFrom-Json
    $vanillaLanguage = Read-Entry 'assets/minecraft/lang/en_us.json' | ConvertFrom-Json
    foreach ($name in $names) { $language | Add-Member -NotePropertyName "block.copperandtuffbackport.$name" -NotePropertyValue $vanillaLanguage."block.minecraft.$name" -Force }

    $version = Get-Content $VanillaVersionJson -Raw | ConvertFrom-Json
    $index = Invoke-RestMethod $version.assetIndex.url
    $asset = $index.objects.'minecraft/sounds.json'.hash
    $vanillaSounds = Invoke-RestMethod "https://resources.download.minecraft.net/$($asset.Substring(0,2))/$asset"
    $sounds = Get-Content (Join-Path $resourceRoot "$modAssets/sounds.json") -Raw | ConvertFrom-Json
    foreach ($event in $vanillaSounds.PSObject.Properties | Where-Object { $_.Name -match '^block\.copper_chest(_weathered|_oxidized)?\.(open|close)$' }) {
        foreach ($sound in $event.Value.sounds) {
            $name = if ($sound -is [string]) { $sound } else { $sound.name }
            $name = $name.Replace('minecraft:', '')
            $hash = $index.objects."minecraft/sounds/$name.ogg".hash
            if (!$hash) { throw "Missing sound asset: $name" }
            $target = Join-Path $resourceRoot "$modAssets/sounds/$name.ogg"
            [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($target)) | Out-Null
            Invoke-WebRequest -UseBasicParsing "https://resources.download.minecraft.net/$($hash.Substring(0,2))/$hash" -OutFile $target
            if ((Get-FileHash $target -Algorithm SHA1).Hash -ne $hash) { throw "Sound checksum mismatch: $name" }
        }
        $definition = ($event.Value | ConvertTo-Json -Depth 30).Replace('"block/', '"copperandtuffbackport:block/') | ConvertFrom-Json
        $sounds | Add-Member -NotePropertyName $event.Name -NotePropertyValue $definition -Force
        $subtitle = $event.Value.subtitle
        if ($subtitle) { $language | Add-Member -NotePropertyName $subtitle -NotePropertyValue $vanillaLanguage.$subtitle -Force }
    }
    Write-Json "$modAssets/sounds.json" $sounds
    Write-Json "$modAssets/lang/en_us.json" $language
    Write-Output 'Imported all 16 copper bars/chest variants and their 1.21.9 recipes, loot, models, textures, sounds, and tags.'
} finally { $zip.Dispose() }
