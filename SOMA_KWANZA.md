# Bulk SMS App (JS.Dev) — Maelekezo

## 🆕 TOLEO LA 7 — Kikamataji cha Crash (MUHIMU SANA — soma hii kwanza)

Kwa vile app ilianguka kwenye Infinix yako bila kuonyesha error yoyote, nimeongeza **kikamataji cha crash**: sasa app itahifadhi maelezo kamili ya hitilafu yoyote, na itakuonyesha mara ya pili unapofungua app — **kabla** hata haijajaribu kuanzisha skrini ya kawaida. Hii itatuwezesha kuona sababu HALISI badala ya kukisia.

**Nimegundua na kurekebisha pia tatizo la kimuundo:** fragments zote 5 zilikuwa zikijengwa mapema mno (kabla ya `onCreate`), jambo ambalo lingezuia kikamataji cha crash kufanya kazi endapo chanzo cha crash kilikuwa ndani ya ujenzi wa fragment yenyewe. Nimebadilisha ziwe "lazy" (zinajengwa tu zinapohitajika).

**BAADA ya kupakia toleo hili na kufungua app:**
- Ikiwa app inafungua vizuri sasa — HONGERA, tatizo limeondoka!
- Ikiwa bado inaanguka — **fungua app MARA YA PILI** (bofya icon tena baada ya kuanguka mara ya kwanza). Sasa utaona dirisha (dialog) lenye maandishi ya error halisi. **Bofya "Nakili Error"**, kisha nitumie hapa hapa kwenye mazungumzo yetu — nitajua hasa tatizo ni nini na kulirekebisha mara moja badala ya kukisia.
- Unaweza pia kuona ripoti hii wakati wowote kwenye **Mipangilio → "🐞 Angalia Ripoti ya Crash ya Mwisho"**.

Faili mpya/zilizobadilika:
- `app/src/main/java/com/jsdev/bulksms/CrashHandler.kt` *(mpya)*
- `app/src/main/java/com/jsdev/bulksms/BulkSmsApplication.kt` *(mpya)*
- `app/src/main/AndroidManifest.xml` *(imebadilika — Application class imesajiliwa)*
- `app/src/main/java/com/jsdev/bulksms/MainActivity.kt` *(imebadilika kabisa — fragments 'lazy' + kuonyesha crash report)*
- `app/src/main/res/layout/fragment_settings.xml` *(kitufe kipya cha kuona crash)*
- `app/src/main/java/com/jsdev/bulksms/SettingsFragment.kt` *(kuwira kitufe hicho)*

Pakia folda nzima ya `app` upya, jenge, sakinishe, **jaribu mara mbili** kama ilivyoelezwa juu.

---

## 🆕 TOLEO LA 6 — Kuagiza Contacts + Scheduler (VIPENGELE VYA MWISHO)

Hii ndiyo awamu ya mwisho ya vipengele vilivyokuwa vimebaki:

- **📇 Agiza kutoka Anwani za Simu** — kwenye tab ya "Tuma", kitufe kipya juu kabisa kinakusomea anwani zote za simu yako, kinachuja zenye namba sahihi za Tanzania, na kinakuonyesha orodha yenye vibonye (checkbox) kuchagua unazotaka. Zinaongezwa juu ya faili ulilopakia (haziondoi zilizopo)
- **🕒 Panga Muda wa Kutuma Baadaye** — chagua tarehe na saa, kampeni itaanza yenyewe wakati huo

**KIKOMO MUHIMU cha Scheduler unachopaswa kujua:** Scheduler hii inafanya kazi **tu kama app inabaki wazi (japo nyuma) kwenye simu yako**. Haiwezi kutuma ikiwa umefunga app kabisa (kuiondoa kwenye "recent apps") au kuzima simu. Hii ni uamuzi wa kimakusudi wa usalama wa build — scheduler inayofanya kazi hata app ikiwa imefungwa kabisa inahitaji "Foreground Service" ambayo ni ngumu zaidi na yenye hatari kubwa zaidi ya kuvunja build kwenye Android 14. Kwa matumizi yako ya kawaida (kupanga kutuma baada ya saa/dakika chache huku ukiendelea kutumia simu), hii inatosha kabisa.

