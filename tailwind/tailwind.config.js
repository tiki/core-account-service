/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

module.exports = {
  content: ["../src/main/resources/public/pages/*.{html, js}"],
  important: true,
  theme: {
    colors: {
      transparent: "#FFFFFFFF",
      white: "#FFFFFF",
      "yellow-light": "#FFF0BC99",
      yellow: "#FFE68F",
      "yellow-dark": "#FFB822",
      orange: "#EE7F19",
      red: "#C73000",
      pink: "#B5006C",
      gray: "#505C59",
      "gray-dark": "#262E33",
      black: "#1C0000",
      blue: "#00133F",
      green: "#00B272",
    },
    fontFamily: {
      sans: ["Space Grotesk, sans-serif"],
    },
    extend: {
      boxShadow: {
        DEFAULT: "4px 4px 0 0 rgba(0,0,0,0.05)",
      },
    },
  },
  plugins: [],
};
