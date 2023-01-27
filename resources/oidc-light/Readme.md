# Getting Started

The users and their passwords are listed in the users.json file. It must be present in the working directory.

The issuer field of the token is driven by the parameter **mgu.issuer** (defaulted
to **http://localhost:8001**) and the TTL of the JWT is driven by the parameter
**mgu.ttl** (defaulted to 3600 (ie. one hour)).

To get a JWT: **http -f POST :8001/login username=user password=user-password**

You can then invoke the userinfo endpoint: **http :8001/idp/userinfo.openid "Authorization: bearer <the_token>"**

## to build the gradle program

Before you build or run the app you need to create the key files under resources:
- openssl genrsa -out private_key.pem 2048
- openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt
- openssl rsa -in private_key.pem -pubout -outform DER -out public_key.der

**gradle bootJar**

and to run the app

**java -jar build/libs/oidc-light-0.0.1-SNAPSHOT.jar**