Faili mpya/zilizobadilika:
- `app/src/main/AndroidManifest.xml` *(ruhusa mpya ya READ_CONTACTS)*
- `app/src/main/res/layout/fragment_compose.xml` *(vitufe vipya)*
- `app/src/main/java/com/jsdev/bulksms/ComposeFragment.kt` *(uwezo mpya)*

Pakia folda nzima ya `app` upya.

### Ushauri wangu wa mwisho kama "mhandisi mkuu"
Vipengele vyote vilivyoombwa awali (isipokuwa Biometric, Encrypted DB, PDF export, Foreground-service scheduler, na Unit Tests — vilivyoelezwa kuwa vigumu/hatari kiuhalisia) sasa vimekamilika. **Kabla ya kuongeza kitu kingine chochote, tafadhali jaribu app hii kikamilifu kwenye Infinix yako** kwa kutumia orodha ya majaribio niliyokupa awali. Kuongeza vipengele zaidi bila kujaribu vilivyopo kunaongeza hatari ya kukusanya makosa mengi kwa wakati mmoja badala ya moja unaloweza kunitumia kwa urahisi kurekebisha.

---

## 🆕 TOLEO LA 5 — Backup / Restore

Ukurasa wa **Mipangilio** sasa una sehemu ya "Nakala Rudufu (Backup)":

- **⬇️ Hifadhi Nakala** — inatengeneza faili la `.json` lenye templates zako zote za ujumbe + historia yote ya kampeni, unachagua wapi kuhifadhi (Downloads, Google Drive, n.k.)
- **⬆️ Rudisha Nakala** — unachagua faili la `.json` la backup, na templates + historia zinaongezwa (haziondoi zilizopo, zinaongeza tu)

**Kwa usalama, PIN haihifadhiwi kwenye backup.** Ukibadilisha simu, utaweka PIN mpya.

Imejengwa kwa `org.json` iliyomo ndani ya Android SDK yenyewe — hakuna maktaba mpya, hakuna hatari ya ziada ya build.

Faili mpya/zilizobadilika:
- `app/src/main/java/com/jsdev/bulksms/BackupManager.kt` *(mpya)*
- `app/src/main/java/com/jsdev/bulksms/DbHelper.kt` *(imebadilika — method mpya ya kuingiza kampeni na tarehe maalum)*
- `app/src/main/res/layout/fragment_settings.xml` *(imebadilika — kadi mpya)*
- `app/src/main/java/com/jsdev/bulksms/SettingsFragment.kt` *(imebadilika — kuwira vitufe)*

Pakia folda nzima ya `app` upya.

---

## 🆕 TOLEO LA 4 — Charts (Michoro ya Takwimu)

Ukurasa wa **Ripoti** sasa una michoro mitatu, iliyojengwa kwa Canvas ya Android moja kwa moja (hakuna maktaba ya nje iliyoongezwa — hii inaepuka kabisa hatari ya build kuvunjika kwa sababu ya dependency mpya):

- **Pie chart** — mgawanyo wa Zimefika / Zimetumwa / Zimeshindwa / Zinazosubiri, na asilimia ya mafanikio katikati
- **Bar chart** — Jumla / Zimetumwa / Zimefika / Zimeshindwa kwa namba kamili
- **Line chart** — mwenendo wa kiwango cha mafanikio (%) kwa kampeni 10 za hivi karibuni

**Muhimu — hitilafu iliyogunduliwa na kurekebishwa:** Wakati wa ukaguzi wa mwisho, niligundua kuwa `.getColor()` (iliyokuwa ikitumika kwenye StatusAdapter na sasa Reports) inahitaji Android 6.0+ — lakini app inasapoti kuanzia Android 5.0. Hii ingesababisha **crash** kwenye simu za zamani za Android 5.0/5.1. Nimeibadilisha kote kuwa `ContextCompat.getColor()` inayofanya kazi salama kwenye matoleo yote ya Android tangu 5.0.

Faili mpya/zilizobadilika:
- `app/src/main/java/com/jsdev/bulksms/PieChartView.kt` *(mpya)*
- `app/src/main/java/com/jsdev/bulksms/BarChartView.kt` *(mpya)*
- `app/src/main/java/com/jsdev/bulksms/LineChartView.kt` *(mpya)*
- `app/src/main/res/layout/fragment_reports.xml` *(imebadilika — michoro imeongezwa)*
- `app/src/main/java/com/jsdev/bulksms/ReportsFragment.kt` *(imebadilika — inajaza michoro)*
- `app/src/main/java/com/jsdev/bulksms/StatusAdapter.kt` *(imebadilika — rekebisho la getColor)*

