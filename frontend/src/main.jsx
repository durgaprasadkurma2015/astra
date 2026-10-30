import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";

import SessionTimeoutManager from "./components/SessionTimeoutManager.jsx";
import { AuthProvider } from "./context/AuthContext.jsx";
import App from "./App.jsx";

import "./index.css";

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <AuthProvider>
      <BrowserRouter>
        <SessionTimeoutManager />
        <App />
      </BrowserRouter>
    </AuthProvider>
  </React.StrictMode>
);