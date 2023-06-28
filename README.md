###  [🍍 Console](https://console.mytiki.com) &nbsp; ⏐ &nbsp; [📚 Docs](https://docs.mytiki.com)
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

# Layer 0 Authorization Service

An Oauth2 style authorization microservice, used to gate access to TIKI's various Layer 0 services.

For new projects, we recommend signing up at [console.mytiki.com](https://console.mytiki.com) and utilizing one of our
platform-specific SDKs which handle any required L0 auth API calls.

- **🤖 Android: [tiki-sdk-android](https://github.com/tiki/tiki-sdk-android)**
- **🍎 iOS: [tiki-sdk-ios](https://github.com/tiki/tiki-sdk-ios)**
- **🦋 Flutter: [tiki-sdk-flutter](https://github.com/tiki/tiki-sdk-flutter)**

### [🎬 API Reference ➝](https://docs.mytiki.com/reference/l0-auth-info-get)

### Basic Architecture

Supports `jwt-bearer`, `refresh`, and `password` grants. `jwt-bearer` used during social login (Google/GitHub/etc.) to
swap a 3rd-party token for a TIKI L0 service token. `password` grants are used **only** for a passwordless login using
one-time passwords (OTPs) delivered by email.

Tokens are signed [JWTs](https://jwt.io) with `iss`, `exp`, and `iat` claims. `jti`, `sub`, and `aud` claims may be
added depending on the requested authorization. Bearer tokens have a 10-minute expiration with 30 days for refresh
tokens.

The service defaults to anonymous, with no user identification persisted or created. Some L0 Services may require
non-anonymous developer/business accounts (typically for billing) —set `nonAnonymous:true` in request body during signup.

#### Service

A [Spring Boot](https://github.com/spring-projects/spring-boot) microservice
using [Spring Security](https://github.com/spring-projects/spring-security) for token issuance and signing. With common
oauth endpoints (token, revoke, userinfo, and jwks.json) configured as Rest Controllers.

Code follows TIKI's [vertical slice](https://jimmybogard.com/vertical-slice-architecture/) architecture and
nomenclature. For example, business logic to prevent refresh token replays can be found
in `RefreshService.java`

#### Database
[PostgresSQL](https://www.postgresql.org) is used for persistence of user profiles and temporary storage of one-time
passwords and refresh tokens. See `/database` at the project root for database configuration scripts.

#### Infrastructure
As a microservice we utilize a 1 service - 1 database pattern, without state management. Services are containerized
using [Docker](https://www.docker.com) images to scale horizontally based on demand. Images are deployed simply behind
an application load balancer (no k8 needed) to
[Digital Ocean's App Platform](https://docs.digitalocean.com/products/app-platform/). The load balancer sits behind
Cloudflare [Proxied DNS](https://developers.cloudflare.com/fundamentals/get-started/concepts/how-cloudflare-works/) for
basic protection. Email delivery (used in passwordless login) is handled by
[SendGrid's Email API](https://sendgrid.com/solutions/email-api/smtp-service/) service.

Configuration TF scripts are located in the project root under `/infra`. Deployment
driven by GitHub Actions (see `.github/workflows/`) with [Terraform Cloud](https://www.terraform.io).

## Contributors

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="http://tedmarov.github.io"><img src="https://avatars.githubusercontent.com/u/68402820?v=4?s=100" width="100px;" alt="Ted Marov"/><br /><sub><b>Ted Marov</b></sub></a><br /><a href="#infra-tedmarov" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->