Pakia folda nzima ya `app` upya.

---

## 🆕 TOLEO LA 3 — Bottom Navigation (Kurasa 5)

App sasa ina muundo wa kurasa 5 chini (Bottom Navigation), kama app kubwa za kibiashara:

- **🏠 Home** — muhtasari wa haraka (jumla ya kampeni, zilizotumwa, kiwango cha mafanikio) + vitendo vya haraka
- **✉️ Tuma** — hapa ndipo kazi yote ya awali ipo (pakia faili, andika ujumbe, chagua SIM, tuma, fuatilia status) — HAKUNA kilichoondolewa, kimehamishwa tu hapa
- **👥 Wasiliani** — orodha ya namba/majina yaliyopakiwa mara ya mwisho, yenye utafutaji
- **📊 Ripoti** — takwimu kamili + historia ya kampeni zote + kupakua historia kama .csv
- **⚙️ Mipangilio** — PIN/Usalama, kusimamia templates za ujumbe, kuhusu app

**Muhimu:** Ukibadilisha tab wakati kampeni ya SMS inaendelea kutumwa (kwenye 'Tuma'), kampeni HAIACHI — inaendelea nyuma, unaweza kurudi wakati wowote kuona maendeleo yake.

### Faili mpya/zilizobadilika katika toleo hili:
- `app/build.gradle` *(imeongezewa fragment-ktx)*
- `app/src/main/res/layout/activity_main.xml` *(imebadilika kabisa — sasa ni "shell" ya bottom nav)*
- `app/src/main/java/com/jsdev/bulksms/MainActivity.kt` *(imebadilika kabisa — sasa inasimamia fragments)*
- `app/src/main/java/com/jsdev/bulksms/ComposeFragment.kt` *(mpya — ni MainActivity ya zamani, uwezo wote umehamishwa hapa)*
- `app/src/main/java/com/jsdev/bulksms/HomeFragment.kt` *(mpya)*
- `app/src/main/java/com/jsdev/bulksms/ContactsFragment.kt` *(mpya)*
- `app/src/main/java/com/jsdev/bulksms/ReportsFragment.kt` *(mpya)*
- `app/src/main/java/com/jsdev/bulksms/SettingsFragment.kt` *(mpya)*
- `app/src/main/java/com/jsdev/bulksms/AppState.kt` *(mpya — inashirikisha namba kati ya Tuma na Wasiliani)*
- `app/src/main/res/layout/fragment_home.xml` *(mpya)*
- `app/src/main/res/layout/fragment_compose.xml` *(mpya — ni activity_main.xml ya zamani)*
- `app/src/main/res/layout/fragment_contacts.xml` *(mpya)*
- `app/src/main/res/layout/fragment_reports.xml` *(mpya — ni activity_dashboard.xml iliyoboreshwa)*
- `app/src/main/res/layout/fragment_settings.xml` *(mpya)*
- `app/src/main/res/menu/bottom_nav_menu.xml` *(mpya)*
- `app/src/main/res/drawable/ic_nav_*.xml` *(mpya — icons 5 za menu)*

**Faili za DashboardActivity/activity_dashboard.xml/PinLockActivity zilizokuwepo zinabaki** (PinLockActivity bado inatumika; DashboardActivity haitumiki tena — imebadilishwa na ReportsFragment, unaweza kuipuuzia).

Pakia folda nzima ya `app` upya kwenye GitHub (Add file → Upload files, buruta folda nzima) ili kubadilisha faili zote kwa wakati mmoja.

---

## 🆕 AWAMU YA 1 — Usalama (PIN) + Dashboard/Historia

Faili mpya/zilizobadilika katika awamu hii:
- `app/src/main/java/com/jsdev/bulksms/PinManager.kt` *(mpya)* — PIN inahifadhiwa kama hash (SHA-256+salt), si maandishi wazi
- `app/src/main/java/com/jsdev/bulksms/PinLockActivity.kt` *(mpya)*
- `app/src/main/res/layout/activity_pin_lock.xml` *(mpya)*
- `app/src/main/java/com/jsdev/bulksms/DbHelper.kt` *(mpya)* — kumbukumbu za kampeni (SQLite)
- `app/src/main/java/com/jsdev/bulksms/DashboardActivity.kt` *(mpya)*
- `app/src/main/res/layout/activity_dashboard.xml` *(mpya)*
- `app/src/main/AndroidManifest.xml` *(imebadilika — activities mpya)*
- `app/src/main/res/layout/activity_main.xml` *(imebadilika — vitufe Dashboard/Usalama)*
- `app/src/main/java/com/jsdev/bulksms/MainActivity.kt` *(imebadilika — lock check + kuhifadhi historia)*

