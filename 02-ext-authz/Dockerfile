FROM golang:1.17 as build

ENV GO111MODULE=on

WORKDIR /app

COPY go.mod .
COPY go.sum .

RUN go mod download

COPY . .

RUN CGO_ENABLED=0 go build *.go

FROM gcr.io/distroless/base
COPY --from=build /app/ext_authz /
COPY --from=build /app/test/private_key_mgu.pem /test/private_key_mgu.pem
COPY --from=build /app/test/public_key_mgu.pub /test/public_key_mgu.pub

EXPOSE 9000 8000

ENTRYPOINT ["/ext_authz"]