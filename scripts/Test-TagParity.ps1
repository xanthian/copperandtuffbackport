param(
    [Parameter(Mandatory = $true)][string]$Vanilla121Jar,
    [Parameter(Mandatory = $true)][string]$Vanilla1219Jar,
    [Parameter(Mandatory = $true)][string]$Vanilla1201Jar,
    [string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent)
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$resources = Join-Path $ProjectRoot 'src/main/resources'
$names = @(Get-ChildItem (Join-Path $resources 'assets/copperandtuffbackport/blockstates') -Filter '*.json' |
    ForEach-Object { "minecraft:$($_.BaseName)" })
function Load-Tags($path) {
    $zip = [IO.Compression.ZipFile]::OpenRead((Resolve-Path $path))
    $map = @{}
    try {
        foreach ($entry in $zip.Entries) {
            if ($entry.FullName -notmatch '^data/minecraft/tags/(blocks?|items?)/(.+)\.json$') { continue }
            $kind = if ($matches[1].StartsWith('block')) { 'blocks' } else { 'items' }
            $key = "$kind/$($matches[2])"
            $reader = [IO.StreamReader]::new($entry.Open())
            try { $json = $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
            $map[$key] = @($json.values | ForEach-Object { if ($_ -is [string]) { $_ } else { $_.id } })
        }
    } finally { $zip.Dispose() }
    return $map
}
function Resolve-Tags($map) {
    $sets = @{}
    foreach ($key in $map.Keys) {
        $sets[$key] = [Collections.Generic.HashSet[string]]::new()
        foreach ($value in $map[$key]) { if ($value -in $names) { [void]$sets[$key].Add($value) } }
    }
    do {
        $changed = $false
        foreach ($key in $map.Keys) {
            $kind = $key.Split('/')[0]
            foreach ($value in $map[$key]) {
                if ($value -notlike '#minecraft:*') { continue }
                $target = "$kind/$($value.Substring(11))"
                if (!$sets.ContainsKey($target)) { continue }
                $before = $sets[$key].Count
                $sets[$key].UnionWith($sets[$target])
                if ($sets[$key].Count -ne $before) { $changed = $true }
            }
        }
    } while ($changed)
    return $sets
}
$old = Resolve-Tags (Load-Tags $Vanilla121Jar)
$new = Resolve-Tags (Load-Tags $Vanilla1219Jar)
$actualMap = Load-Tags $Vanilla1201Jar
foreach ($file in Get-ChildItem (Join-Path $resources 'data/minecraft/tags') -Recurse -Filter '*.json') {
    $key = $file.FullName.Substring((Join-Path $resources 'data/minecraft/tags').Length + 1).Replace('\', '/').Replace('.json', '')
    $json = (Get-Content $file.FullName -Raw).Replace('copperandtuffbackport:', 'minecraft:') | ConvertFrom-Json
    $values = @($json.values | ForEach-Object { if ($_ -is [string]) { $_ } else { $_.id } })
    $actualMap[$key] = if ($json.replace) { $values } else { @($actualMap[$key]) + $values }
}
$actual = Resolve-Tags $actualMap
$issues = [Collections.Generic.List[string]]::new()
$keys = @(@($old.Keys) + @($new.Keys) + @($actual.Keys) | Sort-Object -Unique)
foreach ($name in $names) {
    $expected = if ($name -match 'copper_(bars|chest|lantern|chain|(?:wall_)?torch)$|lightning_rod$') { $new } else { $old }
    foreach ($key in $keys) {
        $want = $expected.ContainsKey($key) -and $expected[$key].Contains($name)
        $have = $actual.ContainsKey($key) -and $actual[$key].Contains($name)
        if ($want -ne $have) { $issues.Add("$name : $key expected=$want actual=$have") }
    }
}
if ($issues.Count) { $issues; throw "$($issues.Count) tag membership mismatches" }
Write-Output "PASS: all direct and inherited block/item tag memberships match for $($names.Count) backported blocks."
