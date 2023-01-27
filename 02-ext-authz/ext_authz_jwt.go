package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"

	"errors"
	"github.com/golang-jwt/jwt/v4"
	"io/ioutil"

	"crypto/rsa"
	"strings"
)

type customClaims struct {
	Scope []string `json:"scope"`
	jwt.StandardClaims
	Roles []string `json:"roles"`
	// MguRoles map[string]string `json:"mguroles"`
}

type UserInfoData struct {
	Subject  string   `json:"sub"`
	Profiles []string `json:"profile"`
}

// location of the files used for signing and verification
const (
	privKeyPath = "test/private_key_mgu.pem"
	pubKeyPath  = "test/public_key_mgu.pub"
)

var (
	verifyKey         *rsa.PublicKey
	signKey           *rsa.PrivateKey
	issuer            string
	authorizedIssuers []string
)

func init() {
	signBytes, err := ioutil.ReadFile(privKeyPath)
	fatal(err)

	signKey, err = jwt.ParseRSAPrivateKeyFromPEM(signBytes)
	fatal(err)

	verifyBytes, err := ioutil.ReadFile(pubKeyPath)
	fatal(err)

	verifyKey, err = jwt.ParseRSAPublicKeyFromPEM(verifyBytes)
	fatal(err)

	issuer = os.Getenv("ISSUER_URL")
	if issuer == "" {
		log.Printf("Missing ISSUER_URL env variable")
		os.Exit(128)
	}

	tmpAuthorizedIssuers := os.Getenv("ALLOWED_ISSUERS")
	if tmpAuthorizedIssuers == "" {
		log.Printf("Missing ALLOWED_ISSUERS env variable")
		os.Exit(128)
	}
	authorizedIssuers = strings.Split(tmpAuthorizedIssuers, ",")

	log.Printf("RSA files read and private/public keys created")
	log.Printf("Issuer URL is: %v", issuer)
	log.Printf("Allowed issuers are: %v", authorizedIssuers)
}

func isIssuerAuthorized(issuer string) bool {
	for _, v := range authorizedIssuers {
		if v == issuer {
			return true
		}
	}
	return false
}

func validateAndBuildToken(xAccessToken string) (string, error) {
	if xAccessToken == "" {
		return "", errors.New("x-access-token is missing")
	}

	token, _, err := new(jwt.Parser).ParseUnverified(xAccessToken, jwt.MapClaims{})
	if err != nil {
		return "", errors.New("x-access-token is invalid (#1)")
	}

	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok {
		return "", errors.New("x-access-token is invalid (#1)")
	}

	issuer := fmt.Sprintf("%v", claims["iss"])

	if !isIssuerAuthorized(issuer) {
		return "", errors.New("x-access-token is about untrusted issuer")
	}

	exp := claims["exp"]
	subject := fmt.Sprintf("%v", claims["sub"])

	log.Printf("Looking for token in redis for user: %v", subject)
	redisToken := getTokenFromRedis(subject)
	if redisToken != "" {
		log.Printf("Returning Redis token")
		return redisToken, nil
	}

	log.Printf("No entry in Redis, calling the issuer userinfo endpoint")

	client := &http.Client{}
	req, _ := http.NewRequest(http.MethodGet, issuer+"/idp/userinfo.openid", nil)

	req.Header.Add("Accept", "application/json")
	req.Header.Add("Authorization", authHeaderPrefix+xAccessToken)

	resp, idpErr := client.Do(req)
	if idpErr != nil {
		log.Printf("error when calling userinfo: %v", idpErr)
		return "", idpErr
	}

	if resp.StatusCode != 200 {
		return "", errors.New("error accessing userinfo endpoint")
	}

	defer resp.Body.Close()
	responseBody, idp2Err := ioutil.ReadAll(resp.Body)
	if idp2Err != nil {
		return "", idp2Err
	}

	// log.Printf("response is %v", string(responseBody))
	var userInfoData UserInfoData
	if unmarshalErr := json.Unmarshal(responseBody, &userInfoData); unmarshalErr != nil {
		return "", unmarshalErr
	}

	newJwt, _ := createToken(userInfoData.Subject, userInfoData.Profiles, exp)
	saveTokenInRedis(subject, newJwt, exp)
	saveUserInfoInRedis(newJwt, string(responseBody), exp)
	return newJwt, nil
}

func createToken(user string, roles []string, exp interface{}) (string, error) {
	// create a signer for rsa 512
	t := jwt.New(jwt.GetSigningMethod("RS512"))
	// set our claims
	m := make(map[string]string)
	for _, name := range roles {
		m[name] = "true"
	}

	t.Claims = customClaims{
		Scope: append([]string{"openid", "profile"}, roles...),
		StandardClaims: jwt.StandardClaims{
			Subject:   user,
			ExpiresAt: int64(exp.(float64)),
			Issuer:    issuer,
		},
		Roles: roles,
		// MguRoles: m,
	}
	// Create token string
	t.Header["kid"] = "go-ext-authz"
	return t.SignedString(signKey)
}
