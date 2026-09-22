# Ular Tangga Keluarga — Web (Tamu)

Halaman statis (HTML/CSS/JS murni, tanpa build step, tanpa Node.js) untuk anggota
keluarga yang mau gabung room online lewat browser tanpa install APK Android.

Nyambung ke Firebase Realtime Database yang sama persis dengan app Android
(skema data identik dengan `RoomRepository.kt`), jadi 100% kompatibel: host
selalu dari Android, tamu bisa dari Android **atau** browser ini secara
bersamaan dalam satu room.

**Batasan**: halaman ini hanya untuk role **tamu** (gabung pakai kode room),
bukan host — logika permainan (dadu, tangga, ular, kartu, dst) tetap
dijalankan oleh device Android yang jadi host, sama seperti tamu Android.

## Deploy ke VPS (nginx)

Tidak butuh backend/server-side apa pun — cukup file statis yang disajikan
oleh web server mana saja.

1. Salin folder `web/` ini ke VPS, misal ke `/var/www/ular-tangga`:
   ```bash
   scp -r web/ user@vps-kamu:/var/www/ular-tangga
   ```
2. Tambahkan server block nginx (contoh, sesuaikan domain/port):
   ```nginx
   server {
       listen 80;
       server_name ular-tangga.domainkamu.com;
       root /var/www/ular-tangga;
       index index.html;
   }
   ```
3. Reload nginx: `sudo nginx -t && sudo systemctl reload nginx`.
4. (Disarankan) Pasang HTTPS gratis dengan Certbot:
   ```bash
   sudo certbot --nginx -d ular-tangga.domainkamu.com
   ```

Setelah itu, siapa saja di keluarga tinggal buka
`http://ular-tangga.domainkamu.com` (atau `https://` setelah pasang Certbot)
di browser HP/laptop, masukkan kode room dari host Android, dan langsung main.

## Coba lokal dulu (opsional)

Sebelum upload ke VPS, bisa dites di komputer sendiri:
```bash
cd web
python3 -m http.server 8080
```
Lalu buka `http://localhost:8080` di browser.
