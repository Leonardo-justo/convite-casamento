$ErrorActionPreference = "Stop"

$tenantId = "11111111-1111-1111-1111-111111111111"
$storeId = "22222222-2222-2222-2222-222222222222"
$catalogUrl = "http://localhost:8081"
$edgeUrl = "http://localhost:8082"
$sku = "DEMO-" + (Get-Date -Format "HHmmss")

$createBody = @{
    tenantId = $tenantId
    storeId = $storeId
    sku = $sku
    name = "Combo Kafka"
    price = 29.90
    active = $true
} | ConvertTo-Json

Write-Host "Criando produto na API central..."
$product = Invoke-RestMethod `
    -Method Post `
    -Uri "$catalogUrl/api/catalogo/produtos" `
    -ContentType "application/json" `
    -Body $createBody

Start-Sleep -Seconds 2
Write-Host "Produto recebido pela API local:"
Invoke-RestMethod "$edgeUrl/api/local/produtos?activeOnly=false" |
    Where-Object { $_.id -eq $product.id } |
    Format-List

$updateBody = @{
    tenantId = $tenantId
    storeId = $storeId
    sku = $sku
    name = "Combo Kafka Promocional"
    price = 25.90
    active = $true
} | ConvertTo-Json

Write-Host "Atualizando preço na API central..."
Invoke-RestMethod `
    -Method Put `
    -Uri "$catalogUrl/api/catalogo/produtos/$($product.id)" `
    -ContentType "application/json" `
    -Body $updateBody | Out-Null

Start-Sleep -Seconds 2
Write-Host "Versão atualizada na loja:"
Invoke-RestMethod "$edgeUrl/api/local/produtos?activeOnly=false" |
    Where-Object { $_.id -eq $product.id } |
    Format-List

Write-Host "Status do outbox central:"
Invoke-RestMethod "$catalogUrl/internal/outbox/status" | Format-List

Write-Host "Status do sincronizador:"
Invoke-RestMethod "$edgeUrl/interno/sincronizacao/status" | Format-List
