package main

import (
	"encoding/base64"
	"encoding/json"
	"io/ioutil"
	"log"
	"math/big"
	"net/http"
)

type PublicKeysData struct {
	Keys []KeyData `json:"keys"`
}

type KeyData struct {
	Kty string `json:"kty"`
	Kid string `json:"kid"`
	Use string `json:"use"`
	N   string `json:"n"`
	E   string `json:"e"`
}

type WellKnownData struct {
	Issuer   string `json:"issuer"`
	JwksUri  string `json:"jwks_uri,omitempty"`
	UserInfo string `json:"userinf_endpoint"`
}

func serveHome(response http.ResponseWriter) {
	log.Printf("[HTTP] / requested")
	response.WriteHeader(http.StatusOK)
	_, _ = response.Write([]byte("ok"))
}

func serveFavIcon(response http.ResponseWriter) {
	log.Printf("[HTTP] favicon requested")
	fileBytes, err := ioutil.ReadFile("openid-connect-oauth-logo.png")
	if err != nil {
		panic(err)
	}
	response.WriteHeader(http.StatusOK)
	response.Header().Set("Content-Type", "application/octet-stream")
	_, _ = response.Write(fileBytes)
}

func serveJWKs(response http.ResponseWriter) {
	log.Printf("[HTTP] jwks requested")
	n := base64.URLEncoding.EncodeToString((*verifyKey.N).Bytes())
	e := base64.StdEncoding.EncodeToString(big.NewInt(int64(verifyKey.E)).Bytes())
	keys := PublicKeysData{
		Keys: []KeyData{KeyData{"RSA", "go-ext-authz", "sig", n, e}},
	}

	response.WriteHeader(http.StatusOK)

	b, _ := json.Marshal(keys)
	_, _ = response.Write([]byte(b))
}

func serveOIDC(response http.ResponseWriter) {
	log.Printf("[HTTP] jwks requested")
	wkd := WellKnownData{issuer, issuer + "/.well-known/jwks.json", issuer + "/idp/userinfo.openid"}
	b, _ := json.Marshal(wkd)
	response.Header().Set("Content-Type", "application/json")
	response.Header().Set("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate")
	response.Header().Set("Expires", "0")
	response.Header().Set("Pragma", "no-cache")
	response.WriteHeader(http.StatusOK)
	_, _ = response.Write([]byte(b))
}

func serveUserInfo(response http.ResponseWriter, request *http.Request) {
	log.Printf("[HTTP] userinfo requested")
	authorizationHeader := request.Header.Get(authHeader)
	log.Printf("[HTTP] auth header is: %s", authorizationHeader)
	if authorizationHeader == "" {
		log.Printf("missing Authorization header")
		response.WriteHeader(http.StatusBadRequest)
		return
	}

	token := authorizationHeader[len(authHeaderPrefix):]
	log.Printf("[HTTP] token is: %s", token)

	userinfo := getUserInfoFromRedis(token)
	if userinfo == "" {
		response.WriteHeader(http.StatusNoContent)
		return
	}

	response.Header().Set("Content-Type", "application/json")
	response.WriteHeader(http.StatusOK)
	log.Printf("[HTTP] user info in redis is: %s", userinfo)

	_, _ = response.Write([]byte(userinfo))
}

func (s *ExtAuthzServer) ServeHTTP(response http.ResponseWriter, request *http.Request) {
	switch request.URL.Path {
	case "/", "":
		serveHome(response)
		return
	case "/.well-known/openid-configuration":
		serveOIDC(response)
		return
	case "/.well-known/jwks.json":
		serveJWKs(response)
		return
	case "/idp/userinfo.openid":
		serveUserInfo(response, request)
		return
	case "/favicon.ico":
		serveFavIcon(response)
		return
	}

	// this one to test the validation in HTTP :)
	log.Printf("[HTTP] path requested: %s", request.URL.Path)
	log.Printf("[HTTP] checking the request")
	afklATHeader := request.Header.Get(afklATHeaderName)
	log.Printf("[HTTP] afkl header is: %s", afklATHeader)

	generatedToken, tokenErr := validateAndBuildToken(afklATHeader)
	if tokenErr != nil {
		log.Printf("validateAndBuildToken error: %v", tokenErr)
		response.WriteHeader(http.StatusForbidden)
		_, _ = response.Write([]byte("generic AF/KL header error"))
		return
	}

	response.Header().Set(afklATHeaderName, afklATHeader)
	response.Header().Set(authHeader, authHeaderPrefix+generatedToken)
	response.WriteHeader(http.StatusOK)
}
