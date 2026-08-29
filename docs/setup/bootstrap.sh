#!/usr/bin/env bash
# Pobiera narzedzia warsztatu do .tooling/ - bez praw administratora,
# bez dotykania czegokolwiek poza katalogiem repozytorium.
#
#   bash docs/setup/bootstrap.sh
#
# Po zakonczeniu:
#   source .tooling/env.sh
#   ./sprawdz env
#
# Nic tu nie laduje sie "do systemu". Kasujesz .tooling/ i po sladzie.
# Skrypt jest idempotentny - mozesz go uruchamiac wielokrotnie.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TOOLING="$ROOT/.tooling"
mkdir -p "$TOOLING"
cd "$TOOLING"

JDK_GLOB="jdk-25*"
MAVEN_VERSION="3.9.11"
MAVEN_DIR="apache-maven-$MAVEN_VERSION"

echo "==> katalog narzedzi: $TOOLING"

# --- JDK 25 (Temurin, portable) -------------------------------------------
# Uwaga na system. Katalog jdk-25* moze zostac po bootstrapie zrobionym
# w innym systemie - typowo: pobrane w PowerShellu, uruchamiane pod WSL.
# Sama obecnosc katalogu nic nie znaczy, trzeba sprawdzic, czy to ten JDK.
_pasuje_do_systemu() {
  local kat="$1"
  case "$(uname -s)" in
    Linux*|Darwin*) [ ! -f "$kat/bin/java.exe" ] ;;
    *)              [ -f "$kat/bin/java.exe" ]   ;;
  esac
}

if compgen -G "$JDK_GLOB" > /dev/null &&    ! _pasuje_do_systemu "$(compgen -G "$JDK_GLOB" | head -1)"; then
  echo "==> UWAGA: w .tooling/ lezy JDK dla innego systemu niz $(uname -s)."
  echo "    Kasuje go i pobieram wlasciwy."
  rm -rf $(compgen -G "$JDK_GLOB")
fi

if compgen -G "$JDK_GLOB" > /dev/null; then
  echo "==> JDK 25 juz jest, pomijam"
else
  echo "==> pobieram Temurin JDK 25 (~140 MB, chwile to potrwa)"
  case "$(uname -s)" in
    Linux*)  OS=linux;   EXT=tar.gz ;;
    Darwin*) OS=mac;     EXT=tar.gz ;;
    *)       OS=windows; EXT=zip ;;
  esac
  case "$(uname -m)" in
    arm64|aarch64) ARCH=aarch64 ;;
    *)             ARCH=x64 ;;
  esac
  curl -fsSL -o "jdk25.$EXT" \
    "https://api.adoptium.net/v3/binary/latest/25/ga/$OS/$ARCH/jdk/hotspot/normal/eclipse"
  if [ "$EXT" = "zip" ]; then unzip -q -o "jdk25.$EXT"; else tar -xzf "jdk25.$EXT"; fi
  rm -f "jdk25.$EXT"
fi
JDK_DIR="$(compgen -G "$JDK_GLOB" | head -1)"

# --- Maven -----------------------------------------------------------------
if [ -d "$MAVEN_DIR" ]; then
  echo "==> Maven $MAVEN_VERSION juz jest, pomijam"
else
  echo "==> pobieram Maven $MAVEN_VERSION"
  curl -fsSL -o maven.zip \
    "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip"
  unzip -q -o maven.zip
  rm -f maven.zip
fi

# --- env.sh ----------------------------------------------------------------
cat > env.sh <<ENVEOF
#!/usr/bin/env bash
# Wygenerowane przez docs/setup/bootstrap.sh - nie edytuj recznie.
# Uzycie:  source .tooling/env.sh
WS_ROOT="\$(cd "\$(dirname "\${BASH_SOURCE[0]}")/.." && pwd)"
export JAVA_HOME="\$WS_ROOT/.tooling/$JDK_DIR"
export MAVEN_HOME="\$WS_ROOT/.tooling/$MAVEN_DIR"
export PATH="\$JAVA_HOME/bin:\$MAVEN_HOME/bin:\$PATH"
export WS_SETTINGS="\$WS_ROOT/docs/setup/settings-central.xml"
export WS_M2="\$WS_ROOT/.tooling/m2"

