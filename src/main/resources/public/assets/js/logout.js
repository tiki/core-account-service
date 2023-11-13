window.onload = function () {
  const headers = new Headers();
  headers.append("Content-Type", "application/x-www-form-urlencoded");
  const options = {
    method: "POST",
    headers,
  };
  fetch("https://account.mytiki.com/api/latest/auth/revoke", options)
    .then(async (response) => {
        window.location.href = `https://tiki-dev.mytiki.com`;
    })
    .catch((error) => {
      console.log("error: ", error);
    });
};
