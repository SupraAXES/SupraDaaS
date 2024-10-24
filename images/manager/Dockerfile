FROM debian:12-slim

RUN echo 'QA-date-20241017'

RUN apt update && apt install -y --no-install-recommends \
    apt-transport-https \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

RUN apt update && apt upgrade -y \
    && apt install -y --no-install-recommends \
    python3 \
    python3-pip \
    python3-netifaces \
    nginx \
    rsyslog \
    qemu-utils \
    && rm -rf /var/lib/apt/lists/*


COPY requirements.txt requirements.txt
RUN pip install --break-system-packages --no-cache-dir -r requirements.txt && rm requirements.txt


RUN adduser --disabled-password --gecos "" --uid 1711 supra

COPY rsyslog.conf /etc/rsyslog.conf
COPY supra supra

RUN mkdir -p /supra/logs/vmmgr
ENV PATH=/supra/bin:$PATH
ENV PYTHONPATH=/supra/pymodule


HEALTHCHECK CMD health-check.py
ENTRYPOINT ["entry.sh"]
