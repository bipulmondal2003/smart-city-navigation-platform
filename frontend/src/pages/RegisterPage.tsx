import { useState, type FormEvent } from "react";
import { useNavigate, Link } from "react-router-dom";
import axios from "axios";
import { useAuth } from "@/contexts/AuthContext";

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  function describeRegisterError(err: unknown): string {
    if (axios.isAxiosError(err)) {
      if (!err.response) {
        return "Can't reach the backend. Is it running on http://localhost:8080?";
      }
      if (err.response.status === 409) {
        return "That email is already registered. Try logging in instead.";
      }
      if (err.response.status === 400) {
        const backendMessage = (err.response.data as { message?: string } | undefined)?.message;
        return backendMessage ?? "Please check your details (password must be at least 8 characters).";
      }
      const backendMessage = (err.response.data as { message?: string } | undefined)?.message;
      return backendMessage ?? `Registration failed (HTTP ${err.response.status}).`;
    }
    return "Something went wrong registering.";
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await register(name, email, password);
      navigate("/route");
    } catch (err) {
      setError(describeRegisterError(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex h-full items-center justify-center bg-gray-50">
      <form onSubmit={handleSubmit} className="w-full max-w-sm rounded-lg bg-white p-8 shadow-md">
        <h1 className="mb-1 text-xl font-semibold text-gray-800">Create your account</h1>
        <p className="mb-6 text-sm text-gray-500">New accounts are citizens by default.</p>

        <label className="mb-1 block text-sm font-medium text-gray-700">Name</label>
        <input
          required
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="mb-4 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          placeholder="Jane Doe"
        />

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
          minLength={8}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="mb-4 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          placeholder="At least 8 characters"
        />

        {error && <p className="mb-4 text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-md bg-primary-600 px-3 py-2 text-sm font-medium text-white hover:bg-primary-700 disabled:opacity-50"
        >
          {loading ? "Creating account…" : "Register"}
        </button>

        <p className="mt-4 text-center text-sm text-gray-500">
          Already have an account? <Link to="/login" className="text-primary-600 hover:underline">Log in</Link>
        </p>
      </form>
    </div>
  );
}
