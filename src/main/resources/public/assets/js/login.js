/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

document.getElementById("tikiLoginForm").addEventListener("submit", (event) => {
  event.preventDefault();
  const data = new FormData(event.target);
  const dataObject = Object.fromEntries(data.entries());

  const headers = new Headers();
  headers.append("content-type", "application/json");
  headers.append("accept", "application/json");
  const options = {
    method: "POST",
    headers,
    body: JSON.stringify({ email: dataObject.email, notAnonymous: true }),
  };

  fetch("https://account.mytiki.com/api/latest/auth/otp", options)
    .then(async (response) => {
      const data = await response.json();
      const self = window;
      document.cookie = `TikiAccountOtpUser=${data.deviceId}; expires=${data.expires};`;
      self.location.replace("./otp.html");
    })
    .catch((error) => {
      console.log("error: ", error);
    });
});
