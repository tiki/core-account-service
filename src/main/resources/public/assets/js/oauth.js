function handleCredentialResponse(response) {
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
        "536431375324-cnmaso5e0q8jslf9a7um4dhc4mhugkf4.apps.googleusercontent.com",
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
  console.log(code);
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
        scope: "account admin"
      }),
    };

    fetch(`https://account.mytiki.com/api/latest/auth/token`, options)
      .then((response) => response.json())
      .then((response) => {
        console.log(response)
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
      "536431375324-cnmaso5e0q8jslf9a7um4dhc4mhugkf4.apps.googleusercontent.com",
    callback: handleCredentialResponse,
  });
  google.accounts.id.renderButton(document.getElementById("googleBtn"), {
    theme: "outline",
    size: "large",
    logo_alignment: "center",
  });
};

function githubLogin() {
  window.location.href = "https://github.com/login/oauth/authorize?client_id=ebbf90361bcb8c527416&scope=user:email"
}
