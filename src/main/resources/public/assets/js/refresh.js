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

const headers = new Headers();
headers.append("accept", "application/json");
headers.append(
  "Content-Type",
  "application/x-www-form-urlencoded;charset=UTF-8"
);

const options = {
  method: "POST",
  headers,
  credentials: "include",
  body: new URLSearchParams({
    grant_type: "refresh_token",
    username,
    scope: "account admin",
  }),
};

fetch("https://account.mytiki.com/api/latest/auth/token", options).then(
  async (response) => {
    document.getElementById("loading-container").classList.remove("hidden");
    const data = await response.json();
    if (data.readme_token) {
      window.location.href = `https://tiki-dev.mytiki.com/?auth_token=${data.readme_token}`;
    } else document.getElementById("loading-container").classList.add("hidden");
  }
);
