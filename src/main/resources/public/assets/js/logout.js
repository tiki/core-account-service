/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

window.onload = function () {
  const headers = new Headers();
  headers.append("Content-Type", "application/x-www-form-urlencoded");
  const options = {
    method: "POST",
    headers,
    credentials: "include",
  };
  fetch("https://account.mytiki.com/api/latest/auth/revoke", options)
    .then(async (_response) => {
      window.location.href = `https://mytiki.com`;
    })
    .catch((error) => {
      console.log("error: ", error);
    });
};
