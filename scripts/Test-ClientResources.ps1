param(
    [Parameter(Mandatory = $true)][string]$Vanilla121Jar,
    [Parameter(Mandatory = $true)][string]$Vanilla1219Jar,
    [Parameter(Mandatory = $true)][string]$Vanilla1201Jar,
    [string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent)
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$refs = @{}
$refs.old = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $Vanilla121Jar))
$refs.age = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $Vanilla1219Jar))
$refs.base = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $Vanilla1201Jar))
$assets = Join-Path $ProjectRoot 'src/main/resources/assets/copperandtuffbackport'
$issues = [Collections.Generic.List[string]]::new()
function Read-Entry($zip, $name) {
    $entry = $zip.GetEntry($name)
    if (!$entry) { return $null }
    $reader = [IO.StreamReader]::new($entry.Open())
    try { return $reader.ReadToEnd() } finally { $reader.Dispose() }
}
function Parse($text) { return ($text -replace '""\s*:', '"__empty":') | ConvertFrom-Json }
function Canonical($value) {
    if ($null -eq $value) { return 'null' }
    if ($value -is [Management.Automation.PSCustomObject]) {
        $parts = foreach ($p in ($value.PSObject.Properties | Sort-Object Name)) { ($p.Name | ConvertTo-Json -Compress) + ':' + (Canonical $p.Value) }
        return '{' + ($parts -join ',') + '}'
    }
    if ($value -is [array]) { return '[' + (($value | ForEach-Object { Canonical $_ }) -join ',') + ']' }
    return ConvertTo-Json -InputObject $value -Compress
}
try {
    $models = 0
    foreach ($file in Get-ChildItem $assets -Recurse -Filter '*.json') {
        $relative = $file.FullName.Substring($assets.Length + 1).Replace('\', '/')
        if ($relative -notmatch '^(blockstates|models)/') { continue }
        $new = $relative -match 'copper_(bars|chest|lantern|chain|(?:wall_)?torch|nugget)|lightning_rod|template_(bars_|chain|lightning_rod)'
        $reference = if ($new) { $refs.age } else { $refs.old }
        $raw = (Get-Content $file.FullName -Raw).Replace('copperandtuffbackport:', 'minecraft:')
        $original = Get-Content $file.FullName -Raw
        foreach ($match in [regex]::Matches($original, '"(?:parent|model)"\s*:\s*"([^":]+):([^"]+)"')) {
            $namespace = $match.Groups[1].Value; $model = $match.Groups[2].Value
            if ($namespace -eq 'copperandtuffbackport' -and !(Test-Path (Join-Path $assets "models/$model.json"))) {
                $issues.Add("Missing model reference: $relative -> $model")
            } elseif ($namespace -eq 'minecraft' -and !$refs.base.GetEntry("assets/minecraft/models/$model.json")) {
                $issues.Add("Model unavailable in 1.20.1: $relative -> $model")
            }
        }
        $parsed = Parse $original
        foreach ($texture in $parsed.textures.PSObject.Properties) {
            if ($texture.Value -isnot [string] -or $texture.Value.StartsWith('#')) { continue }
            $parts = $texture.Value.Split(':')
            $namespace = if ($parts.Length -eq 2) { $parts[0] } else { 'minecraft' }
            $path = $parts[-1]
            if ($namespace -eq 'copperandtuffbackport' -and !(Test-Path (Join-Path $assets "textures/$path.png"))) {
                $issues.Add("Missing texture reference: $relative -> $path")
            } elseif ($namespace -eq 'minecraft' -and !$refs.base.GetEntry("assets/minecraft/textures/$path.png")) {
                $issues.Add("Texture unavailable in 1.20.1: $relative -> $path")
            }
        }
        $expected = Read-Entry $reference "assets/minecraft/$relative"
        if ($relative -match '^models/item/waxed_.*(?:copper_(bars|chest|lantern|chain)|lightning_rod)\.json$') {
            $parent = 'minecraft:item/' + $file.BaseName.Substring(6)
            if ((Parse $raw).parent -ne $parent) { $issues.Add("Wrong waxed item alias: $relative") }
        } elseif ($relative -match '^models/item/(exposed|weathered|oxidized)_lightning_rod\.json$') {
            if ((Parse $raw).parent -ne ('minecraft:block/' + $file.BaseName)) { $issues.Add("Wrong rod item model: $relative") }
        } elseif ($relative -match '^models/item/.*copper_chest\.json$') {
            $template = Read-Entry $refs.age 'assets/minecraft/models/item/template_chest.json'
            $templateModel = Parse $template
            $templateModel | Add-Member -NotePropertyName parent -NotePropertyValue 'builtin/entity'
            if ((Canonical (Parse $raw)) -ne (Canonical $templateModel)) {
                $issues.Add("Chest item transforms differ from 1.21.9 template: $relative")
            }
        } elseif (!$expected) { $issues.Add("Missing vanilla counterpart: $relative") }
        elseif ((Canonical (Parse $raw)) -ne (Canonical (Parse $expected))) { $issues.Add("JSON mismatch: $relative") }
        $models++
    }
    $oldLanguage = Read-Entry $refs.old 'assets/minecraft/lang/en_us.json' | ConvertFrom-Json
    $newLanguage = Read-Entry $refs.age 'assets/minecraft/lang/en_us.json' | ConvertFrom-Json
    $localLanguage = Get-Content (Join-Path $assets 'lang/en_us.json') -Raw | ConvertFrom-Json
    foreach ($property in $localLanguage.PSObject.Properties) {
        $key = $property.Name.Replace('copperandtuffbackport.', 'minecraft.')
        $language = if ($key -match 'copper_(bars|chest|lantern|chain|(?:wall_)?torch|nugget)|lightning_rod') { $newLanguage } else { $oldLanguage }
        if ($property.Value -cne $language.$key) { $issues.Add("Language mismatch: $($property.Name): '$($property.Value)' vs '$($language.$key)'") }
    }
    $oldInfo = Get-Content (Join-Path (Split-Path (Resolve-Path $Vanilla121Jar)) 'mojang_minecraft_info.json') -Raw | ConvertFrom-Json
    $newInfo = Get-Content (Join-Path (Split-Path (Resolve-Path $Vanilla1219Jar)) 'mojang_minecraft_info.json') -Raw | ConvertFrom-Json
    $indexes = @{}
    $soundRefs = @{}
    foreach ($pair in @(@('old', $oldInfo), @('age', $newInfo))) {
        $index = Invoke-RestMethod $pair[1].assetIndex.url
        $indexes[$pair[0]] = $index
        $hash = $index.objects.'minecraft/sounds.json'.hash
        $soundRefs[$pair[0]] = Invoke-RestMethod "https://resources.download.minecraft.net/$($hash.Substring(0,2))/$hash"
    }
    $sounds = Get-Content (Join-Path $assets 'sounds.json') -Raw | ConvertFrom-Json
    $usedAudio = [Collections.Generic.HashSet[string]]::new()
    foreach ($event in $sounds.PSObject.Properties) {
        $version = if ($event.Name -match 'copper_chest') { 'age' } else { 'old' }
        $normalized = ($event.Value | ConvertTo-Json -Depth 30).Replace('copperandtuffbackport:', 'block/').Replace('block/block/', 'block/').Replace('block/copper_bulb/turn_on', 'block/copper_bulb/toggle') | ConvertFrom-Json
        if ((Canonical $normalized) -ne (Canonical $soundRefs[$version].($event.Name))) { $issues.Add("Sound definition mismatch: $($event.Name)") }
        foreach ($sound in $event.Value.sounds) {
            $name = if ($sound -is [string]) { $sound } else { $sound.name }
            if (!$name.StartsWith('copperandtuffbackport:')) { continue }
            $path = $name.Replace('copperandtuffbackport:', '') + '.ogg'
            [void]$usedAudio.Add($path)
            $official = ('block/' + $path).Replace('block/block/', 'block/').Replace('block/copper_bulb/turn_on.ogg', 'block/copper_bulb/toggle.ogg')
            $hash = $indexes[$version].objects."minecraft/sounds/$official".hash
            $file = Join-Path $assets "sounds/$path"
            if (!$hash -or !(Test-Path $file) -or (Get-FileHash $file -Algorithm SHA1).Hash -ne $hash) { $issues.Add("Audio mismatch: $path") }
        }
    }
    foreach ($file in Get-ChildItem (Join-Path $assets 'sounds') -Recurse -Filter '*.ogg') {
        $relative = $file.FullName.Substring((Join-Path $assets 'sounds').Length + 1).Replace('\', '/')
        if (!$usedAudio.Contains($relative)) { $issues.Add("Unreferenced audio: $relative") }
    }
    Write-Output "Checked $models blockstate/model files, $(@($localLanguage.PSObject.Properties).Count) English language entries, $(@($sounds.PSObject.Properties).Count) sound events, and $($usedAudio.Count) referenced audio files."
    if ($issues.Count) { $issues; throw "$($issues.Count) client resource mismatches found." }
    Write-Output 'PASS: client resources match their release references, including the required legacy item aliases.'
} finally { foreach ($zip in $refs.Values) { $zip.Dispose() } }
