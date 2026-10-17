
FROM alpine:3.22

RUN apk add --no-cache bash curl python3

WORKDIR /app

COPY test-api.sh  .
COPY test-api-2.sh  .
COPY run-tests.sh .

RUN chmod +x test-api.sh
RUN chmod +x test-api-2.sh

CMD ["./test-api-2.sh"]
