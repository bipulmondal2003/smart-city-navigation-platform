import { useState, type FormEvent } from "react";
import { useNavigate, Link } from "react-router-dom";
import axios from "axios";
import { useAuth } from "@/contexts/AuthContext";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(email, password);
      navigate("/route");
    } catch (err) {
      setError(describeLoginError(err));
    } finally {
      setLoading(false);
    }
  }

  function describeLoginError(err: unknown): string {
    if (axios.isAxiosError(err)) {
      if (!err.response) {
        return "Can't reach the backend. Is it running on http://localhost:8080?";
      }
      if (err.response.status === 401) {
        return "Invalid email or password.";
      }
      if (err.response.status === 403) {
        return "Login blocked (403) — likely a CORS or security config issue.";
      }
      const backendMessage = (err.response.data as { message?: string } | undefined)?.message;
      return backendMessage ?? `Login failed (HTTP ${err.response.status}).`;
    }
    return "Something went wrong logging in.";
  }

  return (
    <div className="flex h-full items-center justify-center bg-gray-50">
      <form onSubmit={handleSubmit} className="w-full max-w-sm rounded-lg bg-white p-8 shadow-md">
        <h1 className="mb-1 text-xl font-semibold text-gray-800">Welcome back</h1>
        <p className="mb-6 text-sm text-gray-500">Log in to plan routes and save your history.</p>

        <label className="mb-1 block text-sm font-medium text-gray-700">Email</label>
        <input
          type="email"
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="mb-4 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          placeholder="you@example.com"
        />

        <label className="mb-1 block text-sm font-medium text-gray-700">Password</label>
        <input
          type="password"
          required
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="mb-4 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          placeholder="••••••••"
        />

        {error && <p className="mb-4 text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-md bg-primary-600 px-3 py-2 text-sm font-medium text-white hover:bg-primary-700 disabled:opacity-50"
        >
          {loading ? "Logging in…" : "Log in"}
        </button>

        <p className="mt-4 text-center text-sm text-gray-500">
          No account? <Link to="/register" className="text-primary-600 hover:underline">Register</Link>
        </p>

        <div className="mt-6 rounded-md bg-gray-50 p-3 text-xs text-gray-500">
          <div className="font-medium text-gray-600">Demo accounts (seeded):</div>
          <div>Admin: admin@smartcity.local / Admin@123</div>
          <div>Citizen: citizen@smartcity.local / Citizen@123</div>
        </div>
      </form>
    </div>
  );
}
