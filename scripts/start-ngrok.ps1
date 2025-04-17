$ngrokPath = "C:\ngrok\ngrok.exe"
$port = 8443

# Kiểm tra ngrok.exe
if (-Not (Test-Path $ngrokPath)) {
    Write-Error "ngrok.exe không tìm thấy tại $ngrokPath"
    exit 1
}

Write-Output "Đang khởi động ngrok trên cổng $port..."
Start-Process -FilePath $ngrokPath -ArgumentList "http $port" -WindowStyle Hidden
Start-Sleep -Seconds 5

try {
    Write-Output "Đang gọi API ngrok..."
    $response = Invoke-RestMethod -Uri "http://localhost:4040/api/tunnels"
    $ngrokUrl = ($response.tunnels | Where-Object { $_.proto -eq "https" }).public_url
    if ($ngrokUrl) {
        Write-Output "ngrok URL: $ngrokUrl"
        [Environment]::SetEnvironmentVariable("NGROK_URL", $ngrokUrl, [EnvironmentVariableTarget]::Process)

        # Đường dẫn tới file YAML
        $yamlFile = "E:\My Ultimate Workspace\Project KHMT\HotelSystemManagement\src\main\resources\application.yaml"
        Write-Output "Đang kiểm tra file YAML tại: $yamlFile"

        # Kiểm tra file YAML tồn tại
        if (-Not (Test-Path $yamlFile)) {
            Write-Error "File YAML không tìm thấy tại $yamlFile. Vui lòng kiểm tra đường dẫn."
            exit 1
        }

        # Đọc nội dung file YAML
        $yamlContent = Get-Content -Path $yamlFile -Raw
        Write-Output "Nội dung file YAML trước khi thay đổi:`n$yamlContent"

        # Thay thế callback-url và redirect-url
        $newCallbackUrl = "$ngrokUrl/zalopay/callback"
        #$newRedirectUrl = "$ngrokUrl/payment-success.html"
        Write-Output "Thay thế callback-url thành: $newCallbackUrl"
        #Write-Output "Thay thế redirect-url thành: $newRedirectUrl"

        # Sử dụng regex để khớp dòng callback-url và redirect-url
        $yamlContent = $yamlContent -replace "callback-url:\s*.*", "callback-url: $newCallbackUrl"
        #$yamlContent = $yamlContent -replace "redirect-url:\s*.*", "redirect-url: $newRedirectUrl"

        # Ghi lại file YAML
        Write-Output "Đang ghi file YAML..."
        try {
            Set-Content -Path $yamlFile -Value $yamlContent -Force
            Write-Output "Đã ghi file YAML thành công."
            Write-Output "Nội dung file YAML sau khi thay đổi:`n$(Get-Content -Path $yamlFile -Raw)"
        } catch {
            Write-Error "Lỗi khi ghi file YAML: $_"
            exit 1
        }
    } else {
        Write-Error "Không lấy được ngrok URL"
        exit 1
    }
} catch {
    Write-Error "Lỗi khi gọi ngrok API: $_"
    exit 1
}