
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";
const API_BASE = `${API_BASE_URL}/api/auth`;

async function sendAuthRequest(endpoint, data) {
  const response = await fetch(`${API_BASE}/${endpoint}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  const result = await response.json();

  if (!response.ok) {
    throw new Error(
      result.error || result.message || "Authentication failed."
    );
  }

  return result;
}

export async function loginUser(email, password) {
  return sendAuthRequest("login", {
    email,
    password,
  });
}

export async function registerUser(name, email, password) {
  return sendAuthRequest("register", {
    name,
    email,
    password,
  });
}
