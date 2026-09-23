import { createTheme } from "@mui/material/styles";

export default createTheme({
  palette: {
    primary: { main: "#1976d2" },
    secondary: { main: "#ff9800" },
    background: { default: "#f5f6f8" }
  },
  shape: { borderRadius: 10 },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif'
  },
  components: {
    MuiButton: { defaultProps: { disableElevation: true } },
    MuiCard: { styleOverrides: { root: { height: "100%" } } }
  }
});