# Load the properties from the properties file
$map = @{}

foreach ($line in Get-Content -Path './input.properties') {
    if (-not ($line -match '^#|^$|^[^=]+=[^=]+$')) {
        continue
    }
    $key, $value = $line -split '=', 2
    $map[$key.Trim()] = $value.Trim()
}

$serviceResponse = az deployment group create --resource-group $($map['resourceGroupName']) --template-file ./templates/searchservice.template.json --parameters serviceName=$($map['serviceName']) location=$($map['location'])
$serviceResponse

$resp=az search admin-key show --resource-group $($map['resourceGroupName']) --service-name $($map['serviceName'])
$jsonObject = $resp | ConvertFrom-Json
$primaryKey = $jsonObject.primaryKey

Write-Host "Api Key: $primaryKey"

$dataSrcResources = @("./definition-categories/categories.datasource.json","./definition-orders/orders.datasource.json",'./definition-products/products.datasource.json')
foreach ($dataSrc in $dataSrcResources) {
    $response = Add-SearchDataSource -fileName $dataSrc -SearchServiceName $map['serviceName'] -ApiKey $primaryKey -DBUser $map['DBUser'] -DBPass $($map['DBPass'])
    $response
}

$indexResources = @("./definition-categories/categories.index.json","./definition-orders/orders.index.json",'./definition-products/products.index.json')
foreach ($index in $indexResources) {
    $response = Add-SearchIndex -fileName $index -SearchServiceName $map['serviceName'] -ApiKey $primaryKey
    $response
}

$indexerResources = @("./definition-categories/categories.indexer.json","./definition-orders/orders.indexer.json",'./definition-products/products.indexer.json')
foreach ($indexer in $indexerResources) {
    $response = Add-SearchIndexer -fileName $indexer -SearchServiceName $map['serviceName'] -ApiKey $primaryKey
    $response
}



function Add-SearchDataSource {
    param (
        [string]$fileName,
        [string]$SearchServiceName,
        [string]$ApiVersion = "2023-11-01",
        [string]$ApiKey,
        [string]$DBUser,
        [string]$DBPass
    )

    $uri = "https://$SearchServiceName.search.windows.net/datasources?api-version=$ApiVersion"

    $headers = @{
        "Content-Type" = "application/json"
        "api-key" = $ApiKey
    }

    $jsonPayload=Get-Content -Raw -Path $fileName | ConvertFrom-Json
    $jsonPayload.credentials.connectionString="Server=tcp:vkszndbserver.database.windows.net,1433;Initial Catalog=oms;Persist Security Info=False;User ID=$DBUser;Password=$DBPass;MultipleActiveResultSets=False;Encrypt=True;TrustServerCertificate=False;Connection Timeout=30;"


    $jsonPayload

    $jsonString = $jsonPayload | ConvertTo-Json

    $response = Invoke-RestMethod -Uri $uri -Headers $headers -Body $jsonString -Method Post

    return $response
}

function Add-SearchIndex {
    param (
        [string]$fileName,
        [string]$SearchServiceName,
        [string]$ApiKey,
        [string]$ApiVersion = "2023-11-01"
    )

    $uri = "https://$SearchServiceName.search.windows.net/indexes?api-version=$ApiVersion"

    $headers = @{
        "Content-Type" = "application/json"
        "api-key" = $ApiKey
    }

    $jsonPayload=Get-Content -Raw -Path $fileName | ConvertFrom-Json

    $jsonString = $jsonPayload | ConvertTo-Json

    $response = Invoke-RestMethod -Uri $uri -Headers $headers -Body $jsonString -Method Post

    return $response
}

function Add-SearchIndexer {
    param (
        [string]$fileName,
        [string]$SearchServiceName,
        [string]$ApiKey,
        [string]$ApiVersion = "2023-11-01"
    )

    $uri = "https://$SearchServiceName.search.windows.net/indexers?api-version=$ApiVersion"

    $headers = @{
        "Content-Type" = "application/json"
        "api-key" = $ApiKey
    }

    $jsonPayload=Get-Content -Raw -Path $fileName | ConvertFrom-Json

    $jsonString = $jsonPayload | ConvertTo-Json

    $response = Invoke-RestMethod -Uri $uri -Headers $headers -Body $jsonString -Method Post

    return $response
}