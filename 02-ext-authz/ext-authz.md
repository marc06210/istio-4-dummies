# What is this documentation about?

This file describes in details what the Go module does.

## Overview

The Go module has two purposes:

- check if a request can reach a service
- act as an OIDC provider. As it acts as an OIDC provider, it will generate a new JWT that will
  replace the incoming one.

In order to do so, it listens on two different interface types: gRPC and HTTP. The gRPC interface
will be used for the request validation and the HTTP one to expose the OIDC interface.

It relies on a REDIS database for some cache mechanism (generated JWT and response of the request
to the userinfo endpoint ofthe original OIDC server).

At the end of the documentation is a small warnin section about what is not covered in this version
and should be done in order to bee used in production.

## Startup

We expect some environment variables:

- ISSUER_URL<br/> will be used to fill the **iss** claim of the generated JWT
- ALLOWED_ISSUERS<br/> list of issuers from which we allow incoming JWT
- REDIS_URL<br/> URL of our REDIS instance

We starts the gRPC listener on port 9000 (or whatever port injected in command line **-grpc=xxx**).

We starts the HTTP listener on port 8000 (or whatever port injected in command line **-http=xxx**).

We connect to a REDIS instance.

We load two key files (one private and one public) used to generate and validate a JWT.

## Generated JWT - what for and what's in it?

Why do we want to generate a new JWT?

It is because we want all applications inside our kube cluster to talk to our custom and dedicated
authorization server instead of the original one. This is mainly done
when your authorization server is not reaaly scallable and your application relies on a lot
of interactions betwenn micro-services. Just image one service is calling 10 or more other services
and each of them request to validate the JWT, we could easily have some issues if our network
holds many applications of the type and all tergetting a single (not scallable OIDC server).

Our module will generate token that will also holds all the authorizations/roles of the user that
are not present in the original JWT but retrieved by calling the userinfo endpoint. They are
stored in two different places in the generated JWT (in order to play with different configuration
in spring-security). We insert them inside the claim **scope** and also inside a custom claim
**roles**.

## REDIS - what for?

The Go module generates a JWT, this is a time consuming process. In order to speed up further requests
on the same user, once a JWT is generated by the Go module it will be saved in the REDIS database. This
REDIS entry will have a TTL calculated from the expiration of the incoming JWT.

We will also store in the REDIS the result of the invocation to the userinfo enpoint that we made to
generate the JWT. The TTL of this entry is also set to match the TTL of the incoming JWT.

Those REDIS TTL ensures that if we process a request after the expiration of the initial JWT, the REDIS
lookup will return nothing. We will then have to request the original OIDC provider that will validate

## gRPC interface and the validation process

The gRPC interface listens on port **9000**. Our gRPC interface accepts envoy authentication requests in
versions 2 and 3.

The first think done during validation is the extraction of the HTTP header holding the JWT.
In our case, the header is a custom one called **x-access-token** and the there is no prefix with
the token. So, the name of the header is **x-access-token** and its value is the JWT.

If the **x-access-token** is not present or equal to an empty string, then the request is denied.

If we have a value for the JWT, then we parse it without checking the signature. We do not need
to check the signature, because we will invoke the userinfo endpoint of the server that has
generated the token to have the user details. So, if the token is not valid, the OIDC server
will reject our request.

We then extract the follwoing claims from the token **iss**, **exp** and **sub**.

The **iss** field must be listed in the **TRUSTED_ISSUERS** environment variable, otherwise
the request is denied.

At this point we check in REDIS if already have a generatedd JWT with the key equals to the **sub**.
If we have an antry in REDIS, we allow the request and inject into the request a new header
**Authorization** whose content is **bearer generatedJWT**.

If there is no entry in REDIS, we invoke the userinfo endpoint of the original OIDC server. We
extract from the response the **authorities** or **roles** of the user. We generate a new JWT
containing the user details, the same TTL as the incoming JWT and the user roles(\*).

We then save in REDIS the result of the invocation to the userinfo endpoint and the generated
JWT. Those REDIS entities have the same TTL as the incoming JWT.

We allow the request and inject into the request a new header
**Authorization** whose content is **bearer generatedJWT**.

(\*) the user roles are inserted into two different claims of the JWT: **scope** and **roles**.

## HTTP interface and the OIDC exposition

The HTTP interface listens on port **8000**.

We expose a very small subset of what an OIDC server can expose. This is because the applications
we have created with spring-security do not require a lot of different interactions with this OIDC
server.

Here are the exposed endpoints:

- / <br/> returns ok
- /favicon.ico <br/> returns a logo
- /idp/userinfo.openid <br/> returns the REDIS content stored after the invocation to the original
  OIDC server
- /.well-known/jwks.json <br/> returns the public key used to validate the signature of the generated
  JWT
- /.well-known/openid-configuration <br/> returns our limited OIDC configuration (ie. jwks and userinfo)

## Warning section - non covered features

The ports used for the gRPC and HTTP interfaces are customized from the command line. Other variables
are taken from environment variables. We should get the same way to define variables.

Our REDIS instance is not protected. It must be protected. The key that we use to store data into
REDIS isso far the content of **sub**. This may be changed depending on the context.

Revocation of incoming tokens is not covered. If an incoming JWT is revoked before it has expired, we have
no way to detect it.

The URL of the userinfo endpoint should be retrieved by calling the **.well-known/openid-configuration**
endpoint and extracting the **userinfo** URL.