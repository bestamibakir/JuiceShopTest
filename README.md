# 🧃 Juice Shop — UI Test Otomasyonu

### Selenium Grid · Jenkins · Docker

> **OWASP Juice Shop** uygulaması üzerinde, tamamen Docker konteyner ortamında çalışan, Jenkins CI/CD ve Selenium Grid destekli çapraz tarayıcı (cross-browser) UI test otomasyon projesidir.

---

## 📖 İçindekiler

- [Teknoloji Tanımları](#-teknoloji-tanımları)
- [Kullanılan Teknolojiler](#-kullanılan-teknolojiler)
- [Mimari & Altyapı](#-mimari--altyapı)
- [Proje Yapısı](#-proje-yapısı)
- [Kurulum & Çalıştırma (Adım Adım)](#-kurulum--çalıştırma-adım-adım)
  - [1. Ön Gereksinimler](#1-ön-gereksinimler)
  - [2. SSH Anahtar Çifti Oluşturma](#2-ssh-anahtar-çifti-oluşturma)
  - [3. `.env` Dosyasının Hazırlanması](#3-env-dosyasının-hazırlanması)
  - [4. Docker Network Oluşturma](#4-docker-network-oluşturma)
  - [5. Konteynerlerin Ayağa Kaldırılması](#5-konteynerlerin-ayağa-kaldırılması)
  - [6. Jenkins Yapılandırması](#6-jenkins-yapılandırması)
  - [7. Testlerin Çalıştırılması](#7-testlerin-çalıştırılması)
- [Test Senaryoları](#-test-senaryoları)
- [VNC ile Canlı İzleme](#-vnc-ile-canlı-izleme)
- [Önemli Notlar](#-önemli-notlar)

---

## 🔍 Teknoloji Tanımları

### 🐳 Docker Nedir?

**Docker**, uygulamaları ve tüm bağımlılıklarını birlikte paketleyerek izole ortamlarda (**konteyner**) çalıştırmayı sağlayan bir konteynerizasyon platformudur. "Benim makinemde çalışıyor" sorununu ortadan kaldırır; geliştirme, test ve üretim ortamları arasında tutarlılık sağlar. Docker sayesinde birden fazla servisi (Jenkins, Selenium Hub, tarayıcı node'ları, vb.) tek bir komutla ayağa kaldırıp yönetebilirsiniz.

### 🔧 Jenkins Nedir?

**Jenkins**, açık kaynaklı bir **CI/CD (Sürekli Entegrasyon / Sürekli Dağıtım)** aracıdır. Kod değişikliklerini otomatik olarak derler, test eder ve dağıtır. Bu projede Jenkins; kaynak kodu repodan çeker, Maven ile testleri derler ve Selenium Grid üzerinde koşturur. Tüm bu süreç bir Docker konteyneri içinde otomatik olarak gerçekleşir.

### 🌐 Selenium Grid Nedir?

**Selenium Grid**, test koşumlarını birden fazla makine ve tarayıcıya **paralel** olarak dağıtan bir test altyapısıdır. Merkezî bir **Hub** sunucusu, gelen test isteklerini kayıtlı **Node**'lara (Chrome, Firefox vb.) yönlendirir. Bu sayede aynı anda farklı tarayıcılarda testler koşturularak hem zaman kazanılır hem de çapraz tarayıcı uyumluluğu doğrulanmış olur.

---

## 🛠️ Kullanılan Teknolojiler

| Teknoloji | Versiyon | Açıklama |
|---|---|---|
| **Java** | 21 | Temel programlama dili |
| **Selenium WebDriver** | 4.43.0 | Tarayıcı otomasyonu |
| **TestNG** | 7.12.0 | Test framework'ü & paralel koşum |
| **Maven** | — | Bağımlılık yönetimi & derleme |
| **Docker & Docker Compose** | — | Konteynerizasyon & orkestrasyon |
| **Jenkins (LTS)** | — | CI/CD pipeline yönetimi |
| **Selenium Grid (Hub + Node)** | latest | Dağıtık test koşumu altyapısı |
| **Lombok** | 1.18.36 | Boilerplate kod azaltma |
| **Datafaker** | 2.5.4 | Rastgele test verisi üretimi |

---

## 🏗️ Mimari & Altyapı

Tüm servisler, `sj-net` adında özel bir Docker bridge network'ü üzerinde çalışır. Bu yapı sayesinde konteynerler birbirlerini **servis adıyla** (ör. `selenium-hub`, `juice-shop`) çözümleyebilir ve dış dünyaya bağımlı olmadan iletişim kurabilir.

```
┌─────────────────────────────────────────────────────────────┐
│                   Docker Network: sj-net                    │
│                                                             │
│  ┌─────────────┐  SSH  ┌──────────────┐                     │
│  │   Jenkins   │──────▶│ Jenkins SSH  │                     │
│  │    :8080    │       │    Agent     │                     │
│  │   (CI/CD)   │       │ (Build Node) │                     │
│  └─────────────┘       └──────┬───────┘                     │
│                               │ mvn clean test              │
│                               ▼                             │
│                        ┌──────────────┐                     │
│                        │ Selenium Hub │                     │
│                        │    :4444     │                     │
│                        └──────┬───────┘                     │
│                     ┌─────────┴─────────┐                   │
│                     │                   │                   │
│            ┌────────┴───────┐  ┌────────┴───────┐           │
│            │  Chrome Node   │  │  Firefox Node  │           │
│            │  VNC: :7900    │  │  VNC: :7901    │           │
│            └────────┬───────┘  └────────┬───────┘           │
│                     │                   │                   │
│                     ▼                   ▼                   │
│            ┌────────────────────────────────────┐           │
│            │           Juice Shop               │           │
│            │             :3000                  │           │
│            │      (Test Edilen Uygulama)        │           │
│            └────────────────────────────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

### Akış Özeti

1. **Jenkins**, SSH Agent konteynerinde `mvn clean test` komutunu çalıştırır.
2. Testler, `http://selenium-hub:4444/wd/hub` adresine bağlanarak **Selenium Hub**'a istek gönderir.
3. Hub, gelen istekleri ilgili **Chrome** veya **Firefox Node**'a yönlendirir.
4. Node'lar, `http://juice-shop:3000` adresindeki **Juice Shop** uygulamasına erişir ve UI testlerini gerçekleştirir.

---

## 📁 Proje Yapısı

```
JuiceShopTest/
│
├── sj/                                    # Docker altyapı dosyaları
│   ├── docker-compose.yaml                # Tüm servislerin tanımı
│   ├── .env                               # Docker secret (hassas veri)
│   ├── Jenkins-agent                      # SSH private key dosyası
│   ├── Jenkins-agent.pub                  # SSH public key dosyası
│   └── jenkins-configuration/             # Jenkins kalıcı veri dizini (volume)
│
├── pom.xml                                # Maven bağımlılıkları & Surefire plugin
├── testng.xml                             # Çapraz tarayıcı paralel test konfigürasyonu
├── .gitignore                             # Git'e dahil edilmeyecek dosyalar
├── README.md                              # Proje dokümantasyonu
└── src/test/java/com/bestamibakir/
    ├── BaseTest.java                      # ThreadLocal WebDriver & Selenium Grid bağlantısı
    ├── JuiceShopTest.java                 # Ana test senaryoları
    ├── User.java                          # Lombok @Data @Builder ile User POJO
    ├── data/
    │   └── UserDataBuilder.java           # Datafaker ile rastgele kullanıcı verisi üretimi
    └── pages/                             # Page Object Model (POM) sınıfları
        ├── BasePage.java                  # Tüm sayfa sınıflarının ortak üst sınıfı
        ├── HomePage.java                  # Ana sayfa aksiyonları
        ├── LoginPage.java                 # Giriş sayfası aksiyonları
        └── RegistrationPage.java          # Kayıt sayfası aksiyonları
```

---

## ⚙️ Kurulum & Çalıştırma (Adım Adım)

> ⚠️ **Aşağıdaki adımların sırası önemlidir.** Lütfen belirtilen sırayla ilerleyin.

### 1. Ön Gereksinimler

Aşağıdaki yazılımların sisteminizde kurulu olduğundan emin olun:

- [Docker Desktop](https://docs.docker.com/get-docker/) (Docker Engine + Docker Compose dahil)
- [Git](https://git-scm.com/downloads)

Kurulumu doğrulamak için:

```bash
docker --version
docker compose version
git --version
```

---

### 2. SSH Anahtar Çifti Oluşturma

Jenkins master konteynerinin, SSH Agent konteynerine güvenli bir şekilde bağlanabilmesi için bir SSH anahtar çifti gereklidir. `sj/` dizininde aşağıdaki komutu çalıştırın:

```bash
ssh-keygen -t ed25519 -f Jenkins-agent
```

Bu komut iki dosya oluşturur:
- `Jenkins-agent` → **Private key** (Jenkins credentials'a eklenecek)
- `Jenkins-agent.pub` → **Public key** (`.env` dosyasına yazılacak)

---

### 3. `.env` Dosyasının Hazırlanması

SSH public key'i Docker Compose'a güvenli şekilde aktarmak için `sj/` dizininde bir `.env` dosyası oluşturun. Bu sayede hassas bilgiler `docker-compose.yaml` içine doğrudan yazılmaz:

```bash
# sj/.env
JENKINS_AGENT_SSH_PUBKEY="ssh-ed25519 AAAA... kullanici@host"
```

> 💡 **İpucu:** Public key değerini almak için `cat Jenkins-agent.pub` komutunu kullanabilirsiniz.

---

### 4. Docker Network Oluşturma

Tüm konteynerlerin birbirleriyle iletişim kurabilmesi için önce **özel bir bridge network** oluşturulmalıdır. Network, `docker-compose.yaml` dosyasında `external: true` olarak tanımlandığı için **konteynerleri ayağa kaldırmadan önce** bu adımın tamamlanması zorunludur:

```bash
docker network create sj-net
```

Oluşturulduğunu doğrulamak için:

```bash
docker network ls
```

Çıktıda `sj-net` adında bir network görmelisiniz.

---

### 5. Konteynerlerin Ayağa Kaldırılması

#### 5.1 Docker Compose Dosyası

`sj/docker-compose.yaml` dosyası aşağıdaki **6 servisi** tanımlar:

| Servis | İmaj | Port | Açıklama |
|---|---|---|---|
| `jenkins` | `jenkins/jenkins:lts` | `8080`, `50000` | CI/CD yönetim arayüzü |
| `agent` | `jenkins/ssh-agent:latest-jdk21` | `22` (internal) | Jenkins build agent (JDK 21) |
| `selenium-hub` | `seleniarm/hub:latest` | `4444` | Selenium Grid merkezi hub |
| `chrome` | `seleniarm/node-chromium:latest` | `7900` | Chrome tarayıcı node'u |
| `firefox` | `seleniarm/node-firefox:latest` | `7901` | Firefox tarayıcı node'u |
| `juice-shop` | `bkimminich/juice-shop` | `3000` | Test edilen hedef uygulama |

> 📝 **Not:** ARM mimarili sistemler (Apple Silicon - M1/M2/M3/M4) için `seleniarm` imajları kullanılmıştır. Intel/AMD tabanlı sistemlerde `selenium/hub`, `selenium/node-chrome` ve `selenium/node-firefox` imajlarını kullanabilirsiniz.

#### 📄 docker-compose.yaml İçeriği

```yaml
services:

  # ── Jenkins Master ──────────────────────────────────────────
  # CI/CD yönetim paneli. Web arayüzü :8080, agent iletişimi :50000.
  # jenkins-configuration/ dizini volume olarak bağlanır → konteyner
  # silinse bile ayarlar ve job'lar korunur.
  jenkins:
    image: jenkins/jenkins:lts
    privileged: true
    user: root
    ports:
      - 8080:8080
      - 50000:50000
    container_name: jenkins
    volumes:
      - ./jenkins-configuration:/var/jenkins_home
    networks:
      - sj-net

  # ── Selenium Hub ────────────────────────────────────────────
  # Grid'in merkezî dağıtıcısı. Gelen test isteklerini uygun
  # Chrome/Firefox node'larına yönlendirir.
  selenium-hub:
    image: seleniarm/hub:latest
    ports:
      - "4444:4444"
    networks:
      - sj-net

  # ── Juice Shop (Test Edilen Uygulama) ──────────────────────
  # OWASP Juice Shop — testlerin koşulacağı hedef web uygulaması.
  juice-shop:
    image: bkimminich/juice-shop
    ports:
      - 3000:3000
    networks:
      - sj-net

  # ── Chrome Node ─────────────────────────────────────────────
  # Selenium Hub'a kayıtlı Chrome tarayıcı node'u.
  # noVNC sayesinde :7900 portundan canlı izlenebilir.
  chrome:
    image: seleniarm/node-chromium:latest
    shm_size: 2gb                              # Tarayıcı bellek alanı
    ports:
      - "7900:7900"
    depends_on:
      - selenium-hub
    networks:
      - sj-net
    environment:
      - SE_VNC_NO_PASSWORD=1                   # VNC şifresiz erişim
      - SE_START_VNC=true                      # VNC sunucusunu başlat
      - SE_START_NO_VNC=true                   # noVNC (web tabanlı) başlat
      - SE_EVENT_BUS_HOST=selenium-hub         # Hub adresi
      - SE_EVENT_BUS_PUBLISH_PORT=4442         # Event Bus yayın portu
      - SE_EVENT_BUS_SUBSCRIBE_PORT=4443       # Event Bus dinleme portu
      - SE_NODE_MAX_INSTANCES=1                # Maks. tarayıcı instance sayısı
      - SE_NODE_MAX_SESSIONS=1                 # Maks. eşzamanlı oturum sayısı
      - SE_NODE_SESSION_TIMEOUT=180            # Oturum zaman aşımı (saniye)

  # ── Firefox Node ────────────────────────────────────────────
  # Selenium Hub'a kayıtlı Firefox tarayıcı node'u.
  # noVNC sayesinde :7901 portundan canlı izlenebilir.
  firefox:
    image: seleniarm/node-firefox:latest
    shm_size: 2gb
    ports:
      - "7901:7900"
    depends_on:
      - selenium-hub
    networks:
      - sj-net
    environment:
      - SE_VNC_NO_PASSWORD=1
      - SE_START_VNC=true
      - SE_START_NO_VNC=true
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_EVENT_BUS_PUBLISH_PORT=4442
      - SE_EVENT_BUS_SUBSCRIBE_PORT=4443
      - SE_NODE_MAX_INSTANCES=1
      - SE_NODE_MAX_SESSIONS=1
      - SE_NODE_SESSION_TIMEOUT=180

  # ── Jenkins SSH Agent ───────────────────────────────────────
  # Jenkins master'ın SSH ile bağlanarak build/test koştuğu agent.
  # Public key .env dosyasından okunur.
  agent:
    image: jenkins/ssh-agent:latest-jdk21
    privileged: true
    user: root
    container_name: agent
    expose:
      - 22
    environment:
      - JENKINS_AGENT_SSH_PUBKEY=${JENKINS_AGENT_SSH_PUBKEY}
    networks:
      - sj-net

# ── Network ─────────────────────────────────────────────────
# external: true → Bu network Docker Compose tarafından otomatik
# oluşturulmaz, önceden "docker network create sj-net" ile
# manuel oluşturulması gerekir.
networks:
  sj-net:
    external: true
```

#### 5.2 İmajları Çekme (Pull)

Tüm Docker imajlarını önceden indirmek için (opsiyonel ama önerilir):

```bash
docker pull jenkins/jenkins:lts
docker pull jenkins/ssh-agent:latest-jdk21
docker pull seleniarm/hub:latest
docker pull seleniarm/node-chromium:latest
docker pull seleniarm/node-firefox:latest
docker pull bkimminich/juice-shop
```

#### 5.3 Konteynerleri Başlatma

`sj/` dizinine gidin ve tüm servisleri arka planda başlatın:

```bash
cd sj
docker compose up -d
```

Konteynerlerin durumunu kontrol etmek için:

```bash
docker compose ps
```

Tüm servislerin `running` durumunda olduğunu doğrulayın.

#### 5.4 Servislerin Erişim Adresleri

| Servis | Erişim Adresi |
|---|---|
| Jenkins Arayüzü | [http://localhost:8080](http://localhost:8080) |
| Selenium Grid Dashboard | [http://localhost:4444](http://localhost:4444) |
| Juice Shop Uygulaması | [http://localhost:3000](http://localhost:3000) |
| Chrome VNC (noVNC) | [http://localhost:7900](http://localhost:7900) |
| Firefox VNC (noVNC) | [http://localhost:7901](http://localhost:7901) |

---

### 6. Jenkins Yapılandırması

#### 6.1 İlk Giriş

Jenkins ilk kez başlatıldığında bir **Unlock** şifresi ister. Bu şifreyi aşağıdaki komutla alabilirsiniz:

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

#### 6.2 Gerekli Plugin'ler

Jenkins kurulum sihirbazında **önerilen plugin'leri** yükleyin. Ek olarak aşağıdaki plugin'lerin kurulu olduğundan emin olun:

- **SSH Agent** — SSH üzerinden agent bağlantısı
- **Maven Integration** — Maven projeleri desteği
- **Git** — Git reposu entegrasyonu

#### 6.3 SSH Credentials Ekleme

1. **Jenkins Dashboard** → **Manage Jenkins** → **Credentials** → **(global)** → **Add Credentials**
2. **Kind:** `SSH Username with private key`
3. **ID:** `jenkins-agent-ssh`
4. **Username:** `jenkins`
5. **Private Key → Enter directly:** `Jenkins-agent` dosyasındaki private key içeriğini yapıştırın

#### 6.4 Agent Node Tanımlama

1. **Jenkins Dashboard** → **Manage Jenkins** → **Nodes** → **New Node**
2. **Remote root directory:** `/home/jenkins`
3. **Launch method:** `Launch agents via SSH`
4. **Host:** `agent`
5. **Credentials:** Yukarıda eklenen SSH credential'ı seçin

#### 6.5 Maven Tanımlama

1. **Jenkins Dashboard** → **Manage Jenkins** → **Tools**
2. **Maven installations** → **Add Maven**
3. **Name:** `Maven` (veya tercih ettiğiniz bir isim)
4. **Install automatically** seçeneğini işaretleyin

---

### 7. Testlerin Çalıştırılması

#### Jenkins Üzerinden (Önerilen)

1. Jenkins'te yeni bir **Freestyle Project** veya **Pipeline** oluşturun.
2. **Source Code Management** bölümünde Git repo URL'sini girin.
3. **Build Steps** bölümüne aşağıdaki Maven komutunu ekleyin:

```bash
mvn clean test
```

4. Projeyi **Build Now** ile çalıştırın.

#### Lokal Olarak

Docker altyapısı çalışıyorken, testleri doğrudan kendi makinenizden de koşabilirsiniz (Selenium Hub'ın `localhost:4444` portunda erişilebilir olması gerekir):

```bash
cd JuiceShopTest
mvn clean test
```

---

## 🧪 Test Senaryoları

Projede **2 ana test senaryosu** bulunmaktadır. Her senaryo hem **Chrome** hem de **Firefox** tarayıcılarında paralel olarak koşturulur (`testng.xml` ile yapılandırılmıştır):

### 1. Ana Sayfa Doğrulama Testi (`testJuiceShopHomePage`)

Juice Shop ana sayfasının doğru şekilde yüklendiğini kontrol eder.

| Adım | Açıklama |
|---|---|
| 1 | Juice Shop ana sayfasına git (`http://juice-shop:3000`) |
| 2 | Açılır pencereleri (pop-up) kapat |
| 3 | Sayfa başlığının `"OWASP Juice Shop"` olduğunu doğrula |
| 4 | Arama ikonunun görünür olduğunu doğrula |

### 2. Kullanıcı Kayıt & Giriş Testi (`testUserRegistrationAndLogin`)

Uçtan uca (end-to-end) bir kullanıcı kayıt ve giriş akışını test eder.

| Adım | Açıklama |
|---|---|
| 1 | Ana sayfayı aç ve pop-up'ları kapat |
| 2 | **Datafaker** ile rastgele kullanıcı verisi üret (e-posta, şifre, güvenlik sorusu) |
| 3 | Login sayfasına geç, ardından **Kayıt (Register)** sayfasını aç |
| 4 | Kayıt formunu doldur ve gönder |
| 5 | Kayıt sonrası yönlendirilen Login ekranından oluşturulan bilgilerle giriş yap |
| 6 | Başarılı girişi sepet ikonunun görünürlüğü ile doğrula |

### TestNG Konfigürasyonu (`testng.xml`)

```xml
<suite name="JuiceShop Cross-Browser Test Suite" parallel="tests" thread-count="2">
    <test name="Selenium Grid Chrome Tests">
        <parameter name="browser" value="chrome"/>
        ...
    </test>
    <test name="Selenium Grid Firefox Tests">
        <parameter name="browser" value="firefox"/>
        ...
    </test>
</suite>
```

- **`parallel="tests"`**: Her `<test>` bloğu ayrı bir thread'de koşturulur.
- **`thread-count="2"`**: Aynı anda 2 tarayıcıda paralel test koşumu yapılır.

---

## 🖥️ VNC ile Canlı İzleme

Testler koşulurken tarayıcıda neler olduğunu **canlı** olarak izleyebilirsiniz:

| Tarayıcı | noVNC Adresi |
|---|---|
| Chrome | [http://localhost:7900](http://localhost:7900) |
| Firefox | [http://localhost:7901](http://localhost:7901) |

> 🔓 VNC şifresi kaldırılmıştır (`SE_VNC_NO_PASSWORD=1`), doğrudan bağlanabilirsiniz.

---

## 📌 Önemli Notlar

- **Network önce oluşturulmalıdır.** `docker-compose.yaml` dosyasında network `external: true` olarak tanımlıdır, bu yüzden `docker network create sj-net` komutu konteynerleri başlatmadan **önce** çalıştırılmalıdır.
- **ARM vs x86:** Apple Silicon (M1/M2/M3/M4) kullanıyorsanız `seleniarm/*` imajlarını, Intel/AMD kullanıyorsanız `selenium/*` imajlarını tercih edin.
- **Jenkins verisi kalıcıdır.** `jenkins-configuration/` dizini volume olarak bağlandığı için, konteyner silinse bile Jenkins ayarları ve job'lar korunur.
- **Thread-Safe tasarım:** `BaseTest.java` içindeki `ThreadLocal<WebDriver>` sayesinde paralel koşumlarda her thread'in kendi izole WebDriver instance'ı bulunur — race condition riski yoktur.
- **`.env` dosyası ile güvenlik:** SSH public key gibi hassas bilgiler `docker-compose.yaml` içine doğrudan yazılmak yerine `.env` dosyasından okunur. Bu dosyayı `.gitignore`'a eklemeniz önerilir.

---

## 🗑️ Temizlik

Tüm konteynerleri durdurmak ve kaldırmak için:

```bash
cd sj
docker compose down
```

Konteynerleri, volume'ları ve network'ü tamamen silmek için:

```bash
docker compose down -v
docker network rm sj-net
```

---

<p align="center">
  <b>Bestami Bakır</b> tarafından oluşturulmuştur.
</p>