**Jinsi inavyofanya kazi:**
- Bofya **"🔒 Usalama"** kwenye skrini kuu kuweka PIN (tarakimu 4-6). Ukiwa umeshaweka, kitufe hicho kinatoa chaguo za kubadilisha PIN, kuweka muda wa auto-lock, au kuzima PIN
- Baada ya kuweka PIN, app itataka PIN kila mara inapofunguliwa (au baada ya muda uliochagua wa kutokutumika)
- Bofya **"📊 Dashboard"** kuona jumla ya kampeni, walengwa, zilizotumwa, zilizofika, zilizoshindwa, na kiwango cha mafanikio (%) — pamoja na historia ya kampeni zote zilizopita
- Historia inahifadhiwa moja kwa moja kila kampeni inapokamilika — hakuna hatua ya ziada inayohitajika

Pakia folda nzima ya `app` upya kwenye GitHub (kama ulivyofanya awali) ili kuingiza masasisho haya yote.

---

## 🆕 TOLEO LA 2 — Premium UI + Function Mpya

Nimeboresha app kikamilifu. Mabadiliko makubwa:
- **Muonekano wa "premium"**: Material Design, rangi za brand (kijani), icon halisi, kadi zenye mviringo
- **Inasoma .xlsx moja kwa moja** (sio CSV/TXT tu) — hakuna maktaba nzito iliyoongezwa (huepuka makosa ya build)
- **Kutambua majina kiotomatiki**: ukiwa na safu ya "jina" kwenye faili lako, andika `{jina}` ndani ya ujumbe na kila mteja atapata ujumbe wenye jina lake mahususi
- **Chaguo la SIM/laini**: dropdown ya kuchagua Vodacom/Tigo/Airtel (kama simu ina SIM 2), au "Chaguo-msingi"
- **Simamisha / Endelea / Komesha**: unaweza kusitisha kampeni katikati na kuendelea baadaye
- **Jaribu Tena Zilizoshindwa**: baada ya kukamilika, kitufe cha kutuma tena kwa namba zilizoshindikana pekee
- **Templates za ujumbe**: hifadhi ujumbe unaotumika mara kwa mara, fungua tena wakati mwingine
- **Tafuta/Search** kwenye orodha ya matokeo
- **Ripoti ya .csv** ya kila mtu na status yake, inayopakuliwa mwishoni

### Jinsi ya kupakia masasisho haya kwenye GitHub (njia rahisi zaidi)

Badala ya kuongeza faili moja moja, ni rahisi zaidi ku-**pakia folda nzima upya**:

1. Pakua `BulkSmsApp.zip` (chini ya ujumbe huu), **fungua/unzip** kwenye kifaa chako
2. Fungua repo yako `BulkSmsApp` kwenye GitHub (browser)
3. Bofya **"Add file" → "Upload files"**
4. **Buruta (drag) folda nzima ya `app`** kutoka kwenye faili ulizozi-unzip moja kwa moja kwenye eneo la upload — GitHub itahifadhi muundo wa folda zote ndani yake kiotomatiki (hii inafanya kazi vizuri kwenye kompyuta/browser kubwa; kwenye baadhi ya simu unaweza kuhitaji kuchagua faili moja moja badala yake)
5. Fanya vivyo hivyo kwa faili zilizobadilika nje ya `app` kama zipo (mfano `SOMA_KWANZA.md`)
6. Bofya **Commit changes** — GitHub itauliza kama unataka "kubadilisha" (replace) faili zilizopo — kubali
7. Nenda **Actions** tab, subiri ujenzi mpya ukamilike, pakua APK mpya kutoka **Artifacts**

