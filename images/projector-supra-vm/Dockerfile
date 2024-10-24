FROM supraaxes/supravm:latest

RUN apt update && DEBIAN_FRONTEND=noninteractive apt install -y \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt requirements.txt
RUN pip install --break-system-packages --no-cache-dir -r requirements.txt && rm requirements.txt

COPY impacket.tar.xz /usr/local/lib/python3.11/dist-packages
RUN cd /usr/local/lib/python3.11/dist-packages \
    && tar xvf impacket.tar.xz \
    && rm impacket.tar.xz

COPY supra supra
