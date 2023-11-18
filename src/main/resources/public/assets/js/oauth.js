/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

const googleCliendId =
  "240428403253-buvkqgjamee7vqv9dmll0da69m1mpu04.apps.googleusercontent.com";
const githubClientId = "ebbf90361bcb8c527416";

function githubLogin() {
  window.location.href =
    "https://github.com/login/oauth/authorize?scope=user:email&client_id=" +
    githubClientId;
}

function googleLogin() {
  window.location.href =
    "https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount?" +
    "access_type=offline&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile%20https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email" +
    "&prompt=select_account&redirect_uri=https://account.mytiki.com/pages/code/google.html&response_type=code&client_id=" +
    googleCliendId;
}

window.onload = function () {
  google.accounts.id.initialize({
    client_id: googleCliendId,
  });
  document
    .getElementById("google-sign-in-btn")
    .addEventListener("click", (_e) => googleLogin());
  document
    .getElementById("github-sign-in-btn")
    .addEventListener("click", (_e) => githubLogin());
};