# Maven 3.9+ czyta MAVEN_ARGS sam. Dzieki temu ZWYKLE polecenie mvn - w dowolnym
# katalogu warsztatu - omija firmowego Nexusa i uzywa naszego repozytorium
# lokalnego, bez pamietania o flagach. Bramki uczestnikow wolaja mvn,
# nie wsmvn, wiec bez tego nie zadzialalyby za proxy.
export MAVEN_ARGS="-s \$WS_SETTINGS -Dmaven.repo.local=\$WS_M2"


# Testcontainers (Quarkus Dev Services) potrafi nie znalezc demona Dockera pod
# Windowsem, gdy uzywasz Rancher Desktop albo Podmana. Wskazujemy go jawnie -
# to jest najczestsza przyczyna "u mnie testy nie startuja" na warsztacie.
if [ -z "\${DOCKER_HOST:-}" ] && command -v docker >/dev/null 2>&1; then
  _ep="\$(docker context inspect -f '{{.Endpoints.docker.Host}}' 2>/dev/null || true)"
  [ -n "\$_ep" ] && export DOCKER_HOST="\$_ep"
  export TESTCONTAINERS_RYUK_DISABLED=true
fi
# Maven z pelna konfiguracja warsztatu: wlasny settings.xml (omija firmowego
# Nexusa) i osobne repozytorium lokalne (nie miesza sie z firmowym ~/.m2).
wsmvn() { mvn -B -s "\$WS_SETTINGS" -Dmaven.repo.local="\$WS_M2" "\$@"; }
export -f wsmvn
ENVEOF
chmod +x env.sh

# --- archetyp warsztatowy --------------------------------------------------
# Instalujemy do LOKALNEGO repozytorium warsztatu, zeby Z02 dzialalo offline
# i zeby wszyscy generowali dokladnie ten sam szkielet.
echo "==> instaluje archetyp returns-service-archetype"
if JAVA_HOME="$TOOLING/$JDK_DIR" "$TOOLING/$MAVEN_DIR/bin/mvn" -B -q \
      -f "$ROOT/archetype/pom.xml" \
      -s "$ROOT/docs/setup/settings-central.xml" \
      -Dmaven.repo.local="$TOOLING/m2" \
      install; then
  echo "    zainstalowany"
else
  echo "    UWAGA: instalacja archetypu nie powiodla sie - Z02 nie zadziala"
fi

# --- petclinic do cache'u ---------------------------------------------------
# Siedem zadan pracuje na petclinicu. Sciagamy go RAZ, tutaj, zeby w trakcie
# zajec `./przygotuj` klonowal z dysku, a nie z sieci. Repozytorium jest
# przypiete na jednym commicie - patrz repo/petclinic/UPSTREAM.
CACHE="$TOOLING/cache/spring-petclinic"
URL="$(grep '^URL=' "$ROOT/repo/petclinic/UPSTREAM" | cut -d= -f2-)"
COMMIT="$(grep '^COMMIT=' "$ROOT/repo/petclinic/UPSTREAM" | cut -d= -f2-)"

if [ -d "$CACHE" ] && git -C "$CACHE" cat-file -e "$COMMIT^{commit}" 2>/dev/null; then
  echo "==> petclinic juz jest w cache'u, pomijam"
else
  echo "==> pobieram petclinica (~11 MB)"
  mkdir -p "$TOOLING/cache"
  if [ -d "$CACHE" ]; then
    git -C "$CACHE" fetch --quiet origin || echo "    UWAGA: nie udalo sie odswiezyc"
  elif git clone --quiet --no-checkout "$URL" "$CACHE"; then
    echo "    sklonowany"
  else
    echo "    UWAGA: nie udalo sie sklonowac - ./przygotuj sprobuje ponownie z sieci"
  fi
fi

echo
echo "==> gotowe. Teraz:"
echo "      source .tooling/env.sh"
echo "      ./sprawdz env"
