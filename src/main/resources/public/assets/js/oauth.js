const googleCliendId = "240428403253-buvkqgjamee7vqv9dmll0da69m1mpu04.apps.googleusercontent.com"
const githubClientId = "ebbf90361bcb8c527416"

function githubLogin() {
  window.location.href =
    "https://github.com/login/oauth/authorize?scope=user:email&client_id=" +
    githubClientId;
}

function googleLogin() {
  window.location.href =
    "https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount?" +
    "access_type=offline&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile%20https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email" +
    "&prompt=select_account&redirect_uri=https://account.mytiki.com/pages/login&response_type=code&client_id=" +
    googleCliendId;
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
      client_id: googleCliendId,
      scope: "account:admin",
    }),
  };

  fetch(`https://account.mytiki.com/api/latest/auth/token`, options)
    .then((response) => response.json())
    .then((response) => {
      if (!response.readme_token) {
        let element = document.getElementById("error");
        element.innerHTML = "Error: Please try again.";
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
    document.getElementById("loading-container").classList.remove("hidden");
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
        client_id: githubClientId,
        scope: "account admin",
      }),
    };

    fetch(`https://account.mytiki.com/api/latest/auth/token`, options)
      .then((response) => response.json())
      .then((response) => {
        if (!response.readme_token) {
          let element = document.getElementById("error");
          element.innerHTML = "Error: Please try again.";
          element.classList.remove("hidden");
          document.getElementById("loading-container").classList.add("hidden");
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
    client_id: googleCliendId,
    callback: handleGoogleSignin,
  });
  document
    .getElementById("google-sign-in-btn")
    .addEventListener("click", (_e) => googleLogin());
  document
    .getElementById("github-sign-in-btn")
    .addEventListener("click", (_e) => githubLogin());
};
