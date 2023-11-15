/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

// eslint-disable-next-line no-unused-vars
function githubLogin() {
  window.location.href =
    "https://github.com/login/oauth/authorize?client_id=ebbf90361bcb8c527416&scope=user:email";
}

function handleGoogleSignin(response) {
  let headers = new Headers();
  headers.append("Content-Type", "application/x-www-form-urlencoded");
  headers.append("Accept", "application/json");

  const options = {
    method: "POST",
    headers: headers,
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:token-exchange",
      subject_token: response.credential,
      subject_token_type: "urn:mytiki:params:oauth:token-type:google",
      client_id:
        "240428403253-buvkqgjamee7vqv9dmll0da69m1mpu04.apps.googleusercontent.com",
      scope: "account:admin",
    }),
  };

  fetch(`https://account.mytiki.com/api/latest/auth/token`, options)
    .then((response) => response.json())
    .then((response) => {
      if (!response.readme_token) {
        let element = document.getElementById("error");
        element.innerHTML =
          "Hey! Something got bad with your signin, try again in a few minutes";
        element.classList.remove("hidden");
        return;
      }
      window.location.href = `https://tiki-dev.mytiki.com/?auth_token=${response.readme_token}`;
    })
    .catch((err) => console.error(err));
}

function handleGithubSign() {
  const url_string = window.location.href.toLowerCase();
  const url = new URL(url_string);
  const code = url.searchParams.get("code");
  if (code) {
    const headers = new Headers();
    headers.append("Content-Type", "application/x-www-form-urlencoded");
    headers.append("Accept", "application/json");

    const options = {
      method: "POST",
      headers: headers,
      body: new URLSearchParams({
        grant_type: "urn:ietf:params:oauth:grant-type:token-exchange",
        subject_token: code,
        subject_token_type: "urn:mytiki:params:oauth:token-type:github",
        client_id: "ebbf90361bcb8c527416",
        scope: "account admin",
      }),
    };

    fetch(`https://account.mytiki.com/api/latest/auth/token`, options)
      .then((response) => response.json())
      .then((response) => {
        if (!response.readme_token) {
          let element = document.getElementById("error");
          element.innerHTML =
            "Hey! Something got bad with your signin, try again in a few minutes";
          element.classList.remove("hidden");
          return;
        }
        window.location.href = `https://tiki-dev.mytiki.com/?auth_token=${response.readme_token}`;
      })
      .catch((err) => console.error(err));
  }
}

window.onload = function () {
  handleGithubSign();

  google.accounts.id.initialize({
    client_id:
      "240428403253-buvkqgjamee7vqv9dmll0da69m1mpu04.apps.googleusercontent.com",
    callback: handleGoogleSignin,
  });
  google.accounts.id.renderButton(document.getElementById("googleBtn"), {
    theme: "outline",
    size: "large",
    logo_alignment: "center",
  });
};
