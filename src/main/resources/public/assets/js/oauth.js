const googleCliendId = "240428403253-buvkqgjamee7vqv9dmll0da69m1mpu04.apps.googleusercontent.com"
const githubClientId = "ebbf90361bcb8c527416"

function githubLogin() {
  localStorage.setItem("oauthType", "github")
  window.location.href = "https://github.com/login/oauth/authorize?scope=user:email&client_id=" + githubClientId
}

function googleLogin(){
  localStorage.setItem("oauthType", "google")
   window.location.href = "https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount?"
    + "access_type=offline&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile%20https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email"
    + "&prompt=select_account&redirect_uri=https://account.mytiki.com/pages/login&response_type=code&client_id=" + googleCliendId
}

function handleSignIn() {
  const oauthType = localStorage.getItem("oauthType")

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
        subject_token_type: "urn:mytiki:params:oauth:token-type:" + oauthType,
        client_id: oauthType === "github" ? githubClientId : googleCliendId,
        scope: "account admin"
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
  google.accounts.id.initialize({
    client_id: googleCliendId,
  });
  handleSignIn();
  document.getElementById('google-sign-in-btn').addEventListener('click', (e) => googleLogin())
  document.getElementById('github-sign-in-btn').addEventListener('click', (e) => githubLogin())
};