**Faili zilizobadilika/mpya katika toleo hili** (kama unapakia moja moja badala ya folda nzima):
- `app/build.gradle` *(imebadilika — imeongezewa Material Design)*
- `app/src/main/AndroidManifest.xml` *(imebadilika — icon na ruhusa mpya)*
- `app/src/main/res/layout/activity_main.xml` *(imebadilika kabisa)*
- `app/src/main/java/com/jsdev/bulksms/MainActivity.kt` *(imebadilika kabisa)*
- `app/src/main/res/values/colors.xml` *(mpya)*
- `app/src/main/res/values/themes.xml` *(mpya)*
- `app/src/main/res/values-night/themes.xml` *(mpya)*
- `app/src/main/res/drawable/ic_launcher.xml` *(mpya)*
- `app/src/main/res/drawable/bg_status_waiting.xml` *(mpya)*
- `app/src/main/res/drawable/bg_status_sending.xml` *(mpya)*
- `app/src/main/res/drawable/bg_status_sent.xml` *(mpya)*
- `app/src/main/res/drawable/bg_status_delivered.xml` *(mpya)*
- `app/src/main/res/drawable/bg_status_failed.xml` *(mpya)*
- `app/src/main/res/layout/item_status.xml` *(mpya)*
- `app/src/main/java/com/jsdev/bulksms/XlsxReader.kt` *(mpya)*
- `app/src/main/java/com/jsdev/bulksms/RecipientParser.kt` *(mpya)*
- `app/src/main/java/com/jsdev/bulksms/StatusAdapter.kt` *(mpya)*

Faili zisizobadilika (usizoze upya, ziache kama zilivyo): `build.gradle` (root), `settings.gradle`, `gradle.properties`, `.github/workflows/build-apk.yml`, `PhoneNormalizer.kt`, `SmsSentReceiver.kt`, `SmsDeliveredReceiver.kt`, `SmsSendTracker.kt`.

### Kuhusu faili ya .xlsx
App inasoma sheet ya kwanza tu ya Excel yako. Kama una safu ya jina na safu ya namba, weka zote mbili kwenye Excel — app itatambua yenyewe ni safu gani ni namba na ipi ni jina, ikitegemea maudhui (haihitaji majina maalum ya column).

---

## NJIA A — Kujenga APK BILA laptop (GitHub Actions, kwa simu tu)

Project hii tayari ina faili zote zinazohitajika (`build.gradle`, `settings.gradle`, na
`.github/workflows/build-apk.yml`) ili GitHub ijijengee APK yenyewe kwenye "cloud" —
wewe unahitaji simu na mtandao tu, hakuna Android Studio wala laptop.

**Hatua:**

1. Fungua **github.com** kwenye browser ya simu, fungua akaunti bure (ukiwa tayari
   una akaunti, ingia tu).
2. Bofya **"+" → New repository**. Ipe jina mfano `bulk-sms-app`, chagua **Private**
   (kwa sababu ni app ya biashara yako), bofya **Create repository**.
3. Ndani ya repo, tumia **"Add file" → "Upload files"** — pakia faili/folda zote za
   BulkSmsApp (unaweza kuchagua faili nyingi kwa mara moja kutoka kwenye "Files" app
   ya simu yako baada ya ku-unzip hii package). Kama browser yako haikubali kupakia
   folda nzima kwa mara moja, tumia **"Create new file"** kwa kila faili — andika
   njia kamili kwenye jina la faili (mfano `app/src/main/java/com/jsdev/bulksms/MainActivity.kt`)
   na GitHub itatengeneza folda zenyewe kiotomatiki, kisha bandika content na Commit.
   **Hakikisha unapakia hata faili zilizo ndani ya `.github/workflows/`** — hilo ndilo
   linaloamuru ujenzi wa APK.
4. Mara commit ya mwisho ikimalizika, nenda kwenye tab ya **"Actions"** ya repo yako —
   utaona "Build APK" ikiendesha yenyewe kiotomatiki (inachukua dakika 3-6).
5. Ikimaliza kwa alama ya kijani (✓), bofya ile run, chini utaona sehemu ya
   **"Artifacts"** — bofya **bulk-sms-app-debug** kuipakua (itakuja kama .zip).
