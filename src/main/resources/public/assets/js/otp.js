/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

const decodedCookie = decodeURIComponent(document.cookie);
const cookieName = "TikiAccountOtpUser=";
let username;
const ca = decodedCookie.split(";");
for (let i = 0; i < ca.length; i++) {
  let c = ca[i];
  while (c.charAt(0) === " ") {
    c = c.substring(1);
  }
  if (c.indexOf(cookieName) === 0) {
    username = c.substring(cookieName.length, c.length);
  }
}

const authenticateReadme = (otp) => {
  document.getElementById("loading-container").classList.remove("hidden");
  const headers = new Headers();
  headers.append("accept", "application/json");
  headers.append(
    "Content-Type",
    "application/x-www-form-urlencoded;charset=UTF-8",
  );

  const options = {
    method: "POST",
    headers,
    body: new URLSearchParams({
      grant_type: "password",
      username,
      password: otp.toUpperCase(),
      scope: "account admin",
    }),
  };

  fetch(`https://account.mytiki.com/api/latest/auth/token`, options).then(
    async (response) => {
      const data = await response.json();
      if (!data.readme_token) {
        const element = document.getElementById("error");
        element.classList.remove("hidden");
        document.getElementById("loading-container").classList.add("hidden");
        return;
      }
      window.location.href = `https://tiki-dev.mytiki.com/?auth_token=${data.readme_token}`;
    },
  );
};

const url_string = window.location.href.toLowerCase();
const url = new URL(url_string);
const code = url.searchParams.get("code");

if (code) {
  authenticateReadme(code);
}
document.getElementById("code").addEventListener("keydown", (_event) => {
  const element = document.getElementById("error");
  element.classList.add("hidden");
});
document.getElementById("tikiOtpForm").addEventListener("submit", (event) => {
  event.preventDefault();
  const data = new FormData(event.target);
  const dataObject = Object.fromEntries(data.entries());
  if (!dataObject.code) {
    const element = document.getElementById("error");
    element.innerHTML = "The code field is required!";
    element.classList.remove("hidden");
  }
  authenticateReadme(dataObject.code);
});
