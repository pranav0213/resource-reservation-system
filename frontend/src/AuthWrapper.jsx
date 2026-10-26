
import { useState } from "react";
import App from "./App";
import AuthPage from "./AuthPage";
import "./AuthPage.css";

function getRoleFromToken(token) {
  try {
    const payload = token.split(".")[1];

    const base64 = payload
      .replace(/-/g, "+")
      .replace(/_/g, "/");

    const decoded = JSON.parse(atob(base64));

    return decoded.role === "ADMIN" ? "ADMIN" : "USER";
  } catch {
    return "USER";
  }
}

export default function AuthWrapper() {
  const [token, setToken] = useState(null);

  const role = token ? getRoleFromToken(token) : null;

  function handleLogin(newToken) {
    setToken(newToken);
  }

  function handleLogout() {
    setToken(null);
  }

  if (!token) {
    return <AuthPage onLogin={handleLogin} />;
  }

  return (
    <div>
      <div className="session-bar">
        <span>
          ReserveHub | Logged in as {role}
        </span>

        <button onClick={handleLogout}>
          Logout
        </button>
      </div>

      <App token={token} role={role} />
    </div>
  );
}
