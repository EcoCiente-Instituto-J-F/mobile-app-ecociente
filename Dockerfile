# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:19-jdk-jammy

ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH="${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools"

WORKDIR /app


# Dependencias necessarias para instalar o Android SDK
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        wget \
        unzip \
        ca-certificates && \
    rm -rf /var/lib/apt/lists/*


# Android Command Line Tools
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    wget -q \
        https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip \
        -O /tmp/android-tools.zip && \
    unzip -q /tmp/android-tools.zip -d /tmp/android-tools && \
    mv /tmp/android-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm -rf /tmp/android-tools /tmp/android-tools.zip


# Aceita as licencas do Android SDK
RUN yes | sdkmanager --licenses > /dev/null || true


# SDK utilizado pelo EcoCiente
RUN sdkmanager \
    "platform-tools" \
    "platforms;android-36" \
    "build-tools;36.0.0"


# Arquivos principais do Gradle
COPY gradlew gradlew.bat gradle.properties ./

# Kotlin DSL
COPY *.gradle* ./

COPY gradle ./gradle


# Corrige quebra de linha do Windows
RUN sed -i 's/\r$//' gradlew && \
    chmod +x gradlew


# Modulo Android
COPY app ./app


# O .env fica disponivel somente durante esta etapa do build.
# Ele nao e copiado nem armazenado na imagem final.
RUN --mount=type=secret,id=env,target=/app/.env \
    ./gradlew \
        testDebugUnitTest \
        lintDebug \
        assembleDebug \
        --no-daemon


CMD ["sh", "-c", "echo 'EcoCiente Mobile build concluido com sucesso' && ls -lh app/build/outputs/apk/debug"]