window.onload = function () {
  const headers = new Headers();
  headers.append("content-type", "application/json");
  headers.append("accept", "application/json");
  const options = {
    method: "POST",
    headers,
  };
  fetch("https://account.mytiki.com/api/latest/auth/revoke", options)
    .then(async (response) => {
        console.log(response)
        window.location.href = `https://tiki-dev.mytiki.com`;
    })
    .catch((error) => {
      console.log("error: ", error);
    });
};
