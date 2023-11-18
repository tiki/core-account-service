/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

window.onload = function () {
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
        grant_type: "authorization_code",
        code: code,
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
          element.innerHTML = "Error: Failed to log in. Try again.";
          element.classList.remove("hidden");
          return;
        }
        window.location.href = `https://tiki-dev.mytiki.com/?auth_token=${response.readme_token}`;
      })
      .catch((err) => console.error(err));
  }
};
