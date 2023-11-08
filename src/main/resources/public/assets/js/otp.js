let decodedCookie = decodeURIComponent(document.cookie);
let cookieName = "TikiAccountOtpUser=";
let username;
let ca = decodedCookie.split(";");
for (let i = 0; i < ca.length; i++) {
  let c = ca[i];
  while (c.charAt(0) == " ") {
    c = c.substring(1);
  }
  if (c.indexOf(cookieName) == 0) {
    username = c.substring(cookieName.length, c.length);
  }
}

const authenticateReadme = (otp) => {
  let headers = new Headers();
  headers.append("accept", "application/json");
  headers.append(
    "Content-Type",
    "application/x-www-form-urlencoded;charset=UTF-8"
  );

  const options = {
    method: "POST",
    headers: headers,
    body: new URLSearchParams({
      grant_type: "password",
      username: username,
      password: otp,
      scope: "account admin",
    }),
  };

  fetch(`https://account.mytiki.com/api/latest/auth/token`, options).then(
    async (response) => {
      const data = await response.json();
      if (!data.readme_token) {
        let element = document.getElementById("error");
        element.classList.remove("hidden");
        return;
      }
      window.location.href = `https://tiki-dev.mytiki.com/?auth_token=${data.readme_token}`;
    }
  );
};

let url_string = window.location.href.toLowerCase();
let url = new URL(url_string);
let code = url.searchParams.get("code");

if (code) {
  authenticateReadme(code);
}
document.getElementById("code").addEventListener("keydown", (event)=>{
    let element = document.getElementById("error");
    element.classList.add("hidden")
})
document.getElementById("tikiOtpForm").addEventListener("submit", (event) => {
  event.preventDefault();
  const data = new FormData(event.target);
  const dataObject = Object.fromEntries(data.entries());
  if (!dataObject.code) {
    let element = document.getElementById("error");
    element.innerHTML = "The code field is required!";
    element.classList.remove("hidden");
  }
  authenticateReadme(dataObject.code);
});
