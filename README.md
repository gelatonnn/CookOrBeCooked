# 🍳 CookOrBeCooked

![Game Banner](src/resources/bg_menu.jpg)

**CookOrBeCooked** adalah game simulasi memasak 2D yang seru dan *chaotic*, terinspirasi dari game populer *Overcooked*. Dibangun sepenuhnya menggunakan **Java Swing** dan **AWT**, game ini menantang pemain untuk menyiapkan bahan, memasak, dan menyajikan hidangan sebelum waktu habis!

Game ini mendukung **Single Player** (dengan fitur ganti karakter) dan **Local Multiplayer** (Co-op 2 Pemain).

---

## ✨ Fitur Utama

* **👨‍🍳 Mode Permainan:**
    * **Single Player:** Kendalikan dua chef secara bergantian untuk menyelesaikan pesanan.
    * **Local Multiplayer:** Bermain bersama teman dalam satu keyboard.
* **🔪 Mekanik Memasak:**
    * Potong bahan di *Cutting Station*.
    * Rebus pasta di *Boiling Pot* dan goreng daging di *Frying Pan* (Hati-hati gosong! 🔥).
    * Cuci piring kotor di *Washing Station*.
    * Rakit hidangan di *Assembly Station*.
* **🎰 Lucky Station (Gacha Mechanics):**
    Station unik yang memberikan efek acak (Buff/Debuff) saat diputar:
    * ⚡ **The Flash:** Lari super cepat & dash tanpa cooldown.
    * 💰 **Double Money:** Skor 2x lipat.
    * 😵 **Drunk Chef:** Kontrol arah terbalik.
    * 🔥 **Hell's Kitchen:** Salah satu masakan otomatis gosong.
    * ✨ **Magic Sponge:** Semua piring kotor langsung bersih.
* **🛠️ Sistem Fisika:** Lempar bahan mentah (*Throw*) ke lantai atau ke teman untuk efisiensi waktu.
* **🏆 Stage Progression:** 3 Level kesulitan (Easy, Medium, Survival Mode).

---

## 🎮 Kontrol

### Single Player
| Aksi | Tombol |
| :--- | :--- |
| **Gerak** | `W`, `A`, `S`, `D` |
| **Interaksi (Potong/Cuci/Spin)** | `E` |
| **Ambil / Taruh** | `F` |
| **Lempar (Throw)** | `T` |
| **Dash (Lari)** | `Shift` |
| **Ganti Chef** | `Tab` atau `C` |

### Multiplayer (2 Pemain)
| Aksi | Player 1 (Kiri) | Player 2 (Kanan) |
| :--- | :--- | :--- |
| **Gerak** | `W`, `A`, `S`, `D` | `Panah Atas`, `Bawah`, `Kiri`, `Kanan` |
| **Interaksi** | `V` | `K` |
| **Ambil / Taruh** | `B` | `L` |
| **Lempar** | `F` | `;` (Titik Koma) |
| **Dash** | `Shift Kiri` | `Shift Kanan` |

---

## 📜 Daftar Resep

1.  🍝 **Pasta Marinara:** Pasta + Tomato
2.  🍝 **Pasta Bolognese:** Pasta + Meat
3.  🦐 **Seafood Pasta:** Pasta + Shrimp + Fish

*Semua bahan harus diproses (dipotong & dimasak) sebelum disatukan di Piring.*

---

## 🚀 Cara Menjalankan Project

### Prasyarat
* **Java JDK 17** atau lebih baru.
* **VS Code** atau **IntelliJ IDEA**.

### Instalasi & Run
1.  **Clone** repository ini atau download folder project.
2.  Buka terminal di dalam folder project.
3.  Jalankan perintah berikut menggunakan Gradle Wrapper:

    **Windows (PowerShell/CMD):**
    ```powershell
    .\gradlew run
    ```

    **Mac/Linux:**
    ```bash
    ./gradlew run
    ```

### Struktur Project
Game ini menggunakan arsitektur **MVC (Model-View-Controller)**:
* `src/model`: Logika utama game (`GameEngine`), entitas (`Chef`, `Item`), dan state management.
* `src/view`: Rendering grafis menggunakan `Graphics2D`, GUI Panel (`HomePanel`, `GamePanel`), dan animasi sprite.
* `src/controller`: Menangani input keyboard dan menerjemahkannya menjadi perintah (`Command Pattern`).

---

## 🛠️ Tech Stack
* **Language:** Java 17+
* **GUI Framework:** Java Swing & AWT
* **Build Tool:** Gradle
* **Rendering:** Custom 2D Graphics Loop (Double Buffering)

---

## 📸 Screenshots

| Menu Utama | Gameplay |
| :---: | :---: |
| ![Menu](src/resources/bg_menu.jpg) | ![Gameplay](src/resources/SelectStageBackground.png) |

---

Made with ❤️ by [Nama Anda/Kelompok Anda]
