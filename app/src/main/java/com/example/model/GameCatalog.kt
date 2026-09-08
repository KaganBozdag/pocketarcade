package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

object GameCatalog {
    val games: List<ArcadeGame> = listOf(
        // --- 1 to 6: Existing Flagship Games ---
        ArcadeGame(
            id = "snake",
            title = "Yılan (Retro Snake)",
            subtitle = "Klasik yılan oyunu. Elmaları topla, hızlan ve kuyruğuna çarpma!",
            emoji = "🐍",
            category = GameCategory.ARCADE,
            themeColor = NeonGreen,
            actionText = "OYNA",
            instructions = "Yılanı yönlendirerek kırmızı ve altın elmaları topla. Duvarlara ve kendine çarpmamaya dikkat et!"
        ),
        ArcadeGame(
            id = "space",
            title = "Uzay Savunucusu",
            subtitle = "Lazerlerle göktaşlarını ve uzaylı UFO'ları patlat, galaksiyi kurtar!",
            emoji = "🚀",
            category = GameCategory.ARCADE,
            themeColor = NeonCyan,
            actionText = "SAVAŞ",
            instructions = "Parmağını sürükleyerek gemini hareket ettir. Lazerler otomatik ateşlenir."
        ),
        ArcadeGame(
            id = "reflex",
            title = "Refleks Avcısı (Rush)",
            subtitle = "30 saniyede beliren elmasları yakala, bombalardan kaç, kombo yap!",
            emoji = "⚡",
            category = GameCategory.REFLEX,
            themeColor = NeonAmber,
            actionText = "KAPIŞ",
            instructions = "Işıldayan mücevherlere ve saatlere tıkla, kuru kafa bombalarından kaçın!"
        ),
        ArcadeGame(
            id = "memory",
            title = "Hafıza Kartları (Flip)",
            subtitle = "Gizli retro oyun kartlarını eşleştir, hafızanı test et ve 3 yıldız kazan!",
            emoji = "🧠",
            category = GameCategory.PUZZLE,
            themeColor = NeonPurple,
            actionText = "EŞLEŞTİR",
            instructions = "Kapalı kartları çevir ve aynı olan çiftleri en az hamlede bul."
        ),
        ArcadeGame(
            id = "2048",
            title = "2048 Sayı Bulmacası",
            subtitle = "Sayıları kaydırıp birleştir, rekor puanlara ve 2048 karosuna ulaş!",
            emoji = "🎲",
            category = GameCategory.PUZZLE,
            themeColor = Color(0xFFF97316),
            actionText = "BİRLEŞTİR",
            instructions = "Blokları kaydır. Aynı sayılar çarpışınca ikiye katlanır!"
        ),
        ArcadeGame(
            id = "flappy",
            title = "Zıplayan Kuş (Flappy)",
            subtitle = "Ekrana dokunup kanat çırp, boruların arasından süzül ve madalyaları topla!",
            emoji = "🐤",
            category = GameCategory.ARCADE,
            themeColor = NeonPink,
            actionText = "UÇ",
            instructions = "Ekrana dokunarak kuşu havada tut ve borulardan geçerek altın topla."
        ),

        // --- 7 to 14: Classic Arcade Pack ---
        ArcadeGame(
            id = "pong",
            title = "Retro Pong Tenis",
            subtitle = "Robot rakibe karşı raketini hareket ettir, topu kaçırma!",
            emoji = "🏓",
            category = GameCategory.ARCADE,
            themeColor = NeonCyan,
            actionText = "KAPIŞ",
            instructions = "Raketini parmağınla kontrol et. Rakibin arkasına topu göndererek sayı kazan."
        ),
        ArcadeGame(
            id = "breakout",
            title = "Tuğla Kırıcı (Breakout)",
            subtitle = "Raketle topu sektirerek yukarıdaki tüm renkli tuğlaları parçala!",
            emoji = "🧱",
            category = GameCategory.ARCADE,
            themeColor = Color(0xFFE11D48),
            actionText = "KIR",
            instructions = "Topu aşağı düşürmeden tuğlaları kır ve bonusları topla."
        ),
        ArcadeGame(
            id = "sky_jump",
            title = "Zıplayan Dost (Sky Jump)",
            subtitle = "Sürekli yukarı zıplayarak hareketli bulut basamaklarına tırman!",
            emoji = "🦘",
            category = GameCategory.ARCADE,
            themeColor = NeonGreen,
            actionText = "TIRMAN",
            instructions = "Sağa ve sola sürükleyerek platformlara bas ve gökyüzünün zirvesine çık."
        ),
        ArcadeGame(
            id = "asteroid_dodge",
            title = "Göktaşı Kaçışı",
            subtitle = "Yukarıdan yağan meteor yağmurundan gemini ustalıkla kaçır!",
            emoji = "☄️",
            category = GameCategory.ARCADE,
            themeColor = Color(0xFFFB923C),
            actionText = "KAÇ",
            instructions = "Parmağınla gemini sağa sola çekerek çarpmaktan kurtul."
        ),
        ArcadeGame(
            id = "frogger",
            title = "Yol Geçişi (Crossy Hop)",
            subtitle = "Trafik dolu otobandan ve nehir kütüklerinden karşıya zıpla!",
            emoji = "🐸",
            category = GameCategory.ARCADE,
            themeColor = NeonGreen,
            actionText = "GEÇ",
            instructions = "İleri, sol ve sağ butonlarıyla arabalara çarpmadan nehri geç."
        ),
        ArcadeGame(
            id = "copter",
            title = "Mağara Helikopteri",
            subtitle = "Basılı tutarak yüksel, bırakınca alçal. Sivri kayalara çarpma!",
            emoji = "🚁",
            category = GameCategory.ARCADE,
            themeColor = NeonAmber,
            actionText = "UÇUR",
            instructions = "Basınca motor çalışır, bırakınca süzülür. Tavan ve tabandaki kayalara dikkat et."
        ),
        ArcadeGame(
            id = "maze_runner",
            title = "Labirent Kaçışı",
            subtitle = "Karanlık neon labirentte çıkış kapısını ve altın anahtarı bul!",
            emoji = "🌀",
            category = GameCategory.ARCADE,
            themeColor = NeonPurple,
            actionText = "BUL",
            instructions = "Ok tuşlarıyla karakterini labirentin sonundaki çıkış kapısına ulaştır."
        ),
        ArcadeGame(
            id = "pac_dash",
            title = "Neon Pac Yemcisi",
            subtitle = "Koridorlardaki sarı yemleri topla, peşindeki neon hayaletten kaç!",
            emoji = "👻",
            category = GameCategory.ARCADE,
            themeColor = Color(0xFFFBBF24),
            actionText = "TOPLA",
            instructions = "Tüm sarı noktaları toplayarak koridorları temizle!"
        ),

        // --- 15 to 24: Puzzle & Logic Pack ---
        ArcadeGame(
            id = "minesweeper",
            title = "Mayın Tarlası",
            subtitle = "Rakam ipuçlarını kullanarak gizli mayınları bayrakla işaretle!",
            emoji = "💣",
            category = GameCategory.PUZZLE,
            themeColor = Color(0xFFEF4444),
            actionText = "AÇ",
            instructions = "Kareleri aç, sayılar etraftaki mayın sayısını belirtir. Mayınlara basma!"
        ),
        ArcadeGame(
            id = "puzzle15",
            title = "15-Puzzle (Kayan Sayılar)",
            subtitle = "1'den 15'e kadar olan sayıları boşluğu kullanarak sıraya diz!",
            emoji = "🔢",
            category = GameCategory.PUZZLE,
            themeColor = NeonCyan,
            actionText = "ÇÖZ",
            instructions = "Boşluğun yanındaki sayılara dokunarak yerlerini değiştir ve sırala."
        ),
        ArcadeGame(
            id = "tictactoe",
            title = "XOX Düellosu",
            subtitle = "Akıllı yapay zekaya karşı X veya O ile 3'lü sıra oluştur!",
            emoji = "❌",
            category = GameCategory.PUZZLE,
            themeColor = NeonPink,
            actionText = "KAPIŞ",
            instructions = "3x3 ızgarada yatay, dikey ya da çapraz 3'lü yapmaya çalış."
        ),
        ArcadeGame(
            id = "connect4",
            title = "Dörtlü Sıra (Connect 4)",
            subtitle = "Pullarını sütunlara bırakarak 4 aynı rengi yan yana getir!",
            emoji = "🔴",
            category = GameCategory.PUZZLE,
            themeColor = Color(0xFF3B82F6),
            actionText = "SIRALA",
            instructions = "Sütunlara pul at. İlk 4'lü yapan kazanır."
        ),
        ArcadeGame(
            id = "sudoku_mini",
            title = "Mini Sudoku 4x4",
            subtitle = "Her satır, sütun ve 2x2 karede 1-4 arası rakamları tekrar etmeden doldur!",
            emoji = "✏️",
            category = GameCategory.PUZZLE,
            themeColor = Color(0xFF10B981),
            actionText = "DOLDUR",
            instructions = "Boş karelere 1, 2, 3, 4 sayılarını kurallara uygun yerleştir."
        ),
        ArcadeGame(
            id = "lights_out",
            title = "Işıkları Söndür (Lights Out)",
            subtitle = "Bir lambaya bastığında komşuları da söner. Tüm ızgarayı karart!",
            emoji = "💡",
            category = GameCategory.PUZZLE,
            themeColor = NeonAmber,
            actionText = "SÖNDÜR",
            instructions = "Dokunduğun ışık ve çevresindekiler durum değiştirir. Hepsini söndür."
        ),
        ArcadeGame(
            id = "simon",
            title = "Simon Renk Tekrarı",
            subtitle = "Gittikçe uzayan renk ve ses dizisini hafızanda tut ve tekrar et!",
            emoji = "🎨",
            category = GameCategory.PUZZLE,
            themeColor = NeonPurple,
            actionText = "TEKRAR ET",
            instructions = "Yanan renk sırasını aklında tut ve aynı sırayla butonlara bas."
        ),
        ArcadeGame(
            id = "water_sort",
            title = "Renk Tüpleri (Water Sort)",
            subtitle = "Aynı renkteki sıvıları aynı tüpte toplayana kadar dikkatle aktar!",
            emoji = "🧪",
            category = GameCategory.PUZZLE,
            themeColor = NeonCyan,
            actionText = "DÖK",
            instructions = "Bir tüpe dokunup diğerine dök. Sadece aynı renkteki sıvılar birbirinin üzerine dökülebilir."
        ),
        ArcadeGame(
            id = "block_drop",
            title = "Blok Patlatma (Tetro Grid)",
            subtitle = "8x8 ızgaraya blok şekillerini yerleştir ve tam sıraları patlat!",
            emoji = "🟦",
            category = GameCategory.PUZZLE,
            themeColor = Color(0xFF6366F1),
            actionText = "YERLEŞTİR",
            instructions = "Aşağıdaki 3 blok parçasını ızgaraya koy, satır veya sütun doldurarak yok et."
        ),
        ArcadeGame(
            id = "bulls_cows",
            title = "Sayı Avcısı (Mastermind)",
            subtitle = "4 basamaklı gizli sayıyı tahmin et, yeşil ve sarı ipuçlarını çöz!",
            emoji = "🕵️",
            category = GameCategory.PUZZLE,
            themeColor = NeonAmber,
            actionText = "BUL",
            instructions = "Yeşil: Sayı ve basamak doğru. Sarı: Sayı var ama yeri yanlış."
        ),

        // --- 25 to 34: Speed & Reflex Pack ---
        ArcadeGame(
            id = "color_tap",
            title = "Renk Tuzağı (Stroop)",
            subtitle = "Yazılan kelimeye değil, kelimenin gerçek yazı rengine hızlıca dokun!",
            emoji = "🔴",
            category = GameCategory.REFLEX,
            themeColor = Color(0xFFEC4899),
            actionText = "VUR",
            instructions = "Beynini yanıltan kelimeyi oku ama rengine tıkla. Hızlı ol!"
        ),
        ArcadeGame(
            id = "math_sprint",
            title = "Hızlı Matematik Deparı",
            subtitle = "30 saniyede ekrandaki toplama, çıkarma ve çarpma sorularını çöz!",
            emoji = "➕",
            category = GameCategory.REFLEX,
            themeColor = NeonCyan,
            actionText = "HESAPLA",
            instructions = "Doğru şıkkı olabildiğince hızlı işaretle ve kombo çarpanı yakala."
        ),
        ArcadeGame(
            id = "piano_tiles",
            title = "Piyano Fayansları",
            subtitle = "Yukarıdan akan siyah tuşlara ritimle bas, beyaz tuşlara dokunma!",
            emoji = "🎹",
            category = GameCategory.REFLEX,
            themeColor = Color(0xFF1E293B),
            actionText = "ÇAL",
            instructions = "Aşağı kayan siyah fayansları kaçırmadan basarak melodi üret."
        ),
        ArcadeGame(
            id = "whack_mole",
            title = "Köstebek Vurmaca",
            subtitle = "Deliklerden kafasını çıkaran köstebeklere çekicinle vur!",
            emoji = "🦔",
            category = GameCategory.REFLEX,
            themeColor = NeonAmber,
            actionText = "VUR",
            instructions = "Beliren köstebeklere hemen dokun. Bomba çıkarsa sakın basma!"
        ),
        ArcadeGame(
            id = "knife_hit",
            title = "Bıçak Fırlatma (Knife Hit)",
            subtitle = "Dönen kütüğe elindeki tüm bıçakları sapla, diğer bıçaklara çarpma!",
            emoji = "🗡️",
            category = GameCategory.REFLEX,
            themeColor = Color(0xFFE2E8F0),
            actionText = "FIRLAT",
            instructions = "Ekrana dokunarak bıçağı fırlat. Var olan bıçaklara değersen yanarsın."
        ),
        ArcadeGame(
            id = "tower_stack",
            title = "Kule Yığma (Tower Stack)",
            subtitle = "Sağa sola kayan blokları tam üst üste denk getirerek göğe tırman!",
            emoji = "🏗️",
            category = GameCategory.REFLEX,
            themeColor = NeonGreen,
            actionText = "DİZ",
            instructions = "Blok tam hizadayken dokun. Taşan kısımlar kesilir!"
        ),
        ArcadeGame(
            id = "traffic_dodge",
            title = "Trafik Kaçışı (Car Rush)",
            subtitle = "3 şeritli otoyolda hızla ilerle, önüne çıkan araçlara çarpma!",
            emoji = "🏎️",
            category = GameCategory.REFLEX,
            themeColor = Color(0xFFEF4444),
            actionText = "SÜR",
            instructions = "Şerit değiştirmek için sola veya sağa tıkla, hızlandıkça reflekslerini konuştur."
        ),
        ArcadeGame(
            id = "coin_catcher",
            title = "Altın Sepeti (Coin Catcher)",
            subtitle = "Gökten yağan paraları ve elmasları topla, dinamitlerden kaç!",
            emoji = "🧺",
            category = GameCategory.REFLEX,
            themeColor = NeonAmber,
            actionText = "YAKALA",
            instructions = "Sepeti parmağınla kaydırarak altınları yakala."
        ),
        ArcadeGame(
            id = "balance_ball",
            title = "Tahterevalli Denge Topu",
            subtitle = "Denge tahtasında topun kenarlardan düşmesini parmaklarınla engelle!",
            emoji = "⚖️",
            category = GameCategory.REFLEX,
            themeColor = NeonPurple,
            actionText = "DENGEDE TUT",
            instructions = "Sol ve sağ butonlarla platformu eğip topu ortada tut."
        ),
        ArcadeGame(
            id = "speed_click",
            title = "10 Saniye Tıklama Testi (CPS)",
            subtitle = "10 saniyede butona kaç defa basabileceksin? Rekor kır!",
            emoji = "👆",
            category = GameCategory.REFLEX,
            themeColor = NeonCyan,
            actionText = "TIKLA",
            instructions = "Süre başlayınca çılgınlar gibi butona tıkla ve CPS hızını ölç."
        ),

        // --- 35 to 42: Word & Trivia Pack ---
        ArcadeGame(
            id = "hangman",
            title = "Adam Asmaca (Hangman)",
            subtitle = "Gizli Türkçe kelimeyi 6 yanlış hakkın bitmeden harf harf tahmin et!",
            emoji = "🪢",
            category = GameCategory.WORD,
            themeColor = Color(0xFFE11D48),
            actionText = "TAHMİN ET",
            instructions = "Harf butonlarına basarak kelimedeki eksik harfleri bul."
        ),
        ArcadeGame(
            id = "wordle",
            title = "5 Harf Kelimece (Wordle)",
            subtitle = "Günün 5 harfli gizli kelimesini 6 denemede yeşil ve sarı ipuçlarıyla bul!",
            emoji = "🟩",
            category = GameCategory.WORD,
            themeColor = NeonGreen,
            actionText = "YAZ",
            instructions = "Kelime tahmin et. Yeşil: Doğru yer. Sarı: Kelimede var. Gri: Yok."
        ),
        ArcadeGame(
            id = "scramble",
            title = "Karışık Kelime (Scramble)",
            subtitle = "Harfleri karışmış kelimeyi ipucuna bakarak doğru sıraya diz!",
            emoji = "🔤",
            category = GameCategory.WORD,
            themeColor = NeonAmber,
            actionText = "DÜZELT",
            instructions = "Harflere sırayla tıklayarak anlamlı kelimeyi oluştur."
        ),
        ArcadeGame(
            id = "trivia_quiz",
            title = "Genel Kültür Quiz",
            subtitle = "Tarih, bilim, coğrafya ve oyun dünyasından soruları bil!",
            emoji = "🏆",
            category = GameCategory.WORD,
            themeColor = NeonPurple,
            actionText = "CEVAPLA",
            instructions = "4 seçenek arasından doğru olanı seçerek seri yap ve puan kazan."
        ),
        ArcadeGame(
            id = "flag_quiz",
            title = "Dünya Bayrakları",
            subtitle = "Gösterilen ülke bayrağının hangi ülkeye ait olduğunu bil!",
            emoji = "🚩",
            category = GameCategory.WORD,
            themeColor = NeonCyan,
            actionText = "TANI",
            instructions = "Bayrağa bakarak doğru ülkeyi işaretle."
        ),
        ArcadeGame(
            id = "true_false_math",
            title = "Doğru mu Yanlış mı?",
            subtitle = "Ekranda beliren matematik işlemi doğru mu yoksa yanlış mı? Anında karar ver!",
            emoji = "⚡",
            category = GameCategory.WORD,
            themeColor = Color(0xFF10B981),
            actionText = "KARAR VER",
            instructions = "İşlemin sonucu doğruysa yeşil tike, yanlışsa kırmızı çarpıya bas."
        ),
        ArcadeGame(
            id = "anagram_rush",
            title = "Anagram Avcısı",
            subtitle = "Verilen 6 harften türetebildiğin kadar Türkçe kelime türet!",
            emoji = "📝",
            category = GameCategory.WORD,
            themeColor = Color(0xFFF59E0B),
            actionText = "TÜRET",
            instructions = "Harfleri birleştirerek geçerli kelimeler bul."
        ),
        ArcadeGame(
            id = "memory_sequence",
            title = "Sayı Hafızası Testi",
            subtitle = "Ekranda saniyelik parlayan sayı dizisini hatırla ve tuşla!",
            emoji = "🔢",
            category = GameCategory.WORD,
            themeColor = NeonCyan,
            actionText = "HATIRLA",
            instructions = "Her turda 1 basamak uzayan sayıyı ezberle ve klavyeden gir."
        ),

        // --- 43 to 50: Online & Live Matchmaking Multiplayer Arena ---
        ArcadeGame(
            id = "online_tictactoe",
            title = "Çevrimiçi XOX Arenası",
            subtitle = "Dünyanın dört bir yanından canlı rakiplerle eşleş ve 3 rauntta yen!",
            emoji = "🌐",
            category = GameCategory.ONLINE,
            themeColor = NeonCyan,
            actionText = "EŞLEŞ",
            isOnline = true,
            instructions = "Canlı lobiye bağlan. Sıran geldiğinde hamleni yap, 3'lü yapan maçı kazanır!"
        ),
        ArcadeGame(
            id = "online_rps",
            title = "Taş Kağıt Makas Arena",
            subtitle = "Canlı rakibe karşı 5 rauntluk zihin savaşı! Hamleni seç ve kazan!",
            emoji = "✊",
            category = GameCategory.ONLINE,
            themeColor = NeonPink,
            actionText = "KAPIŞ",
            isOnline = true,
            instructions = "Aynı anda seçim yapın: Taş makası, kağıt taşı, makas kağıdı yener."
        ),
        ArcadeGame(
            id = "online_tap_battle",
            title = "Halat Çekme Tıklama Düellosu",
            subtitle = "Rakibinle aynı anda çılgınca tıkla, bayrağı kendi tarafına çek!",
            emoji = "🪢",
            category = GameCategory.ONLINE,
            themeColor = Color(0xFFF97316),
            actionText = "ÇEK",
            isOnline = true,
            instructions = "Hızlıca butona tıkla. Daha hızlı olan halatı kendi tarafına çeker."
        ),
        ArcadeGame(
            id = "online_dice",
            title = "Canlı Zar Düellosu",
            subtitle = "Rakibinle 2 zar at, en yüksek kombinasyonu yakalayan altınları toplar!",
            emoji = "🎲",
            category = GameCategory.ONLINE,
            themeColor = NeonAmber,
            actionText = "ZAR AT",
            isOnline = true,
            instructions = "Zarları salla. Çift zarlar x2 puan verir!"
        ),
        ArcadeGame(
            id = "online_roulette",
            title = "Balon Patlatma Ruleti",
            subtitle = "Sırayla balonu şişirin. Balon kimin elinde patlarsa o kaybeder!",
            emoji = "🎈",
            category = GameCategory.ONLINE,
            themeColor = Color(0xFFEC4899),
            actionText = "ŞİŞİR",
            isOnline = true,
            instructions = "Pompalayarak puan al ama patlama riskine dikkat et. Şansına güven!"
        ),
        ArcadeGame(
            id = "online_card21",
            title = "Kart 21 / Blackjack Arcade",
            subtitle = "Canlı masada dağıtıcıya ve oyunculara karşı 21'e en yakın eli yap!",
            emoji = "🃏",
            category = GameCategory.ONLINE,
            themeColor = Color(0xFF10B981),
            actionText = "KART ÇEK",
            isOnline = true,
            instructions = "Kart iste (Hit) veya kal (Stand). 21'i geçen yanar!"
        ),
        ArcadeGame(
            id = "online_lucky_wheel",
            title = "Şans Çarkı Turnuvası",
            subtitle = "Dev neon çarkıfeleği çevir, canlı liderlik tablosunda zirveye oyna!",
            emoji = "🎡",
            category = GameCategory.ONLINE,
            themeColor = NeonPurple,
            actionText = "ÇEVİR",
            isOnline = true,
            instructions = "Çarkı döndür, çarpanları ve bonus puanları topla."
        ),
        ArcadeGame(
            id = "online_bingo",
            title = "Hızlı Tombala / Bingo Rush",
            subtitle = "Canlı spikerin çektiği numaraları kartında işaretle, ilk 'BİNGO' yapan sen ol!",
            emoji = "🎱",
            category = GameCategory.ONLINE,
            themeColor = NeonCyan,
            actionText = "BİNGO",
            isOnline = true,
            instructions = "Çekilen numaralar kartında varsa hızlıca dokun ve sırayı tamamla!"
        )
    )

    fun getGameById(id: String): ArcadeGame? = games.find { it.id == id }
}
