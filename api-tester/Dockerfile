FROM alpine:3.22

RUN apk add --no-cache bash curl python3

WORKDIR /app

COPY test-api.sh .
COPY test-api-2.sh .
COPY run-tests.sh .

RUN sed -i 's/\r$//' test-api.sh test-api-2.sh run-tests.sh \
    && chmod +x test-api.sh test-api-2.sh run-tests.sh

CMD ["./test-api-2.sh"]