/** @type {import('tailwindcss').Config} */
const defaultTheme = require('tailwindcss/defaultTheme')

module.exports = {
  content: [
    "./src/**/*.{html,js}",
    "./login/*.{html, js}"
  ],
  important: true, 
  theme: {
    colors: {
        'tiki-yellow-xxlight': '#FFF8DD',
        'tiki-dark-blue': '#00133f',
        'tiki-black-xlight': '#505C59',
        'white': '#FFFFFF',
        'yellow-dark': '#FFB822',
        'black': '#1C0000',

    },
    fontFamily: {
      sans: ['Space Grotesk, sans-serif']
    },
    extend:{}
  },
  plugins: [],
}

