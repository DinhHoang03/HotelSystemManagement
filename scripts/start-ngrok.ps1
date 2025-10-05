$ngrokPath = "C:\ngrok\ngrok.exe"
$port = 8443

# Thiết lập encoding UTF-8 cho console
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

# Kiểm tra ngrok.exe
if (-Not (Test-Path $ngrokPath)) {
    Write-Host "ngrok.exe khong tim thay tai $ngrokPath" -ForegroundColor Red
    exit 1
}

Write-Host "Dang khoi dong ngrok tren cong $port..." -ForegroundColor Yellow
Start-Process -FilePath $ngrokPath -ArgumentList "http $port" -WindowStyle Hidden
Start-Sleep -Seconds 5

try {
    Write-Host "Dang goi API ngrok..." -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri "http://localhost:4040/api/tunnels"
    $ngrokUrl = ($response.tunnels | Where-Object { $_.proto -eq "https" }).public_url

    if ($ngrokUrl) {
        Write-Host "ngrok URL: $ngrokUrl" -ForegroundColor Green
        [Environment]::SetEnvironmentVariable("NGROK_URL", $ngrokUrl, [EnvironmentVariableTarget]::Process)

        # Đường dẫn tới file YAML
        $yamlFile = "E:\My Ultimate Workspace\Project KHMT\HotelSystemManagement\src\main\resources\application.yaml"
        Write-Host "Dang kiem tra file YAML tai: $yamlFile" -ForegroundColor Yellow

        # Kiểm tra file YAML tồn tại
        if (-Not (Test-Path $yamlFile)) {
            Write-Host "File YAML khong tim thay tai $yamlFile" -ForegroundColor Red
            exit 1
        }

        # Đọc nội dung file YAML với UTF-8 encoding
        $yamlContent = Get-Content -Path $yamlFile -Raw -Encoding UTF8

        # Thay thế callback-url
        $newCallbackUrl = "$ngrokUrl/zalopay/callback"
        Write-Host "Thay the callback-url thanh: $newCallbackUrl" -ForegroundColor Yellow

        # Sử dụng regex để khớp dòng callback-url
        $yamlContent = $yamlContent -replace "callback-url:\s*.*", "callback-url: $newCallbackUrl"

        # Ghi lại file YAML với UTF-8 encoding
        Write-Host "Dang ghi file YAML..." -ForegroundColor Yellow
        try {
            Set-Content -Path $yamlFile -Value $yamlContent -Force -Encoding UTF8
            Write-Host "Da ghi file YAML thanh cong!" -ForegroundColor Green
        } catch {
            Write-Host "Loi khi ghi file YAML: $_" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "Khong lay duoc ngrok URL" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Loi khi goi ngrok API: $_" -ForegroundColor Red
    exit 1
}