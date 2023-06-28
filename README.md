# TIKI Account

<a href="https://mytiki.com/reference"><img alt="API Reference" src="https://img.shields.io/badge/-API_Reference-white?style=flat&logo=ReadMe&logoColor=white&labelColor=018EF5&color=018EF5"></a><!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-4-orange.svg?style=flat)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

TIKI's account management service —configure and provision access to the data platform. Access security is fundamentally based on the OAuth2 standard utilizing JWTs to gate access for users, customers, and services.

The service itself is stateless (horizontal-scaling) Java (spring-boot) Miniservice with a REST API and a PostgreSQL database. Client libraries and UI can be found as submodules.

## Project Structure

- `/src`: The source code for the Miniservice
- `/database`: Database SQL scripts
- `/infra`: Terraform deployment scripts
- `/console`: Frontend UI for account management
- `/refresh`: Serverless worker for silent token refresh
- `/idp`: Library for clapplications

## Contributing

- Use [GitHub Issues](https://github.com/tiki/tiki-account/issues) to report any bugs you find or to request enhancements.
- If you'd like to get in touch with our team or other active contributors, pop in our 👾 [Discord](https://discord.gg/tiki).
- Please use [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/) if you intend to add code to this project.


### Contributors ✨

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="http://mytiki.com"><img src="https://avatars.githubusercontent.com/u/3769672?v=4?s=100" width="100px;" alt="Mike Audi"/><br /><sub><b>Mike Audi</b></sub></a><br /><a href="https://github.com/tiki/tiki-account/commits?author=mike-audi" title="Code">💻</a> <a href="https://github.com/tiki/tiki-account/pulls?q=is%3Apr+reviewed-by%3Amike-audi" title="Reviewed Pull Requests">👀</a> <a href="#infra-mike-audi" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a> <a href="#maintenance-mike-audi" title="Maintenance">🚧</a> <a href="https://github.com/tiki/tiki-account/commits?author=mike-audi" title="Documentation">📖</a> <a href="https://github.com/tiki/tiki-account/commits?author=mike-audi" title="Tests">⚠️</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/timoguin"><img src="https://avatars.githubusercontent.com/u/671968?v=4?s=100" width="100px;" alt="Tim O'Guin"/><br /><sub><b>Tim O'Guin</b></sub></a><br /><a href="#infra-timoguin" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a> <a href="#security-timoguin" title="Security">🛡️</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/wsb1994"><img src="https://avatars.githubusercontent.com/u/36477199?v=4?s=100" width="100px;" alt="Will B."/><br /><sub><b>Will B.</b></sub></a><br /><a href="#infra-wsb1994" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a></td>
      <td align="center" valign="top" width="14.28%"><a href="http://tedmarov.github.io"><img src="https://avatars.githubusercontent.com/u/68402820?v=4?s=100" width="100px;" alt="Ted Marov"/><br /><sub><b>Ted Marov</b></sub></a><br /><a href="#infra-tedmarov" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->