6. Fungua huo .zip kwenye simu (File Manager yoyote inaweza), ndani kuna
   **app-debug.apk** — bofya kuisakinisha (utahitaji kuruhusu "Install from unknown
   sources" kwenye Settings mara ya kwanza).
7. Hiyo ndiyo app yako halisi ya Android — SmsManager halisi, inatumia SIM yako.

**Ukitaka kubadilisha kitu** (mfano ujumbe wa default, rangi, muda wa delay), hariri
faili husika moja kwa moja kwenye tovuti ya GitHub (kila faili ina kalamu ya "Edit" ✎),
Commit, na "Actions" itajijenga upya yenyewe kila mara unapo-commit — huhitaji kugusa
laptop wakati wowote.

---

## NJIA B — Ukipata laptop baadaye (Android Studio)

1. Fungua **Android Studio** → `New Project` → chagua **Empty Views Activity**.
2. Package name uweke: `com.jsdev.bulksms`
3. Baada ya project kuundwa, **badilisha/ongeza** faili zilizomo humu:
   - `app/src/main/AndroidManifest.xml`
   - `app/src/main/java/com/jsdev/bulksms/MainActivity.kt`
   - `app/src/main/java/com/jsdev/bulksms/SmsSentReceiver.kt`
   - `app/src/main/java/com/jsdev/bulksms/SmsDeliveredReceiver.kt`
   - `app/src/main/java/com/jsdev/bulksms/SmsSendTracker.kt`
   - `app/src/main/java/com/jsdev/bulksms/PhoneNormalizer.kt`
   - `app/src/main/res/layout/activity_main.xml`
4. Hakikisha `build.gradle (Module: app)` ina hii dependency (huenda tayari ipo kwa default):
   ```gradle
   implementation "androidx.appcompat:appcompat:1.6.1"
   implementation "androidx.activity:activity-ktx:1.8.0"
   ```
5. Bofya **Sync Now**, kisha **Run ▶** kwenye simu yako halisi (Infinix SMART 9) ikiwa imeunganishwa kwa USB debugging — SIM emulator haiwezi kutuma SMS halisi, lazima simu halisi.

## Jinsi app inavyofanya kazi

1. **Andaa faili la namba** — tumia ile app ya wavuti niliyokutengenezea kwanza (namba-sms.html): pakia Excel yako, ipakue kama `.csv` au `.txt`.
2. Kwenye app hii ya simu, bofya **"Chagua faili la namba"** na uchague hilo faili.
3. App itasoma namba, ikisafisha tena kwa hakika (kuongeza `0` pale inapokosekana) — namba zisizoeleweka hazitumwi, zinaonekana kwenye Logcat.
4. Andika ujumbe wako (mfano: *"Habari [Jina], deni lako la mwezi huu bado halijalipwa. Tafadhali lipa haraka. Asante - JS.Dev"*).
5. Weka muda kati ya ujumbe (chaguo-msingi sekunde 2) — hii ni **muhimu sana**: kutuma SMS nyingi kwa haraka sana kunaweza kusababisha mtandao (Vodacom/Tigo/Airtel) kuzuia namba yako kwa muda (spam protection). Sekunde 2-3 ni salama.
6. Bofya **"Anza Kutuma"** — app itakuomba ruhusa ya SMS mara moja, kisha itatuma kwa namba moja baada ya nyingine ikitumia SIM yako na kifurushi chako.
7. Orodha chini inaonyesha status ya kila namba: `Inasubiri` → `Imetumwa` → `Imefika` (delivered) au `Imeshindwa`.

## Vikwazo vya kweli unavyopaswa kujua

- **Kifurushi chako lazima kiwe na SMS za kutosha** kwa idadi ya wateja — app haiongezi salio, inatumia kilichopo tu.
- **Baadhi ya simu za Android (hasa za bei nafuu kama Infinix)** zina *default SMS app* inayoweza kuzuia app nyingine kutuma SMS moja kwa moja bila idhini ya ziada — ukiona ujumbe haujatumwa, angalia **Settings → Apps → Bulk SMS - JS.Dev → Permissions → SMS** iwe "Allowed".
- **"Imefika" (delivered)** haitaripoti kwa baadhi ya mitandao/muda fulani — hii si hitilafu ya app, ni tabia ya mtandao wa mpokeaji.
- App hii **haihitaji internet** — inatumia mtandao wa simu (GSM) moja kwa moja, kama unavyotuma SMS ya kawaida.
- Kutuma SMS nyingi sana kwa siku moja kwa namba moja ya simu kunaweza kuonekana na mtandao kama tabia ya "spam" — kama wateja ni wengi sana (mamia), fikiria kutumia huduma rasmi ya Bulk SMS (Beem Africa/NextSMS) badala ya SIM binafsi, ili kuepuka namba yako kuzuiliwa.
