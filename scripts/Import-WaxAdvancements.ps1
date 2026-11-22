param(
    [Parameter(Mandatory = $true)][string]$BaseClientJar,
    [string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent)
)
# Preserve the 1.20.1 advancement schema and presentation; extend only block eligibility.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $BaseClientJar))
try {
    $dataRoot = Join-Path $ProjectRoot 'src/main/resources/data'
    $copper = @(Get-ChildItem (Join-Path $dataRoot 'copperandtuffbackport/loot_tables/blocks') -Filter '*.json' | Where-Object { $_.BaseName -match 'copper|lightning_rod' })
    foreach ($name in 'wax_on', 'wax_off') {
        $reader = [IO.StreamReader]::new($zip.GetEntry("data/minecraft/advancements/husbandry/$name.json").Open())
        try { $advancement = $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
        $waxed = $name -eq 'wax_off'
        $added = @($copper | Where-Object { $_.BaseName.StartsWith('waxed_') -eq $waxed } |
            ForEach-Object { "copperandtuffbackport:$($_.BaseName)" })
        $predicate = $advancement.criteria.$name.conditions.location[0].predicate.block
        $predicate.blocks = @($predicate.blocks) + $added
        if (!$waxed) { $predicate.blocks += 'minecraft:lightning_rod' }
        $path = Join-Path $dataRoot "minecraft/advancements/husbandry/$name.json"
        [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($path)) | Out-Null
        [IO.File]::WriteAllText($path, ($advancement | ConvertTo-Json -Depth 40) + "`n")
    }
    Write-Output 'Extended Wax On/Wax Off to all backported waxable blocks and the vanilla lightning rod.'
} finally { $zip.Dispose() }
