FROM grafana/alloy:v1.20.1

COPY observability/config-render.alloy /etc/alloy/config.alloy

CMD ["run", "--server.http.listen-addr=127.0.0.1:12345", "/etc/alloy/config.alloy"]