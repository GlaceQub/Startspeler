# Local Run

## Start the app

1. Find your laptop's Wi-Fi IPv4 address:

```powershell
ipconfig
```

Use the IPv4 address under `Wireless LAN adapter Wi-Fi`.

2. Set `BACKEND_URL` in `Database/docker/.env`:

```env
BACKEND_URL=http://YOUR_WIFI_IP:8080
```

3. Start the backend:

```powershell
& "c:\StartspelerProject\Startspeler\StartspelerApp\gradlew.bat" -p "c:\StartspelerProject\Startspeler\StartspelerApp" :server:run
```

4. Start the frontend:

```powershell
& "c:\StartspelerProject\Startspeler\StartspelerApp\gradlew.bat" --no-daemon -p "c:\StartspelerProject\Startspeler\StartspelerApp" :jsApp:jsBrowserDevelopmentRun
```

5. Open on laptop or phone:

```text
http://YOUR_WIFI_IP:8081
```

## If you switch Wi-Fi networks

1. Run `ipconfig` again.
2. Update `BACKEND_URL` in `Database/docker/.env` with the new Wi-Fi IP.
3. Regenerate the frontend config:

```powershell
& "c:\StartspelerProject\Startspeler\StartspelerApp\gradlew.bat" -p "c:\StartspelerProject\Startspeler\StartspelerApp" :jsApp:jsProcessResources --rerun-tasks
```

4. Restart the frontend dev server:

```powershell
& "c:\StartspelerProject\Startspeler\StartspelerApp\gradlew.bat" --no-daemon -p "c:\StartspelerProject\Startspeler\StartspelerApp" :jsApp:jsBrowserDevelopmentRun
```

5. Refresh the page on your laptop or phone.

## Quick check

Open this in your browser:

```text
http://YOUR_WIFI_IP:8081/config.json
```

It should show:

```json
{
  "backendUrl": "http://YOUR_WIFI_IP:8080"
}
```