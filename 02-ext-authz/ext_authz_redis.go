package main

import (
	"context"
	"encoding/json"
	"github.com/go-redis/redis/v8"
	"log"
	"os"
	"time"
)

// https://developer.redis.com/develop/golang/

var (
	ctx = context.Background()
	rdb *redis.Client
)

func init() {
	redisUrl := os.Getenv("REDIS_URL")
	if redisUrl == "" {
		log.Printf("Missing REDIS_URL env variable")
		os.Exit(128)
	}

	rdb = redis.NewClient(&redis.Options{
		Addr:     redisUrl,
		Password: "",
		DB:       0,
	})
}

func getTokenFromRedis(user string) string {
	return testEntry(user)
}

func saveTokenInRedis(userId string, token string, expiration interface{}) bool {
	tm := getTime(expiration)
	log.Printf("expiration time from jtw is: %v", tm)
	log.Printf("calculated duration is: %v", time.Until(tm))
	err := rdb.Set(ctx, userId, token, time.Until(tm))
	return err == nil
}

func saveUserInfoInRedis(token string, userInfo string, expiration interface{}) bool {
	tm := getTime(expiration)
	log.Printf("expiration time from jtw is: %v", tm)
	log.Printf("calculated duration is: %v", time.Until(tm))
	err := rdb.Set(ctx, "go-ui-"+token, userInfo, time.Until(tm))
	return err == nil
}

func getUserInfoFromRedis(token string) string {
	return testEntry("go-ui-" + token)
}

func getTime(expiration interface{}) time.Time {
	var tm time.Time
	switch expiration.(type) {
	case float64:
		log.Printf("expiration is float64")
		tm = time.Unix(int64(expiration.(float64)), 0)
	case int64:
		log.Printf("expiration is int64")
		tm = time.Unix(expiration.(int64), 0)
	case json.Number:
		log.Printf("expiration is jsonNumber")
		v, _ := expiration.(json.Number).Int64()
		tm = time.Unix(v, 0)
	}
	return tm
}

func testEntry(keyName string) string {
	val, err := rdb.Get(ctx, keyName).Result()
	if err == redis.Nil {
		return ""
	} else if err != nil {
		log.Printf("A Redis error occured: %v", err)
		return ""
	} else {
		return val
	}
}
